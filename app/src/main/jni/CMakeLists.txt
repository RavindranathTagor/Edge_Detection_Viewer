cmake_minimum_required(VERSION 3.22.1)
project("edgedetection")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(OPENCV_DIR ${CMAKE_CURRENT_SOURCE_DIR}/opencv/sdk/native/jni)

if(EXISTS ${OPENCV_DIR}/include/opencv2/opencv.hpp)
    message(STATUS "OpenCV SDK found at ${OPENCV_DIR}")
    
    add_library(opencv_java4 SHARED IMPORTED)
    set_target_properties(opencv_java4 PROPERTIES
        IMPORTED_LOCATION ${OPENCV_DIR}/../libs/${ANDROID_ABI}/libopencv_java4.so)
    
    set(OPENCV_FOUND TRUE)
else()
    message(FATAL_ERROR "OpenCV SDK not found at ${OPENCV_DIR}/include/opencv2/opencv.hpp. Download from: https://github.com/opencv/opencv/releases/download/4.8.0/opencv-4.8.0-android-sdk.zip")
    set(OPENCV_FOUND FALSE)
endif()

add_library(edgedetection SHARED
    native_processor.cpp
    gl_utils.cpp
)

target_include_directories(edgedetection PRIVATE
    ${OPENCV_DIR}/include
)

target_link_libraries(edgedetection
    opencv_java4
    android
    log
    GLESv2
    EGL
)
