package com.flamapp.edgedetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flamapp.edgedetection.gl.GLRenderer
import com.flamapp.edgedetection.jni.NativeProcessor
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: GLRenderer
    private lateinit var fpsText: TextView
    private lateinit var modeText: TextView
    private lateinit var toggleButton: Button
    
    private lateinit var nativeProcessor: NativeProcessor
    private lateinit var cameraExecutor: ExecutorService
    
    private var edgeDetectionEnabled = true
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    private var currentFps = 0.0
    
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glSurfaceView = findViewById(R.id.glSurfaceView)
        fpsText = findViewById(R.id.fpsText)
        modeText = findViewById(R.id.modeText)
        toggleButton = findViewById(R.id.toggleButton)

        glSurfaceView.setEGLContextClientVersion(2)
        glRenderer = GLRenderer()
        glSurfaceView.setRenderer(glRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        nativeProcessor = NativeProcessor()
        cameraExecutor = Executors.newSingleThreadExecutor()

        toggleButton.setOnClickListener {
            edgeDetectionEnabled = !edgeDetectionEnabled
            updateModeText()
        }

        updateModeText()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        startFpsUpdate()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { image ->
                processImage(image)
                image.close()
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalysis
                )
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(image: ImageProxy) {
        val startTime = System.currentTimeMillis()
        
        val width = image.width
        val height = image.height
        
        // Convert YUV_420_888 to NV21 properly
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        
        val ySize = yBuffer.remaining()
        val nv21 = ByteArray(width * height * 3 / 2)
        
        // Copy Y plane (may have row padding)
        if (yPlane.rowStride == width) {
            yBuffer.get(nv21, 0, ySize)
        } else {
            var pos = 0
            for (row in 0 until height) {
                yBuffer.position(row * yPlane.rowStride)
                yBuffer.get(nv21, pos, width)
                pos += width
            }
        }
        yBuffer.rewind()
        
        // Interleave VU for NV21
        val uvPixelStride = vPlane.pixelStride
        val uvRowStride = vPlane.rowStride
        var pos = width * height
        
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val offset = row * uvRowStride + col * uvPixelStride
                vBuffer.position(offset)
                nv21[pos++] = vBuffer.get()
                uBuffer.position(offset)
                nv21[pos++] = uBuffer.get()
            }
        }

        val processedData = nativeProcessor.processFrame(
            nv21, 
            width, 
            height, 
            edgeDetectionEnabled
        )

        glRenderer.updateTexture(processedData, width, height)

        frameCount++
        val processingTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "Frame processing time: ${processingTime}ms")
    }

    private fun startFpsUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val elapsed = (currentTime - lastFpsTime) / 1000.0
                
                if (elapsed > 0) {
                    currentFps = frameCount / elapsed
                    fpsText.text = String.format("FPS: %.1f", currentFps)
                    
                    frameCount = 0
                    lastFpsTime = currentTime
                }
                
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun updateModeText() {
        modeText.text = if (edgeDetectionEnabled) {
            getString(R.string.mode_edge)
        } else {
            getString(R.string.mode_raw)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.camera_permission_required),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        nativeProcessor.release()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
