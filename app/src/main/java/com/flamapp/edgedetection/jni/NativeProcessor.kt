package com.flamapp.edgedetection.jni

class NativeProcessor {
    
    private var nativeHandle: Long = 0

    init {
        System.loadLibrary("edgedetection")
        nativeHandle = nativeInit()
    }

    fun release() {
        if (nativeHandle != 0L) {
            nativeRelease(nativeHandle)
            nativeHandle = 0
        }
    }

    private external fun nativeInit(): Long
    private external fun nativeRelease(handle: Long)
    external fun processFrame(
        inputData: ByteArray,
        width: Int,
        height: Int,
        applyEdgeDetection: Boolean
    ): ByteArray
}
