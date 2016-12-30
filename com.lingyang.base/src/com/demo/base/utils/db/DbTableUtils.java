package com.lingyang.base.utils.db;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;

import com.lingyang.base.utils.Log;

public class DbTableUtils {
	/**
	 * 创建数据表时 自动增量生成的id
	 */
	public static final String AUTO_CREATED_ID = "_id";

	public static void createTable(SQLiteDatabase db, String name, List<DataColumn> columns) {
		createTable(db, true, name, columns);
	}
	
	public static void createTable(SQLiteDatabase db, 
			boolean isAutoIncrementId, String name, List<DataColumn> columns) {
		if (db == null || columns == null || columns.size() < 1) {
			return;
		}
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS ");
		sql.append(name);
		sql.append("( ");
		if (isAutoIncrementId) {
			sql.append(AUTO_CREATED_ID);
			sql.append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		}
		DataColumn primaryColumn = null;
		for (DataColumn item : columns) {
			if (item != null) {
				sql.append(item.name).append(" ");
				sql.append(item.type);
				if (!item.defCanNull) {
					sql.append(" NOT NULL");
				}
				if (item.defValue != null) {
					sql.append(" DEFAULT ").append(String.valueOf(item.defValue));
				}
				if (item.isPrimary) {
					primaryColumn = item;
				} else
				if(item.unique){
				    sql.append(" UNIQUE ");
				}
				sql.append(",");
			}
		}
		if (!isAutoIncrementId && primaryColumn != null) {
			sql.append("PRIMARY KEY(\"" + primaryColumn.name + "\")");
		} else {
			sql.replace(sql.length() - 1, sql.length(), "");
		}
		sql.append(")");
		
		Log.i("CreateTable", "sql: " + sql);
		db.execSQL(sql.toString());
	}

	public static void dropTable(SQLiteDatabase db, String name) {
		String sql = "DROP TABLE IF EXISTS ";
		db.execSQL(sql + name);
	}

	public static void createUniqueIndex(SQLiteDatabase db, String name, String table, String[] columns) {
		createIndex(db, name, table, columns, true);
	}
	
	public static void createIndex(SQLiteDatabase db, String name, String table, String[] columns) {
		createIndex(db, name, table, columns, false);
	}
	
	private static void createIndex(SQLiteDatabase db, String name, String table, String[] columns, boolean isUnique) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE ");
		if (isUnique) {
			sql.append(" UNIQUE ");
		}
		sql.append(" INDEX IF NOT EXISTS ");
		sql.append(name).append(" ON ");
		sql.append(table).append("(");
		for (String item : columns) {
			if (item != null) {
				sql.append(item).append(",");
			}
		}
		sql.replace(sql.length() - 1, sql.length(), ")");
		db.execSQL(sql.toString());
	}
	

}
