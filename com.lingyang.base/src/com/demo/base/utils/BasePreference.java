package com.lingyang.base.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * [SharedPreferences基础配置操作类]<br/>
 */
public abstract class BasePreference {
	private SharedPreferences preferences;
	
	public boolean setValue(Context context, int type, String value) {
		if (context == null) {
			return false;
		}
		String key = getKey(type);
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.edit().putString(key, value).commit();
	}
	
	public boolean setValue(Context context, int type, int value) {
		if (context == null) {
			return false;
		}
		String key = getKey(type);
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.edit().putInt(key, value).commit();
	}
	
	public boolean setValue(Context context, int type, boolean value) {
		if (context == null) {
			return false;
		}
		String key = getKey(type);
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.edit().putBoolean(key, value).commit();
	}
	
	public boolean setValue(Context context, int type, long value) {
		if (context == null) {
			return false;
		}
		String key = getKey(type);
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.edit().putLong(key, value).commit();
	}
	
	public int getInt(Context context, int type) {
		return getInt(context, type, 0);
	}
	
	public int getInt(Context context, int type, int defaultValue) {
		if (context == null) {
			return 0;
		}
		String key = getKey(type);
		if (key == null) {
			return 0;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getInt(key, defaultValue);
	}
	
	public String getString(Context context, int type, String def){
		if (context == null) {
			return def;
		}
		String key = getKey(type);
		if (key == null) {
			return def;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getString(key, def);
	}
	
	public boolean getBoolean(Context context, int type) {
		if (context == null) {
			return false;
		}
		String key = getKey(type);
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getBoolean(key, false);
	}

	public boolean getBoolean(Context context, int type, boolean defaultValue) {
		if (context == null) {
			return false;
		}
		String key = getKey(type);
		if (key == null) {
			return false;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getBoolean(key, defaultValue);
	}
	
	public long getLong(Context context, int type) {
		if (context == null) {
			return 0;
		}
		String key = getKey(type);
		if (key == null) {
			return 0;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getLong(key, 0L);
	}
	
	public long getLong(Context context, int type, long defValue) {
		if (context == null) {
			return 0;
		}
		String key = getKey(type);
		if (key == null) {
			return 0;
		}
		SharedPreferences sp = getPreference(context);
		return sp.getLong(key, defValue);
	}
	
	public void beginTransaction(Context context) {
		preferences = context.getSharedPreferences(getPreferenceName(), 0);
	}

	public void endTrancation() {
		preferences = null;
	}

	protected SharedPreferences getPreference(Context context) {
		if (context == null) {
			return null;
		}
		SharedPreferences sp = null;
		if (preferences != null) {
			sp = preferences;
		} else {
			sp = context.getSharedPreferences(getPreferenceName(), 0);
		}
		return sp;
	}
	
	protected abstract String getPreferenceName();
	protected abstract String getKey(int type);
}
