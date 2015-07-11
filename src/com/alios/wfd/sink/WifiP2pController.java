
package com.alios.wfd.sink;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class WifiP2pController {

	private static final String TAG = "WFD";

	private final static String P2P_INT = "p2p0";

	private static WifiLock mWifiLock;;
	private static final int DEFAULT_CONTROL_PORT = 7236;
	private static final int MAX_THROUGHPUT = 50;

	public static WifiP2pManager.Channel setWFD(Context context) {
		WifiManager cWifiManager = ((WifiManager) context
		        .getSystemService(Context.WIFI_SERVICE));
		WifiP2pManager cWifiP2pManager = ((WifiP2pManager) context
		        .getSystemService(Context.WIFI_P2P_SERVICE));
		WifiP2pManager.Channel cChannel = cWifiP2pManager.initialize(context,
		        context.getMainLooper(), null);
		// cWifiP2pManager.setMiracastMode(WifiP2pManager.MIRACAST_SINK);
		WifiP2pWfdInfo cWifiP2pWfdInfo = new WifiP2pWfdInfo();
		cWifiP2pWfdInfo.setWfdEnabled(true);
		cWifiP2pWfdInfo.setDeviceType(WifiP2pWfdInfo.PRIMARY_SINK);
		cWifiP2pWfdInfo.setSessionAvailable(true);
		cWifiP2pWfdInfo.setControlPort(DEFAULT_CONTROL_PORT);
		cWifiP2pWfdInfo.setMaxThroughput(MAX_THROUGHPUT);
		cWifiP2pManager.setWFDInfo(cChannel, cWifiP2pWfdInfo, new WFDActionListener("setWFDInfo"));
		cWifiP2pManager.setDeviceName(cChannel, getWFDSinkName(cWifiManager),
		        new WFDActionListener("setDeviceName"));

		return cChannel;
	}

	private static String getWFDSinkName(WifiManager cWifiManager) {
		String macString = cWifiManager.getConnectionInfo().getMacAddress();
		if (TextUtils.isEmpty(macString)) {
			return "";
		} else {
			macString = macString.replaceAll(":", "");
			macString = macString.substring(macString.length() - 4);
			return "Sink-" + macString;
		}
	}

	@SuppressLint("InlinedApi")
	public static final void registerWFDAction(Context context, BroadcastReceiver mWifiP2pReceiver) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		context.registerReceiver(mWifiP2pReceiver, intentFilter);
	}

	public static final void unregisterWFDAction(Context context, BroadcastReceiver mWifiP2pReceiver) {
		context.unregisterReceiver(mWifiP2pReceiver);
	}

	public static class WFDActionListener implements WifiP2pManager.ActionListener {

		private String mName = null;

		public WFDActionListener(String name) {
			mName = name;
		}

		@Override
		public void onFailure(int err) {
			Log.d(TAG, "the func exec failed: " + mName);
		}

		@Override
		public void onSuccess() {
			Log.d(TAG, "the func exec successed: " + mName);
		}

	}

	private static WifiLock getWifiLock(Context context) {
		WifiManager cWifiManager = ((WifiManager) context
		        .getSystemService(Context.WIFI_SERVICE));
		if (null == mWifiLock && null != cWifiManager) {
			mWifiLock = cWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "WFD_WifiLock");
		}
		return mWifiLock;
	}

	public static void acquireWifiLock(Context context) {
		WifiLock mWifiLock = getWifiLock(context);
		if (mWifiLock.isHeld()) {
			return;
		}
		mWifiLock.acquire();
	}

	public static void releaseWifiLock(Context context) {
		WifiLock mWifiLock = getWifiLock(context);
		if (mWifiLock.isHeld()) {
			mWifiLock.release();
		}
	}

	public static boolean hasP2P(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT);
	}

	public static String getAndroidID(Context context) {
		return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	public static boolean isWifiDisplaySource(WifiP2pWfdInfo wfd) {
		if (!wfd.isWfdEnabled()) {
			return false;
		}
		int type = wfd.getDeviceType();
		return (type == WifiP2pWfdInfo.WFD_SOURCE)
		        || (type == WifiP2pWfdInfo.SOURCE_OR_PRIMARY_SINK);
	}

	public static final String toString1(Bundle bundle) {
		if (bundle == null || bundle.isEmpty()) {
			return null;
		}
		StringBuffer result = new StringBuffer();
		Set<String> keys = bundle.keySet();
		for (String key : keys) {
			result.append(key);
			result.append(":");
			result.append(bundle.get(key));
			result.append(", ");
		}
		return result.substring(0, result.length() - 1);
	}

	public static String getIPFromMac(String MAC) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");
				if (splitted != null && splitted.length >= 4) {
					String device = splitted[5];
					if (device.matches(".*" + P2P_INT + ".*")) {
						String mac = splitted[3];
						if (mac.matches(MAC)) {
							return splitted[0];
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getLocalIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
			        .hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
				        .hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					String iface = intf.getName();
					if (iface.matches(".*" + P2P_INT + ".*")) {
						if (inetAddress instanceof Inet4Address) {
							return getDottedDecimalIP(inetAddress.getAddress());
						}
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		} catch (NullPointerException ex) {
			Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		}
		return null;
	}

	private static String getDottedDecimalIP(byte[] ipAddr) {
		String ipAddrStr = "";
		for (int i = 0; i < ipAddr.length; i++) {
			if (i > 0) {
				ipAddrStr += ".";
			}
			ipAddrStr += ipAddr[i] & 0xFF;
		}
		return ipAddrStr;
	}
}
