LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files, src) 

LOCAL_PACKAGE_NAME := WifiDisplaySink

LOCAL_CERTIFICATE := platform

LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_JNI_SHARED_LIBRARIES := libiomx-gingerbread libiomx-hc libiomx-ics libvlcjni
LOCAL_REQUIRED_MODULES := libswfd

LOCAL_STATIC_JAVA_LIBRARIES := lib-support-v4

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_LIBS := libiomx-gingerbread:libs/armeabi-v7a/libiomx-gingerbread.so \
	libiomx-hc:libs/armeabi-v7a/libiomx-hc.so \
	libiomx-ics:libs/armeabi-v7a/libiomx-ics.so \
	libvlcjni:libs/armeabi-v7a/libvlcjni.so \
	
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	lib-support-v4:libs/android-support-v4.jar \
	
LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
