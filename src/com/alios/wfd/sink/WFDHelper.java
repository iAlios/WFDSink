
package com.alios.wfd.sink;

import android.view.Surface;

public class WFDHelper {

	public static final String TAG = "WFDHelper";

	static {
		System.loadLibrary("swfd");
	}

	public static native int startSink(String ip, int port);

	public static native int stopSink(int code);

	public static native void setVideoSurface(Surface surface);
}
