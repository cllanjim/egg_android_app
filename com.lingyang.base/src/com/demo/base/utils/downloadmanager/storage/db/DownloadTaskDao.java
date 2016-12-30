package com.lingyang.base.utils.downloadmanager.storage.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lingyang.base.utils.Log;
import com.lingyang.base.utils.db.BasicSQLiteHelper;
import com.lingyang.base.utils.db.DataColumn;
import com.lingyang.base.utils.db.DbTableUtils;
import com.lingyang.base.utils.db.IDatabaseDao;
import com.lingyang.base.utils.db.DataColumn.DataType;
import com.lingyang.base.utils.downloadmanager.KeyConstants;
import com.lingyang.base.utils.downloadmanager.DownloadConfiguration.DownloadTable;
import com.lingyang.base.utils.downloadmanager.storage.DownloadBean;

/**
 * [下载任务DAO]
 * 可能存在操作多个Table的情况，操作db的时候需要指定具体的类
 * 使用SDK前需要提前指定会有哪些Table
 * 
 */
public class DownloadTaskDao implements IDatabaseDao{
	public static final String TAG = "DownloadTaskDao";

	private final String T_DOWNLOAD_ID = "downloadId";
	private final String T_DOWNLOAD_URL = "url";
	private final String T_POST_PARAM = "postParam";
	private final String T_HTTP_METHOD = "httpMethod";
	private final String T_SAVE_PATH = "path";
	private final String T_SAVE_FOLDER = "folder";
	private final String T_FILE_NAME = "fileName";
	private final String T_FILE_SIZE = "fileSize";
	private final String T_DOWNLOAD_SIZE = "downloadSize";
	private final String T_CREATE_TIME = "createTime";
	private final String T_OWNER = "owner";
	private final String T_IS_PRIV = "isPriv";
	private final String T_DOWNLOAD_STATE = "state";
	private final String T_DOWNLOAD_RETRY_COUNT = "retryCount";
	private final String T_DOWNLOAD_CONNECT_TIMEOUT = "connectTimeOut";
	private final String T_DOWNLOAD_READ_TIMEOUT = "readTimeOut";
	private final String T_HTTP_HEAD = "httpHead";
	private final String T_DOWNLOAD_TYPE = "type";
	private final String T_FILE_VERSION = "version";

	private BasicSQLiteHelper mDbEntity;
	
	protected String databaseTable;

	public DownloadTaskDao(Context context, String datableTable) {
		mDbEntity = DownloadDatabaseHelper.getInstance(context);
		this.databaseTable = datableTable;
	}
	
	/**
	 * [该空构造用于反射初始化]
	 */
	public DownloadTaskDao() {}
	
