package com.lingyang.camera.util;

import android.content.Context;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import com.lingyang.base.utils.CLog;

import java.util.ArrayList;
import java.util.List;

public class WifiAdmin {

	private WifiManager mWifiManager;
	private WifiInfo mWifiInfo;
	private List<ScanResult> mWifiList;
	private List<WifiConfiguration> mWifiConfiguration;
	WifiLock mWifiLock;
	private int NetID;

	List<WifiConfiguration> mConfiguredNets;

	public WifiAdmin(Context context) {
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	public int checkState() {
		return mWifiManager.getWifiState();
	}

	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	public void releaseWifiLock() {
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	public void createWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}

	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfiguration;
	}

	public void connectConfiguration(int index) {
		if (index > mWifiConfiguration.size()) {
			return;
		}
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
	}

	public void startScan() {
		mWifiManager.startScan();
		mWifiList = mWifiManager.getScanResults();
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}

	public List<ScanResult> getWifiList() {
		return mWifiList;
	}

	public StringBuilder lookUpScan() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder.append("Index_" + new Integer(i + 1).toString() + ":");
			stringBuilder.append((mWifiList.get(i)).toString());
			stringBuilder.append("/n");
		}
		return stringBuilder;
	}

	public String getSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	// public void addNetwork(WifiConfiguration wcg) {
	// int wcgID = mWifiManager.addNetwork(wcg);
	// boolean b = mWifiManager.enableNetwork(wcgID, true);
	// System.out.println("a--" + wcgID);
	// System.out.println("b--" + b);
	// }

	public int getNetID() {
		return mWifiInfo.getNetworkId();
	}

	public boolean addNetwork(WifiConfiguration wcg) {
		NetID = mWifiManager.addNetwork(wcg);
		mWifiManager.disconnect();
		boolean b = mWifiManager.enableNetwork(NetID, true);
		// boolean result = mWifiManager.reassociate();
		mWifiManager.reconnect();
		mConfiguredNets = new ArrayList<WifiConfiguration>();

		return b;
	}

	public boolean checkNet() {
		final State mNetworkState = State.UNKNOWN;
		if (mNetworkState != State.CONNECTED) {
			CLog.e("ming:" + "Problems connecting to desired network!");
			return false;
		} else {
			CLog.e("ming:" + "Successfully connected to desired network!");
			return true;
		}
	}

	public void disconnectWifi() {
		mWifiManager.disconnect();
	}

	/*
	 * public void disconnectWifi(int netId) {
	 * mWifiManager.disableNetwork(netId); mWifiManager.disconnect(); }
	 */

	public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = this.IsExsits(SSID);
		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}

		// wifi type
		// "WPA2", "WPA", "WEP", "NONE"
		// 3 , 2 , 1 , 0

		switch (Type) {
		case 0:
			// WIFICIPHER_WPA2
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.status = Status.ENABLED;
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			break;
		// case 2:
		// // WIFICIPHER_WPA
		// config.preSharedKey = "\"" + Password + "\"";
		// config.hiddenSSID = true;
		// config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		// config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		// config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		// config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		// // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		// config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		// config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		// config.status = WifiConfiguration.Status.ENABLED;
		// break;
		case 1:
			// WIFICIPHER_WEP
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
			break;
		case -1:
			// WIFICIPHER_NOPASS
			// config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
			break;
		default:
			break;
		}
		return config;
	}

	public WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		if (existingConfigs != null) {
			for (WifiConfiguration existingConfig : existingConfigs) {
				if (existingConfig != null && existingConfig.SSID != null
						&& existingConfig.SSID.equals("\"" + SSID + "\"")) {
					return existingConfig;
				}
			}
		}
		return null;
	}

	public WifiInfo getmWifiInfo() {
		return mWifiInfo;
	}

	public void setmWifiInfo(WifiInfo mWifiInfo) {
		this.mWifiInfo = mWifiInfo;
	}

	public boolean pingSupplicant() {
		return mWifiManager.pingSupplicant();
	}
}