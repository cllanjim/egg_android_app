package com.lingyang.base.utils.db;

import java.util.HashSet;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.db.DBUtil.OnOperateListener;

/**
 * 基础的数据帮助类</br> 
 * 包含了注册数据库Dao的方法</br> 
 * 在需要创建和更新数据的时候调用Dao的方法进行创建和调用</br>
 */
public abstract class BasicSQLiteHelper extends SQLiteOpenHelper {
	private static final String TAG = "BasicSQLiteHelper";
	
	private DBUtil mDbUtil;
	
	private HashSet<Class<? extends IDatabaseDao>> mDaoClasssSet;

	public BasicSQLiteHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		mDaoClasssSet = getTableDaoClass();
		mDbUtil = new DBUtil(this, context);
	}

	protected abstract HashSet<Class<? extends IDatabaseDao>> getTableDaoClass();

	@Override
	public void onCreate(SQLiteDatabase db) {
		Iterator<Class<? extends IDatabaseDao>> it = mDaoClasssSet.iterator();
		while (it.hasNext()) {
			Class<? extends IDatabaseDao> cls = it.next();
			try {
				IDatabaseDao dao = cls.newInstance();
				dao.createDao(db);
				Log.d(TAG, "createDao: " + cls);
			} catch (Exception e) {
				Log.e(TAG, "createDao: " + e.toString());
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Iterator<Class<? extends IDatabaseDao>> it = mDaoClasssSet.iterator();
		while (it.hasNext()) {
			Class<? extends IDatabaseDao> cls = it.next();
			try {
				IDatabaseDao dao = cls.newInstance();
				dao.upgradeDao(db, oldVersion, newVersion);
				Log.d(TAG, "upgradeDao: " + cls);
			} catch (Exception e) {
				Log.e(TAG, "createDao: " + e.toString());
			}
		}
	}
	
	public boolean execSQL(String sql) {
		return mDbUtil.execSQL(sql);
	}
	
	public boolean execSQL(String sql, Object[] args) {
		return mDbUtil.execSQL(sql, args);
	}
	
	public void beginTransaction() {
		mDbUtil.beginTransaction();
	}
	
	public void endTransaction() {
		mDbUtil.endTransaction();
	}
	
	public void setTransactionSuccessful() {
		mDbUtil.setTransactionSuccessful();
	}
	
	public Cursor rawQuery(String sql, String[] args) {
		return mDbUtil.rawQuery(sql, args);
	}

	public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		return mDbUtil.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
	}
	
	public int delete(String table, String whereClause, String[] whereArgs) {
		return mDbUtil.delete(table, whereClause, whereArgs);
	}
	
	public long insert(String table, String nullColumnHack, ContentValues values) {
		return mDbUtil.insert(table, nullColumnHack, values);
	}
	
	public long replace(String table, String nullColumnHack, ContentValues values) {
		return mDbUtil.replace(table, nullColumnHack, values);
	}
	
	public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		return mDbUtil.update(table, values, whereClause, whereArgs);
	}
	
	public synchronized void addOperateListener(OnOperateListener listener) {
		mDbUtil.addOperateListener(listener);
	}
	
	public synchronized void removeOperateListener(OnOperateListener listener) {
		mDbUtil.removeOperateListener(listener);
	}
	
	public void release() {
		mDbUtil.release();
	}
}
