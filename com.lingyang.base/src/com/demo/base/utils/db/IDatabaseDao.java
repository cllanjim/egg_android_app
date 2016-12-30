package com.lingyang.base.utils.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库管理的接口
 * 实现该接口的类需要提供一个空的构造方法
 * 用于反射调用，否则无法正常创建数据库
 */
public interface IDatabaseDao {
	
	/**
	 * 创建数据库的时候调用
	 * @param db
	 */
	void createDao(SQLiteDatabase db);

	/**
	 * 更新数据库的时候调用
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 */
	void upgradeDao(SQLiteDatabase db, int oldVersion, int newVersion);
}
