package com.lingyang.base.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.apache.http.conn.util.InetAddressUtils;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class NetWorkUtils {

	public static String TAG = "NetWorkTools";

	/**
	 * 判断是否支持WIFI
	 * 
	 * @param context
	 * @return true表示不支持，false表示不支持
	 */
	public static boolean isNetworkWIFI(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 判断当前网络是否以太网
	 * 
	 * @param context
	 * @return true 是以太网，false不是以太网
	 */
	public static boolean isCurrentNetworkEthernet(Context context) {
		ConnectivityManager cwjManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cwjManager == null) {
			return false;
		}

		NetworkInfo networkInfo = cwjManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable() && networkInfo.getType() == 9 /*
																							 * ConnectivityManager
																							 * .
																							 * TYPE_ETHERNET
																							 */
				&& networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * [wifi是否可用]<br/>
	 * 功能详细描述
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiEnable(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wifiManager != null && wifiManager.isWifiEnabled();
	}

	/**
	 * 判断当前是否有可用的网络,2G网络、3G网络、wifi可使用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager cwjManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cwjManager == null) {
			return false;
		}
		NetworkInfo networkInfo = cwjManager.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}
		return false;
	}

	/**
	 * 判断是否GPRS
	 * 
	 * @param context
	 * @return true表示是GPRS，否则不是GPRS
	 */
	public static boolean isNetworkGprs(Context context) {
		TelephonyManager telMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telMgr == null) {
			return false;
		}
		/*
		 * telMgr.getNetworkCountryIso();//获取电信网络国别
		 * telMgr.getPhoneType();//获得行动通信类型 telMgr.getNetworkType();//获得网络类型
		 */
		// Log.i(TAG, "NetworkType" + telMgr.getNetworkType());
		int networkType = telMgr.getNetworkType();
		if (networkType == TelephonyManager.NETWORK_TYPE_GPRS
				|| networkType == TelephonyManager.NETWORK_TYPE_EDGE) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否3G网络
	 * 
	 * @param context
	 * @return true表示是3G，否则不是3G
	 */
	public static boolean isNetwork3G(Context context) {
		TelephonyManager telMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telMgr == null) {
			return false;
		}
		int networkYype = telMgr.getNetworkType();
		// NETWORK_TYPE_EDGE 是为GPRS到第三代移动通信的过渡性技术方案(GPRS俗称2.5G， EDGE俗称2.75G）
		if (networkYype != TelephonyManager.NETWORK_TYPE_GPRS
				&& networkYype != TelephonyManager.NETWORK_TYPE_UNKNOWN
				&& networkYype != TelephonyManager.NETWORK_TYPE_EDGE) {
			return true;
		}
		return false;
	}

	/**
	 * 判断3G是否可用
	 * 
	 * @param context
	 * @return true 可用， false不可用
	 */
	public static boolean is3GConnected(Context context) {

		if (!isNetwork3G(context)) {
			return false;
		}

		ConnectivityManager connectivity = (ConnectivityManager) context.getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity == null) {
			return false;
		}
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		int netType = info.getType();
		int netSubtype = info.getSubtype();

		if (netType != ConnectivityManager.TYPE_MOBILE) {
			return false;
		}
		switch (netSubtype) {
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return true; // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return true; // ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return true; // ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return true; // ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return true; // ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return true; // ~ 400-7000 kbps
		}

		if (DeviceUtils.getSdkInt() > 7) {
			// NOT AVAILABLE YET IN API LEVEL 7
			switch (netSubtype) {
			case TelephonyManager.NETWORK_TYPE_EHRPD:
				return true; // ~ 1-2 Mbps
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
				return true; // ~ 5 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return true; // ~ 10-20 Mbps
			case TelephonyManager.NETWORK_TYPE_LTE:
				return true; // ~ 10+ Mbps
			}
		}
		return false;
	}

	/**
	 * 判断GPRS是否可用
	 * 
	 * @param context
	 * @return true 可用， false不可用
	 */
	public static boolean isGprsConnected(Context context) {

		if (!isNetworkGprs(context)) {
			return false;
		}

		ConnectivityManager connectivity = (ConnectivityManager) context.getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity == null) {
			return false;
		}
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		int netType = info.getType();
		int netSubtype = info.getSubtype();
		if (netType != ConnectivityManager.TYPE_MOBILE) {
			return false;
		}
		switch (netSubtype) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return true; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return true; // ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return true; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return true; // ~ 100 kbps
		}
		if (DeviceUtils.getSdkInt() > 7 && netSubtype == TelephonyManager.NETWORK_TYPE_IDEN) {
			return true;
		}
		return false;
	}

	/**
	 * 判断Wifi是否可用
	 * 
	 * @param context
	 * @return true 可用， false不可用
	 */
	public static boolean isWifiConnected(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager connectivity = (ConnectivityManager) context.getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity == null) {
			return false;
		}
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		if (info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	/**
	 * 设置数据流量是否可用
	 * 
	 * @param context
	 * @param enable
	 */
	public static void setGprsEnable(Context context, boolean enable) {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			Class<?> conmanClass = Class.forName(connectivityManager.getClass().getName());
			Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			Object iConnectivityManager = iConnectivityManagerField.get(connectivityManager);
			Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass()
					.getName());
			Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod(
					"setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);
			setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);

		} catch (Exception ex) {
			Log.d(TAG, "Exception: " + ex.toString());
		}
	}

	/**
	 * 获取当前已连接的wifi热点的名称
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getCurrConnectWifiSSID(Context ctx) {
		if (ctx == null) {
			return null;
		}
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null) {
			return wifiInfo.getSSID();
		}
		return null;
	}

	/**
	 * 获取当前mac地址
	 * 
	 * @return
	 */
	public static String getLocalMacAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
					.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
						.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());
		}
		return null;
	}

	/***
	 * 使用WIFI时，获取本机IP地址
	 * 
	 * @param mContext
	 * @return
	 */
	public static String getWIFILocalIpAdress(Context mContext) {

		// 获取wifi服务
		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		// 判断wifi是否开启
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ip = formatIpAddress(ipAddress);
		return ip;
	}

	private static String formatIpAddress(int ipAdress) {

		return (ipAdress & 0xFF) + "." + ((ipAdress >> 8) & 0xFF) + "." + ((ipAdress >> 16) & 0xFF)
				+ "." + (ipAdress >> 24 & 0xFF);
	}

	/**
	 * 使用GPRS时，获取本机IP地址
	 * 
	 * @return
	 */
	public static String getGPRSLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
					.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
						.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());
		}
		return null;
	}

	/**
	 * 这个无论是WIFI上网或者GPRS上网，均可使用
	 * 
	 * @return
	 */
	public static String getHostIp() {
		String ipaddress = "";
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			// 遍历所用的网络接口
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
				Enumeration<InetAddress> inet = nif.getInetAddresses();
				// 遍历每一个接口绑定的所有ip
				while (inet.hasMoreElements()) {
					InetAddress ip = inet.nextElement();
					if (!ip.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
						return ip.getHostAddress();
					}
				}

			}
		} catch (SocketException e) {
			Log.e("feige", "获取本地ip地址失败");
			e.printStackTrace();
		}
		return ipaddress;

	}

	public static boolean isCnetWifiAvailable(Context ctx, String ssid) {
		if (ssid == null || ssid.equals("")) {
			return false;
		}
		String currentSSID = NetWorkUtils.getCurrConnectWifiSSID(ctx);
		if (currentSSID == null || currentSSID.equals("")) {
			return false;
		}
		boolean wifiEnable = NetWorkUtils.isWifiEnable(ctx);
		if (!wifiEnable) {
			return false;
		}
		return currentSSID.toLowerCase().equals(ssid.toLowerCase());
	}
}
