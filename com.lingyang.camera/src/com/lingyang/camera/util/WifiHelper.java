package com.lingyang.camera.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

public class WifiHelper {
	private WifiManager mWifiManager;
	private ConnectivityManager mConnectivityManager;

	public WifiHelper(Context context) {
		mWifiManager = (WifiManager) context.getSystemService(
				Context.WIFI_SERVICE);
		mConnectivityManager = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public boolean connectTo(String ssid, String key) {
		disconnect();
		WifiConfiguration config = CreateWifiInfo(ssid, key);
		return connect(config);
	}

	public boolean connect(WifiConfiguration config) {
		ensureWifiState();
		int netId = -1;
		List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
		for (WifiConfiguration c : configs) {
			if (config.SSID.equals(c.SSID)) {
				mWifiManager.removeNetwork(c.networkId);
				break;
			}
		}
		netId = mWifiManager.addNetwork(config);
		mWifiManager.disconnect();
		if (!mWifiManager.enableNetwork(netId, true)) {
			return false;
		}
		return mWifiManager.reconnect();
	}

	public void disconnect() {
		mWifiManager.disconnect();
	}

	public WifiConfiguration CreateWifiInfo(String ssid, String key) {
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + ssid + "\"";
		config.preSharedKey = "\"" + key + "\"";
		return config;
	}

	/**
	 * Ensure Wi-Fi is on
	 * @return true if on
	 */
	public boolean ensureWifiState() {
		if (!mWifiManager.isWifiEnabled()) {
			return mWifiManager.setWifiEnabled(true);
		}
		return true;
	}

	/**
	 * Get current connected SSID
	 * @return null if no connection
	 */
	public WifiInfo getConnectedInfo() {
		if (mWifiManager.isWifiEnabled() && getNetworkState() == State.CONNECTED) {	
			return mWifiManager.getConnectionInfo();
		}
		return null;
	}

	public WifiConfiguration getConnectedConfig() {
		if (mWifiManager.isWifiEnabled() && getNetworkState() == State.CONNECTED) {
			int id = mWifiManager.getConnectionInfo().getNetworkId();
			return mWifiManager.getConfiguredNetworks().get(id);
		}
		return null;
	}

	public int getWifiState() {
		return mWifiManager.getWifiState();
	}

	public boolean isConnected(String ssid) {
		String newSSID = mWifiManager.getConnectionInfo().getSSID();
		return newSSID == null ? false : newSSID.contains(ssid);
	}

	public State getNetworkState() {
		return mConnectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();
	}

	public boolean isNetworkConnected() {
		return mConnectivityManager.getActiveNetworkInfo().isConnected();
	}
	
	public boolean startScan() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
		return mWifiManager.startScan();
	}

	public List<ScanResult> getScanResults() {
		return mWifiManager.getScanResults();
	}
}
