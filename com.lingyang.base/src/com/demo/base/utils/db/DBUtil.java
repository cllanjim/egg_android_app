package com.lingyang.base.utils.db;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;

import com.lingyang.base.utils.Log;

/**
 * 
 * [数据库操作工具类 ]<br/>
 * 功能详细描述 
 * 维护一个数据库对应的操作对象，
 * 在一定时间内没有操作则自动关闭数据库 
 * 该类封装了一些数据库的基本操作，
 * 对外不公开数据库的连接对象，只公开数据库的操作方法
 */
public class DBUtil {
	public static final String TAG = "DBUtil";
	/**
	 * 超过两分钟没有数据库操作就关闭数据库
	 */
	private static final long CLOSE_DATABASE_DELAY = 2 * 60 * 1000;
	private long mCloseDelayTime = CLOSE_DATABASE_DELAY;

	private SQLiteDatabase mSQLiteDatabase;
	private Handler mHandler;
	private Runnable mCloseDbRunnable;
	private byte[] mLock = new byte[0];
	private SQLiteOpenHelper mOpenHelper;
	private List<OnOperateListener> mOperateListeners;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
	public DBUtil(SQLiteOpenHelper openHelper, Context context) {
		mHandler = new Handler(context.getMainLooper());
		mOpenHelper = openHelper;
		mCloseDbRunnable = new Runnable() {
			@Override
			public void run() {
				closeDatabase();
			}
		};
	}
	
	public void setCloseDelayTime(long time) {
		this.mCloseDelayTime = time;
	}

	public void release() {
		Log.d(TAG, "DbUtil release");
		closeDatabase();
		onRelease();
		mHandler = null;
		mCloseDbRunnable = null;
	}
	
	public void commitDb() {
		Handler handler = mHandler;;
		Runnable closeDbRunnable = mCloseDbRunnable;
		if (closeDbRunnable != null && handler != null) {
			mHandler.removeCallbacks(closeDbRunnable);
		}
		closeDatabase();
	}

	private void closeDatabase() {
		Log.d(TAG, "closeDatabase");
		if (isDatabaseOpen()) {
			new Thread(){
				public void run() {
					Log.d(TAG, "preform closeDatabase");
					if(isDatabaseOpen()){
						mSQLiteDatabase.close();
						mSQLiteDatabase = null;
					}
				};
			}.start();
		}
	}

	private boolean isDatabaseOpen() {
		return mSQLiteDatabase != null && mSQLiteDatabase.isOpen();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	public boolean openDatabase() {
//		Log.e(TAG, " thread: " + Thread.currentThread().getId() + " lock: " + mLock);
		// 打开数据库需要同步
		long t1 = SystemClock.elapsedRealtime();
		synchronized (mLock) {
			if (!isDatabaseOpen()) {
				Log.e(TAG, "mSQLiteDatabase begin: " + Thread.currentThread().getId());
				try {
					mSQLiteDatabase = mOpenHelper.getWritableDatabase();
					Log.e(TAG, "mSQLiteDatabase :" + mSQLiteDatabase + " thread:" + Thread.currentThread().getId());
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Error...: " + e.toString());
				}
				Log.e(TAG, "mSQLiteDatabase end: " + Thread.currentThread().getId());
			}
		}
		if (mSQLiteDatabase != null) {
			if (mHandler != null) {
				mHandler.removeCallbacks(mCloseDbRunnable);
			}
			return true;
		}
		return false;
	}

	public boolean execSQL(String sql) {
		boolean isSuc = false;
		openDatabase();
		try {
			mSQLiteDatabase.execSQL(sql);
			isSuc = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
		return isSuc;
	}

	public boolean execSQL(String sql, Object[] args) {
		boolean isSuc = false;
		openDatabase();
		try {
			mSQLiteDatabase.execSQL(sql, args);
			isSuc = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
		return isSuc;
	}

	public void beginTransaction() {
		openDatabase();
		try {
			mSQLiteDatabase.beginTransaction();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
	}

	public void endTransaction() {
		openDatabase();
		try {
			mSQLiteDatabase.endTransaction();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
	}

	public void setTransactionSuccessful() {
		openDatabase();
		try {
			mSQLiteDatabase.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
	}

	public Cursor rawQuery(String sql, String[] args) {
		openDatabase();
		Cursor c = null;
		try {
			c = mSQLiteDatabase.rawQuery(sql, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
		return c;
	}

	public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		openDatabase();
		Cursor c = null;
		try {
			c = mSQLiteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
		return c;
	}

	public int delete(String table, String whereClause, String[] whereArgs) {
		openDatabase();
		int result = -1;
		try {
			// synchronized (mDbUtil) {
			result = mSQLiteDatabase.delete(table, whereClause, whereArgs);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
		return result;
	}

	public long insert(String table, String nullColumnHack, ContentValues values) {
		openDatabase();
		long result = -1;
		try {
			// synchronized (mDbUtil) {
			result = mSQLiteDatabase.insert(table, nullColumnHack, values);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
		return result;
	}
	
	public long replace(String table, String nullColumnHack, ContentValues values) {
		openDatabase();
		long result = -1;
		try {
			result = mSQLiteDatabase.replace(table, nullColumnHack, values);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
		return result;
	}

	public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		openDatabase();
		int result = -1;
		try {
			// synchronized (mDbUtil) {
			result = mSQLiteDatabase.update(table, values, whereClause, whereArgs);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mHandler != null) {
			mHandler.postDelayed(mCloseDbRunnable, mCloseDelayTime);
		}
		return result;
	}
	
	private synchronized void onRelease() {
		if (mOperateListeners == null) {
			return;
		}
		for(OnOperateListener listener : mOperateListeners) {
			listener.onRelease();
		}
	}
	
	public synchronized void addOperateListener(OnOperateListener listener) {
		if (listener == null) {
			return;
		}
		if (mOperateListeners == null) {
			mOperateListeners = new ArrayList<DBUtil.OnOperateListener>();
		}
		if (mOperateListeners.contains(listener)) {
			return;
		}
		mOperateListeners.add(listener);
	}
	
	public synchronized void removeOperateListener(OnOperateListener listener) {
		if (listener == null || mOperateListeners == null) {
			return;
		}
		mOperateListeners.remove(listener);
	}
	
	public static interface OnOperateListener {
		void onRelease();
	}
}
