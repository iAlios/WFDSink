/*
 * Copyright 2012, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//#define LOG_NDEBUG 0
#define LOG_TAG "wfd"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include <utils/Log.h>

#include "android_runtime/android_view_Surface.h"
#include "sink/WifiDisplaySink.h"

#ifndef LOGD
#define LOGD(...) ALOGD( __VA_ARGS__)
#endif

#ifndef LOGE
#define LOGE(...) ALOGE( __VA_ARGS__)
#endif

#ifndef LOGW
#define LOGW(...) ALOGW( __VA_ARGS__)
#endif

#ifndef LOGI
#define LOGI(...) ALOGI( __VA_ARGS__)
#endif

#ifndef LOGV
#define LOGV(...) ALOGV( __VA_ARGS__)
#endif

#ifndef LOGE_IF
#define LOGE_IF(...) ALOGE_IF( __VA_ARGS__)
#endif

#ifndef LOGV_IF
#define LOGV_IF(...) ALOGV_IF( __VA_ARGS__)
#endif

#define LOG_TAG "WFD_CONTROL"

using namespace android;

static sp<IGraphicBufferProducer> mGraphicBufferProducer;

static sp < AXNetworkSession > mNetworkSession;

static sp < ALooper > mLooper;

static int start_sink(JNIEnv *env, jobject clazz, jstring ip_addr, int port) {
	const char* connectToHost = env->GetStringUTFChars(ip_addr, NULL);
	if (connectToHost == NULL) {
		return -1;
	}

	sp < AXNetworkSession > session = new AXNetworkSession;
	session->start();

	sp < ALooper > looper = new ALooper;

	sp < WifiDisplaySink > sink = new WifiDisplaySink(session, mGraphicBufferProducer);
	looper->registerHandler(sink);

	sink->start(connectToHost, port);

	looper->start(true /* runOnCallingThread */);

	env->ReleaseStringUTFChars(ip_addr, connectToHost);

	mNetworkSession = session;
	mLooper = looper;

	LOGW("start sink success");
	return 1;
}

static int stop_sink(JNIEnv *env, jobject clazz, int port) {
	// TODO
	if (mNetworkSession == NULL) {
		mNetworkSession->stop();
	}

	if (mLooper == NULL) {
		mLooper->stop();
	}
	LOGW("stop sink success");

	return 1;
}

static void setVideoSurface(JNIEnv *env, jobject thiz, jobject jsurface)
{
    sp<IGraphicBufferProducer> new_st;
    if (jsurface) {
        sp<Surface> surface(android_view_Surface_getSurface(env, jsurface));
        if (surface != NULL) {
            new_st = surface->getIGraphicBufferProducer();
            if (new_st == NULL) {
                jniThrowException(env, "java/lang/IllegalArgumentException",
                    "The surface does not have a binding SurfaceTexture!");
                return;
            }
        } else {
            jniThrowException(env, "java/lang/IllegalArgumentException",
                    "The surface has been released");
            return;
        }
    }
    mGraphicBufferProducer = new_st;
}

static JNINativeMethod method_table[] = {
		{ "startSink", "(Ljava/lang/String;I)I", (void*) start_sink },
	    { "setVideoSurface",    "(Landroid/view/Surface;)V",        (void *)setVideoSurface},
		{ "stopSink", "(I)I", (void*) stop_sink }
};

static const char* classPathName = "com/alios/wfd/sink/WFDHelper";

static int register_method(JNIEnv *env) {
	LOGW("register_method === before ===");
	jclass clazz;
	clazz = env->FindClass(classPathName);
	if (clazz == NULL) {
		return -1;
	}
	LOGW("register_method === after ===");
	return AndroidRuntime::registerNativeMethods(env, classPathName,
			method_table, NELEM(method_table));
}

jint JNI_OnLoad(JavaVM* vm,void* reserved) {
    LOGW("JNI_OnLoad");
    JNIEnv* env = NULL;
    jint result = -1;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    LOGW("register mothod");
    assert(env != NULL);
    if (register_method(env) < 0) {
        goto bail;
    }
    return JNI_VERSION_1_4;
bail:
    return result;
}
