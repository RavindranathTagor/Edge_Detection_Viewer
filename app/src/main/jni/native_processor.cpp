#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "NativeProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_com_flamapp_edgedetection_jni_NativeProcessor_processFrame(
    JNIEnv* env,
    jobject,
    jbyteArray inputData,
    jint width,
    jint height,
    jboolean applyEdgeDetection
) {
    jbyte* input = env->GetByteArrayElements(inputData, nullptr);
    jsize inputLength = env->GetArrayLength(inputData);

    // NV21 format: Y plane followed by interleaved VU
    cv::Mat yuvMat(height + height / 2, width, CV_8UC1, (unsigned char*)input);
    cv::Mat rgbMat;
    cv::cvtColor(yuvMat, rgbMat, cv::COLOR_YUV2RGB_NV21);
    
    // Rotate 90 degrees clockwise to fix orientation
    cv::Mat rotated;
    cv::rotate(rgbMat, rotated, cv::ROTATE_90_CLOCKWISE);
    rgbMat = rotated;

    cv::Mat output;
    
    if (applyEdgeDetection) {
        cv::Mat gray;
        cv::cvtColor(rgbMat, gray, cv::COLOR_RGB2GRAY);
        cv::GaussianBlur(gray, gray, cv::Size(5, 5), 1.4);
        cv::Canny(gray, output, 50, 150);
        cv::cvtColor(output, output, cv::COLOR_GRAY2RGB);
    } else {
        output = rgbMat;
    }

    int outputSize = output.total() * output.elemSize();
    jbyteArray result = env->NewByteArray(outputSize);
    env->SetByteArrayRegion(result, 0, outputSize, (jbyte*)output.data);

    env->ReleaseByteArrayElements(inputData, input, JNI_ABORT);

    return result;
}

JNIEXPORT jlong JNICALL
Java_com_flamapp_edgedetection_jni_NativeProcessor_nativeInit(JNIEnv* env, jobject) {
    LOGI("Native processor initialized");
    return 0;
}

JNIEXPORT void JNICALL
Java_com_flamapp_edgedetection_jni_NativeProcessor_nativeRelease(JNIEnv* env, jobject, jlong handle) {
    LOGI("Native processor released");
}

}