	public boolean save(List<DownloadBean> list) {
		if (list == null || list.size() <= 0) {
			return false;
		}

		boolean result = false;
		try {
			mDbEntity.beginTransaction();
			for (DownloadBean item : list) {
				save(item);
			}
			mDbEntity.setTransactionSuccessful();
			mDbEntity.endTransaction();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public long save(DownloadBean downloadBean) {
		if (downloadBean == null) {
			return -1;
		}
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(T_DOWNLOAD_ID, downloadBean.downloadId);
		initialValues.put(T_DOWNLOAD_URL, downloadBean.url);
		initialValues.put(T_POST_PARAM, downloadBean.postParam);
		initialValues.put(T_HTTP_METHOD, downloadBean.httpMethod);
		initialValues.put(T_SAVE_PATH, downloadBean.path);
		initialValues.put(T_SAVE_FOLDER, downloadBean.folder);
		initialValues.put(T_FILE_NAME, downloadBean.fileName);
		initialValues.put(T_FILE_SIZE, downloadBean.filelen);
		initialValues.put(T_DOWNLOAD_SIZE, downloadBean.downloadLen);
		initialValues.put(T_CREATE_TIME, downloadBean.createTime);
		initialValues.put(T_OWNER, downloadBean.owner);
		initialValues.put(T_IS_PRIV, downloadBean.isPriv);
		initialValues.put(T_DOWNLOAD_STATE, downloadBean.downloadState);
		initialValues.put(T_DOWNLOAD_RETRY_COUNT, downloadBean.retryCount);
		initialValues.put(T_DOWNLOAD_CONNECT_TIMEOUT, downloadBean.connectionTimeout);
		initialValues.put(T_DOWNLOAD_READ_TIMEOUT, downloadBean.readTimeout);
		initialValues.put(T_HTTP_HEAD, downloadBean.httpHead);
		initialValues.put(T_DOWNLOAD_TYPE, downloadBean.fileType);
		initialValues.put(T_FILE_VERSION, downloadBean.fileVersion);
		long result = mDbEntity.insert(databaseTable, null, initialValues);
		return result;
	}

	public boolean update(DownloadBean downloadBean) {
		if (downloadBean == null) {
			return false;
		}
		ContentValues initialValues = new ContentValues();
		if (downloadBean.fileName != null) {
			initialValues.put(T_FILE_NAME, downloadBean.fileName);
		}
		if (downloadBean.filelen != 0) {
			initialValues.put(T_FILE_SIZE, downloadBean.filelen);
		}
		if (downloadBean.downloadLen != 0) {
			initialValues.put(T_DOWNLOAD_SIZE, downloadBean.downloadLen);
		}
		if (downloadBean.downloadState != KeyConstants.DOWNLOAD_STATE_UNKOWN) {
			initialValues.put(T_DOWNLOAD_STATE, downloadBean.downloadState);
		}
		initialValues.put(T_OWNER, downloadBean.owner);
		initialValues.put(T_IS_PRIV, downloadBean.isPriv);
		boolean result = mDbEntity.update(databaseTable, initialValues, 
				T_DOWNLOAD_ID + "=\"" + downloadBean.downloadId + "\"", null) > 0;
		return result;
	}
	
	public boolean delete(String downloadID) {
		return delete(T_DOWNLOAD_ID + "=?", new String[] { String.valueOf(downloadID) });
	}

	public boolean deleteAll() {
		return delete(null, null);
	}

	private boolean delete(String whereClause, String[] values) {
		if (whereClause == null) {
			return mDbEntity.delete(databaseTable, null, null) > 0;
		} else {
			return mDbEntity.delete(databaseTable, whereClause, values) > 0;
		}
	}
	
	public DownloadBean queryByUrl(String url) {
		List<DownloadBean> result = getList(null, T_DOWNLOAD_URL + "=?", 
				new String[] { url }, null, null, null, null);
		if (result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}
	
	public DownloadBean queryById(String downloadId) {
		List<DownloadBean> result = getList(null, T_DOWNLOAD_ID + "=?", 
				new String[] { downloadId }, null, null, null, null);
		if (result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	public List<DownloadBean> queryAll() {
		return getList(null, null, null, null,null, null,null);
	}
	
	public List<DownloadBean> queryAll(String user) {
		StringBuilder where = new StringBuilder();
		where.append(T_IS_PRIV).append("=0");
		if (user != null) {
			where.append(" or (");
			where.append(T_IS_PRIV).append("=1");
			where.append(" and ");
			where.append(T_OWNER).append("=\"").append(user).append("\")");
		}
		
		return getList(null, where.toString(), null, null,null, null,null);
	}

	public List<DownloadBean> getList(String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		
		List<DownloadBean> list = new ArrayList<DownloadBean>();
		DownloadBean task = null;
		Cursor c = null;
		
		try {
			c = mDbEntity.query(databaseTable, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			boolean hadata = (c == null) ? false : c.moveToFirst();
			if (hadata) {
				int downloadIdIndex = c.getColumnIndex(T_DOWNLOAD_ID);
				int urlIndex = c.getColumnIndex(T_DOWNLOAD_URL);
				int postParamIndex = c.getColumnIndex(T_POST_PARAM);
				int methodIndex = c.getColumnIndex(T_HTTP_METHOD);
				int pathIndex = c.getColumnIndex(T_SAVE_PATH);
				int folderIndex = c.getColumnIndex(T_SAVE_FOLDER);
				int nameIndex = c.getColumnIndex(T_FILE_NAME);
				int sizeIndex = c.getColumnIndex(T_FILE_SIZE);
				int downloadSizeIndex = c.getColumnIndex(T_DOWNLOAD_SIZE);
				int createTimeIndex = c.getColumnIndex(T_CREATE_TIME);
				int ownerIndex = c.getColumnIndex(T_OWNER);
				int prvIndex = c.getColumnIndex(T_IS_PRIV);
				int stateIndex = c.getColumnIndex(T_DOWNLOAD_STATE);
				int retryIndex = c.getColumnIndex(T_DOWNLOAD_RETRY_COUNT);
				int connectTimeOutIndex = c.getColumnIndex(T_DOWNLOAD_CONNECT_TIMEOUT);
				int readTimeOutIndex = c.getColumnIndex(T_DOWNLOAD_READ_TIMEOUT);
				int httpHeadIndex = c.getColumnIndex(T_HTTP_HEAD);
				int typeIndex = c.getColumnIndex(T_DOWNLOAD_TYPE);
				int versionIndex = c.getColumnIndex(T_FILE_VERSION);
				do {
					task = new DownloadBean();
					if (downloadIdIndex != -1) {
						task.downloadId = c.getString(downloadIdIndex);
					}
					if (urlIndex != -1) {
						task.url = c.getString(urlIndex);
					}
					if (postParamIndex != -1) {
						task.postParam = c.getString(postParamIndex);
					}
					if (methodIndex != -1) {
						task.httpMethod = c.getString(methodIndex);
					}
					if (pathIndex != -1) {
						task.path = c.getString(pathIndex);
					}
					if (folderIndex != -1) {
						task.folder = c.getString(folderIndex);
					}
					if (nameIndex != -1) {
						task.fileName = c.getString(nameIndex);
					}
					if (sizeIndex != -1) {
						task.filelen = c.getLong(sizeIndex);
					}
					if (downloadSizeIndex != -1) {
						task.downloadLen = c.getLong(downloadSizeIndex);
					}
					if (createTimeIndex != -1) {
						task.createTime = Long.parseLong(c.getString(createTimeIndex));
					}
					if (ownerIndex != -1) {
						task.owner = c.getString(ownerIndex);
					}
					if (prvIndex != -1) {
						task.isPriv = c.getInt(prvIndex);
					}
					if (stateIndex != -1) {
						task.downloadState = c.getInt(stateIndex);
					}
					if (retryIndex != -1) {
						task.retryCount = c.getInt(retryIndex);
					}
					if (connectTimeOutIndex != -1) {
						task.connectionTimeout = c.getInt(connectTimeOutIndex);
					}
					if (readTimeOutIndex != -1) {
						task.readTimeout = c.getInt(readTimeOutIndex);
					}
					if (httpHeadIndex != -1) {
						task.httpHead = c.getString(httpHeadIndex);
					}
					if (typeIndex != -1) {
						task.fileType = c.getInt(typeIndex);
					}
					if (versionIndex != -1) {
						task.fileVersion = c.getInt(versionIndex);
					}
					
					if (task.path == null) {
						continue;
					}
					if (task.filelen > 0 && task.filelen == task.downloadLen) {
						//下载完成的文件进行文件检查
						File file = new File(task.path);
						if (! file.exists()) {
							task.downloadState = KeyConstants.DOWNLOAD_STATE_NONE;
							task.downloadLen = 0;
						}
					}
					
					list.add(task);
				} while (c.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}

		return list;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	/**
	 * 创建数据表
	 * 
	 * @param db
	 */
	private void createTable(SQLiteDatabase db, String tableName) {
		ArrayList<DataColumn> list = new ArrayList<DataColumn>();
		list.add(new DataColumn(T_DOWNLOAD_ID, DataType.TEXT, null, false));
		list.add(new DataColumn(T_DOWNLOAD_URL, DataType.TEXT, null, false));
		list.add(new DataColumn(T_POST_PARAM, DataType.TEXT, null, true));
		list.add(new DataColumn(T_HTTP_METHOD, DataType.TEXT, null, false));
		list.add(new DataColumn(T_SAVE_PATH, DataType.TEXT, null, false));
		list.add(new DataColumn(T_SAVE_FOLDER, DataType.TEXT, null, false));
		list.add(new DataColumn(T_FILE_NAME, DataType.TEXT, null, false));
		list.add(new DataColumn(T_FILE_SIZE, DataType.TEXT, null, true));
		list.add(new DataColumn(T_DOWNLOAD_SIZE, DataType.TEXT, null, true));
		list.add(new DataColumn(T_CREATE_TIME, DataType.TEXT, null, true));
		list.add(new DataColumn(T_OWNER, DataType.TEXT, null, true));
		list.add(new DataColumn(T_IS_PRIV, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_DOWNLOAD_STATE, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_DOWNLOAD_RETRY_COUNT, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_DOWNLOAD_CONNECT_TIMEOUT, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_DOWNLOAD_READ_TIMEOUT, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_HTTP_HEAD, DataType.TEXT, null, true));
		list.add(new DataColumn(T_DOWNLOAD_TYPE, DataType.INTEGER, 0, true));
		list.add(new DataColumn(T_FILE_VERSION, DataType.INTEGER, 0, true));
		DbTableUtils.createTable(db, tableName, list);
	}
	
	/**
	 * 更新数据表
	 * 
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 */
	private void onUpgrade(SQLiteDatabase db, String tableName, int oldVersion, int newVersion) {
		if (oldVersion <= 1) {
			//增加type、version字段
			ArrayList<DataColumn> list = new ArrayList<DataColumn>();
			list.add(new DataColumn(T_DOWNLOAD_TYPE, DataType.INTEGER, 0, true));
			list.add(new DataColumn(T_FILE_VERSION, DataType.INTEGER, 0, true));
			for(DataColumn column : list) {
				StringBuilder builder = new StringBuilder();
				builder.append("ALTER TABLE ");
				builder.append(tableName);
				builder.append(" add COLUMN ");
				builder.append(column.name).append(" ");
				builder.append(column.type);
				if (!column.defCanNull) {
					builder.append(" NOT NULL");
				}
				if (column.defValue != null) {
					builder.append(" DEFAULT ").append(String.valueOf(column.defValue));
				}
				
				String sql = builder.toString();
				db.execSQL(sql);
			}
		}
	}

	@Override
	public void createDao(SQLiteDatabase db) {
		Log.v(TAG, "createDao");
		DownloadTable[] createTables = DownloadTable.values();
		for(DownloadTable table : createTables) {
			createTable(db, table.name());
		}
	}

	@Override
	public void upgradeDao(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			DownloadTable[] createTables = DownloadTable.values();
			for(DownloadTable table : createTables) {
				String tableName = table.name();
				boolean isExist = isExistTable(db, tableName);
				if (isExist) {
					Log.v(TAG, "onUpgrade 表已经存在，升级表 :" + tableName);
					onUpgrade(db, tableName, oldVersion, newVersion);
				} else {
					Log.v(TAG, "onUpgrade 创建表 :" + tableName);
					createTable(db, tableName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean isExistTable(SQLiteDatabase db, String tableName) {
		String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					return true;
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}
}
