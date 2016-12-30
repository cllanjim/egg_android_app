package com.lingyang.base.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SimplePreference {
	private static String sPreferenceName = "basePreference";
	private static SharedPreferences preferences;
	
	/**
	 * 如需更改Preference名字，建议在Application.onCreate中更改
	 * @param name
	 */
	public static void setPreferenceName(String name){
		if (name == null || name.length() == 0) {
			return;
		}
		sPreferenceName = name;
	}

	public static void beginTransaction(Context context) {
		preferences = context.getSharedPreferences(sPreferenceName, 0);
	}

	public static void endTrancation() {
		preferences = null;
	}

	private static SharedPreferences getPreference(Context context) {
		SharedPreferences sp = null;
		if (preferences != null) {
			sp = preferences;
		} else {
			sp = context.getSharedPreferences(sPreferenceName, 0);
		}
		return sp;
	}

	public static boolean setString(Context context, String key, String value) {
		if (context == null) {
			return false;
		}
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.edit().putString(key, value).commit();
	}

	public static boolean setInt(Context context, String key, int value) {
		if (context == null) {
			return false;
		}
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.edit().putInt(key, value).commit();
	}

	public static boolean setBoolean(Context context, String key, boolean value) {
		if (context == null) {
			return false;
		}
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.edit().putBoolean(key, value).commit();
	}

	public static boolean setFloat(Context context, String key, float value) {
		if (context == null) {
			return false;
		}
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.edit().putFloat(key, value).commit();
	}

	public static boolean setLong(Context context, String key, long value) {
		if (context == null) {
			return false;
		}
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.edit().putLong(key, value).commit();
	}

	public static long getLong(Context context, String key, long defaultValue) {
		if (context == null) {
			return defaultValue;
		}
		if (key == null) {
			return defaultValue;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getLong(key, defaultValue);
	}

	public static float getFloat(Context context, String key, float defaultValue) {
		if (context == null) {
			return defaultValue;
		}
		if (key == null) {
			return defaultValue;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getFloat(key, defaultValue);
	}

	public static int getInt(Context context, String key, int defaultValue) {
		if (context == null) {
			return defaultValue;
		}
		if (key == null) {
			return defaultValue;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getInt(key, defaultValue);
	}

	public static String getString(Context context, String key, String def) {
		if (context == null) {
			return def;
		}
		if (key == null) {
			return def;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getString(key, def);
	}

	public static boolean getBoolean(Context context, String key, boolean defaultValue) {
		if (context == null) {
			return false;
		}
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getBoolean(key, defaultValue);
	}
}
