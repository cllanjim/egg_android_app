package com.lingyang.camera.framework;

import java.util.WeakHashMap;

public class MgmtClassFactory {

	private static MgmtClassFactory mInstance = null;

	public static MgmtClassFactory getInstance() {
		if (mInstance == null)
			mInstance = new MgmtClassFactory();
		return mInstance;
	}

	private MgmtClassFactory() {
	}

	private WeakHashMap<String, Object> mProxyInstanceMap = new WeakHashMap<String, Object>();

	public void reloadProxyInstanceMap() {
		mProxyInstanceMap = new WeakHashMap<String, Object>();
	}

	// 取得当前对象
	public Object getMgmtClass(Class<?> classType) {

		Object obj = mProxyInstanceMap.get(classType.toString());
		if (obj == null) {
			try {
				obj = classType.newInstance();
				mProxyInstanceMap.put(classType.toString(), obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return obj;
	}
}
