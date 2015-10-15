
package com.alios.wfd.sink.demo;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.alios.wfd.sink.WFDHelper;
import com.alios.wfd.sink.WifiP2pController;
import com.alios.wfd.sink.apitest.R;

public class WFDSinkActivity extends Activity {

	private static final String TAG = "WFD";

	private static WifiP2pManager.Channel mChannel;

	private WFDBroadcastReceiver mWFDBroadcastReceiver = new WFDBroadcastReceiver();

	private Handler mHandler = new Handler();

	private SurfaceHolder mSurfaceHolder;

	private SurfaceView mSurfaceView;

	private AtomicBoolean isEnable = new AtomicBoolean();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sink_main);
		mChannel = WifiP2pController.setWFD(this);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
			        int height) {
				WFDHelper.setVideoSurface(mSurfaceHolder.getSurface());
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		WifiP2pController.registerWFDAction(this, mWFDBroadcastReceiver);
	}

	@Override
	public void onPause() {
		super.onPause();
		WifiP2pController.unregisterWFDAction(this, mWFDBroadcastReceiver);
		WifiP2pController.releaseWifiLock(this);
	}

	public void onClickCreateWFD(View view) {
		discoverPeers();
		isEnable.set(true);
	}

	public void onClickRemoveWFD(View view) {
		isEnable.set(false);
		cancelConnect();
		stopDiscoverPeers();
	}

	protected void requestPeers() {
		WifiP2pManager cWifiP2pManager = ((WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE));
		cWifiP2pManager.requestPeers(mChannel, new PeerListListener() {

			@Override
			public void onPeersAvailable(WifiP2pDeviceList cWifiP2pDeviceList) {
				Log.d(TAG, " ==onPeersAvailable== " + cWifiP2pDeviceList);
			}
		});
	}

	private void discoverPeers() {
		WifiP2pManager cWifiP2pManager = ((WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE));
		cWifiP2pManager.discoverPeers(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				Log.d(TAG, " discoverPeers is onSuccess");
			}

			@Override
			public void onFailure(int err) {
				Log.d(TAG, " discoverPeers is " + err);
			}
		});
		WifiP2pController.acquireWifiLock(this);
	}

	private void stopDiscoverPeers() {
		WifiP2pManager cWifiP2pManager = ((WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE));
		cWifiP2pManager.stopPeerDiscovery(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				Log.d(TAG, " stopPeerDiscovery is onSuccess");
			}

			@Override
			public void onFailure(int err) {
				Log.d(TAG, " stopPeerDiscovery is " + err);
			}
		});
		WifiP2pController.releaseWifiLock(this);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			}
		});
	}

	private void cancelConnect() {
		WifiP2pManager cWifiP2pManager = ((WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE));
		cWifiP2pManager.cancelConnect(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				Log.d(TAG, " cancelConnect is onSuccess");
			}

			@Override
			public void onFailure(int err) {
				Log.d(TAG, " cancelConnect is " + err);
			}
		});
	}

	protected void startSink(final String ip, final String mac, final int cPort) {
		WifiP2pManager cWifiP2pManager = ((WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE));
		cWifiP2pManager.requestGroupInfo(mChannel, new GroupInfoListener() {

			@Override
			public void onGroupInfoAvailable(WifiP2pGroup group) {
				if (group == null) {
					Log.d(TAG, " WifiP2pGroup is NULL!");
					return;
				}
				Log.d(TAG, " WifiP2pGroup is " + group);
				// String passwd = null;
				int mP2pControlPort = cPort;
				if (group.isGroupOwner()) {
					// passwd = group.getPassphrase();
				}
				Collection<WifiP2pDevice> p2pdevs = group.getClientList();
				String sIP = ip;
				for (WifiP2pDevice dev : p2pdevs) {
					if (dev == null || dev.wfdInfo == null) {
						continue;
					}
					if (WifiP2pController.isWifiDisplaySource(dev.wfdInfo) == false) {
						continue;
					}
					mP2pControlPort = dev.wfdInfo.getControlPort();
					sIP = WifiP2pController.getIPFromMac(mac);
					break;
				}

				if (mP2pControlPort <= 0) {
					mP2pControlPort = 7236;
				}
				startVLC(sIP, mac, mP2pControlPort, 1000);
			}

		});
	}

	private void agreeStartSink(final String mac, final int cPort) {
		Log.d(TAG, " =======agreeStartSink========= " + cPort);
		WifiP2pManager cWifiP2pManager = ((WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE));
		cWifiP2pManager.requestConnectionInfo(mChannel, new ConnectionInfoListener() {
			public void onConnectionInfoAvailable(WifiP2pInfo info) {
				Log.d(TAG, " ======== onConnectionInfoAvailable(): ======== " + info);
				Log.d(TAG, " Sink is the P2P GO with IP ======" + mac + "======= ");
				startVLC(null, mac, cPort, 1000);
			}

		});
	}

	private void startVLC(final String sIP, final String mac, final int cPort, final long delay) {
		Log.d(TAG, "startVLC ======" + sIP + "===" + mac + "==== " + cPort);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (!isEnable.get()) {
					return;
				}
				String sip = sIP;
				if (TextUtils.isEmpty(sip)) {
					sip = WifiP2pController.getIPFromMac(mac);
					if (TextUtils.isEmpty(sip)) {
						startVLC(sip, mac, cPort, delay);
						return;
					}
				}
				startVLC(sip, cPort);
			}

		}, delay);
	}

	private void startVLC(final String sIP, final int cPort) {
		Log.d(TAG, " === startVLC === ip:" + sIP + "  port: " + cPort);
		Log.d(TAG,
		        " === some android phone made by Samsung model with Exynos SOC, can use by VideoView with build-in wfd protocol to play with wfd://"
		                + sIP + ":" + cPort);
		new Thread() {

			@Override
			public void run() {
				WFDHelper.startSink(sIP, cPort);
			}

		}.start();
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			}
		});
	}

	public class WFDBroadcastReceiver extends BroadcastReceiver {

		@SuppressLint("InlinedApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
				int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
				String sttStr;
				switch (state) {
					case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
						sttStr = "ENABLED";
						break;
					case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
						sttStr = "DISABLED";
						break;
					default:
						sttStr = "UNKNOWN";
						break;
				}
				// 初期化
				Log.d(TAG,
				        "action: === " + action + " , Extras: === "
				                + WifiP2pController.toString1(intent.getExtras())
				                + " cur Wifi state: " + sttStr
				        );
			} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
				NetworkInfo networkInfo = (NetworkInfo) intent
				        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
				if (networkInfo != null && networkInfo.isConnected()) {
					agreeStartSink(null, -1);
				}
			} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
				Log.d(TAG,
				        "action: === " + action + " , Extras: === "
				                + WifiP2pController.toString1(intent.getExtras())
				        );
				NetworkInfo networkInfo = (NetworkInfo) intent
				        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
				WifiP2pGroup cWifiP2pGroup = intent
				        .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
				if (networkInfo != null && networkInfo.isConnected()) {
					if (cWifiP2pGroup != null) {
						Collection<WifiP2pDevice> cWifiP2pDevices = cWifiP2pGroup.getClientList();
						WifiP2pDevice cWifiP2pDevice = null;
						for (WifiP2pDevice cDevice : cWifiP2pDevices) {
							if (cDevice.wfdInfo != null && cDevice.wfdInfo.isWfdEnabled()) {
								cWifiP2pDevice = cDevice;
							}
						}
						if (cWifiP2pDevice != null) {
							agreeStartSink(cWifiP2pDevice.deviceAddress,
							        cWifiP2pDevice.wfdInfo.getControlPort());
						}
					}
				} else {
					if (isEnable.get()) {
						discoverPeers();
					}
				}
			} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
				// NNNN
			} else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
				// NNNN
			} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				// NNNN
			}
		}
	}

}
