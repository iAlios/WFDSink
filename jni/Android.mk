LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
        AXNetworkSession.cpp             \
        Parameters.cpp                  \
        ParsedMessage.cpp               \
        sink/LinearRegression.cpp       \
        sink/RTPSink.cpp                \
        sink/WifiDisplaySink.cpp        \
        sink/TunnelRenderer.cpp        \
        foundation/AAtomizer.cpp        \
        foundation/ABitReader.cpp        \
        foundation/ABuffer.cpp        \
        foundation/AHandler.cpp        \
        foundation/AHierarchicalStateMachine.cpp        \
        foundation/ALooper.cpp        \
        foundation/ALooperRoster.cpp        \
        foundation/AMessage.cpp        \
        foundation/AString.cpp        \
        foundation/base64.cpp        \
        foundation/hexdump.cpp        \
        TimeSeries.cpp                  \
        Utils.cpp                  \
        ESDS.cpp                  \
        MetaData.cpp                  \
        wfd.cpp                

LOCAL_C_INCLUDES:= \
	    $(JNI_H_INCLUDE) \
	    $(TOP)/frameworks/native/include \
	    $(TOP)/frameworks/base/include \
	    $(LOCAL_PATH)/include/media/stagefright/foundation 

LOCAL_SHARED_LIBRARIES:= \
        libbinder                       \
        libcutils                       \
        libgui                          \
        libmedia                        \
        libandroid_runtime              \
    	libnativehelper                 \
        libui                           \
        libutils
        
LOCAL_MODULE:= libswfd

LOCAL_MODULE_TAGS:= optional

include $(BUILD_SHARED_LIBRARY)
