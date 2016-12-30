package com.lingyang.base.utils.uploadmanager.storage.db;

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
import com.lingyang.base.utils.uploadmanager.KeyConstants;
import com.lingyang.base.utils.uploadmanager.UploadConfiguration.UploadTable;
import com.lingyang.base.utils.uploadmanager.storage.UploadBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * [下载任务DAO]
 * 可能存在操作多个Table的情况，操作db的时候需要指定具体的类
 * 使用SDK前需要提前指定会有哪些Table
 */
public class UploadTaskDao implements IDatabaseDao{
	public static final String TAG = UploadTaskDao.class.getSimpleName();

	private final String T_UPLOAD_ID = "uploadId";
	private final String T_UPLOAD_URL = "url";
	private final String T_POST_PARAM = "postParam";
	private final String T_FILE_PATH = "filePath";
	private final String T_FILE_NAME = "fileName";
	private final String T_FILE_SIZE = "fileSize";
	private final String T_UPLOAD_SIZE = "uploadSize";
	private final String T_CREATE_TIME = "createTime";
	private final String T_OWNER = "owner";
	private final String T_IS_PRIV = "isPriv";
	private final String T_UPLOAD_STATE = "state";
	private final String T_UPLOAD_RETRY_COUNT = "retryCount";
	private final String T_UPLOAD_CONNECT_TIMEOUT = "connectTimeOut";
	private final String T_UPLOAD_READ_TIMEOUT = "readTimeOut";
	private final String T_HTTP_HEAD = "httpHead";
	private final String T_UPLOAD_TYPE = "type";
	private final String T_FILE_VERSION = "version";

	private BasicSQLiteHelper mDbEntity;
	
	protected String databaseTable;

	public UploadTaskDao(Context context, String datableTable) {
		mDbEntity = UploadDatabaseHelper.getInstance(context);
		this.databaseTable = datableTable;
	}
	
	/**
	 * [该空构造用于反射初始化]
	 */
	public UploadTaskDao() {}
	
	public boolean save(List<UploadBean> list) {
		if (list == null || list.size() <= 0) {
			return false;
		}

		boolean result = false;
		try {
			mDbEntity.beginTransaction();
			for (UploadBean item : list) {
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
	
	public long save(UploadBean UploadBean) {
		if (UploadBean == null) {
			return -1;
		}
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(T_UPLOAD_ID, UploadBean.uploadId);
		initialValues.put(T_UPLOAD_URL, UploadBean.url);
		initialValues.put(T_POST_PARAM, UploadBean.postParam);
		initialValues.put(T_FILE_PATH, UploadBean.filePath);
		initialValues.put(T_FILE_NAME, UploadBean.fileName);
		initialValues.put(T_FILE_SIZE, UploadBean.filelen);
		initialValues.put(T_UPLOAD_SIZE, UploadBean.uploadLen);
		initialValues.put(T_CREATE_TIME, UploadBean.createTime);
		initialValues.put(T_OWNER, UploadBean.owner);
		initialValues.put(T_IS_PRIV, UploadBean.isPriv);
		initialValues.put(T_UPLOAD_STATE, UploadBean.uploadState);
		initialValues.put(T_UPLOAD_RETRY_COUNT, UploadBean.retryCount);
		initialValues.put(T_UPLOAD_CONNECT_TIMEOUT, UploadBean.connectionTimeout);
		initialValues.put(T_UPLOAD_READ_TIMEOUT, UploadBean.readTimeout);
		initialValues.put(T_HTTP_HEAD, UploadBean.httpHead);
		initialValues.put(T_UPLOAD_TYPE, UploadBean.fileType);
		initialValues.put(T_FILE_VERSION, UploadBean.fileVersion);
		long result = mDbEntity.insert(databaseTable, null, initialValues);
		return result;
	}

	public boolean update(UploadBean UploadBean) {
		if (UploadBean == null) {
			return false;
		}
		ContentValues initialValues = new ContentValues();
		if (UploadBean.fileName != null) {
			initialValues.put(T_FILE_NAME, UploadBean.fileName);
		}
		if (UploadBean.filelen != 0) {
			initialValues.put(T_FILE_SIZE, UploadBean.filelen);
		}
		if (UploadBean.uploadLen != 0) {
			initialValues.put(T_UPLOAD_SIZE, UploadBean.uploadLen);
		}
		if (UploadBean.uploadState != KeyConstants.UPLOAD_STATE_UNKOWN) {
			initialValues.put(T_UPLOAD_STATE, UploadBean.uploadState);
		}
		initialValues.put(T_OWNER, UploadBean.owner);
		initialValues.put(T_IS_PRIV, UploadBean.isPriv);
		boolean result = mDbEntity.update(databaseTable, initialValues, 
				T_UPLOAD_ID + "=\"" + UploadBean.uploadId + "\"", null) > 0;
		return result;
	}
	
	public boolean delete(String uploadID) {
		return delete(T_UPLOAD_ID + "=?", new String[] { String.valueOf(uploadID) });
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
	
	/*public UploadBean queryByUrl(String url) {
		List<UploadBean> result = getList(null, T_UPLOAD_URL + "=?", 
				new String[] { url }, null, null, null, null);
		if (result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}*/
	
	public UploadBean queryById(String uploadId) {
		List<UploadBean> result = getList(null, T_UPLOAD_ID + "=?", 
				new String[] { uploadId }, null, null, null, null);
		if (result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	public List<UploadBean> queryAll() {
		return getList(null, null, null, null,null, null,null);
	}
	
	public List<UploadBean> queryAll(int state) {
        return getList(null, T_UPLOAD_STATE + " = ? ", new String[]{state+""}, null,null, null,null);
    }
	
	public List<UploadBean> queryExcludeStateAll(int state) {
	    return getList(null, T_UPLOAD_STATE + " != ? ", new String[]{state+""}, null,null, null,null);
	}
	
	public List<UploadBean> queryAll(String user) {
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

	public List<UploadBean> getList(String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		
		List<UploadBean> list = new ArrayList<UploadBean>();
		UploadBean task = null;
		Cursor c = null;
		
		try {
			c = mDbEntity.query(databaseTable, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			boolean hadata = (c == null) ? false : c.moveToFirst();
			if (hadata) {
				int uploadIdIndex = c.getColumnIndex(T_UPLOAD_ID);
				int urlIndex = c.getColumnIndex(T_UPLOAD_URL);
				int postParamIndex = c.getColumnIndex(T_POST_PARAM);
				int pathIndex = c.getColumnIndex(T_FILE_PATH);
				int nameIndex = c.getColumnIndex(T_FILE_NAME);
				int sizeIndex = c.getColumnIndex(T_FILE_SIZE);
				int uploadSizeIndex = c.getColumnIndex(T_UPLOAD_SIZE);
				int createTimeIndex = c.getColumnIndex(T_CREATE_TIME);
				int ownerIndex = c.getColumnIndex(T_OWNER);
				int prvIndex = c.getColumnIndex(T_IS_PRIV);
				int stateIndex = c.getColumnIndex(T_UPLOAD_STATE);
				int retryIndex = c.getColumnIndex(T_UPLOAD_RETRY_COUNT);
				int connectTimeOutIndex = c.getColumnIndex(T_UPLOAD_CONNECT_TIMEOUT);
				int readTimeOutIndex = c.getColumnIndex(T_UPLOAD_READ_TIMEOUT);
				int httpHeadIndex = c.getColumnIndex(T_HTTP_HEAD);
				int typeIndex = c.getColumnIndex(T_UPLOAD_TYPE);
				int versionIndex = c.getColumnIndex(T_FILE_VERSION);
				do {
					task = new UploadBean();
					if (uploadIdIndex != -1) {
						task.uploadId = c.getString(uploadIdIndex);
					}
					if (urlIndex != -1) {
						task.url = c.getString(urlIndex);
					}
					if (postParamIndex != -1) {
						task.postParam = c.getString(postParamIndex);
					}
					if (pathIndex != -1) {
						task.filePath = c.getString(pathIndex);
					}
					if (nameIndex != -1) {
						task.fileName = c.getString(nameIndex);
					}
					if (sizeIndex != -1) {
						task.filelen = c.getLong(sizeIndex);
					}
					if (uploadSizeIndex != -1) {
						task.uploadLen = c.getLong(uploadSizeIndex);
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
						task.uploadState = c.getInt(stateIndex);
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
					
					if (task.filePath == null) {
						continue;
					}
					if (task.filelen > 0 && task.filelen == task.uploadLen) {
						//下载完成的文件进行文件检查
						File file = new File(task.filePath);
						if (! file.exists()) {
							task.uploadState = KeyConstants.UPLOAD_STATE_NONE;
							task.uploadLen = 0;
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
		list.add(new DataColumn(T_UPLOAD_ID, DataType.TEXT, null, false));
		list.add(new DataColumn(T_UPLOAD_URL, DataType.TEXT, null, true));
		list.add(new DataColumn(T_POST_PARAM, DataType.TEXT, null, true));
		list.add(new DataColumn(T_FILE_PATH, DataType.TEXT, null, false));
		list.add(new DataColumn(T_FILE_NAME, DataType.TEXT, null, false));
		list.add(new DataColumn(T_FILE_SIZE, DataType.TEXT, null, true));
		list.add(new DataColumn(T_UPLOAD_SIZE, DataType.TEXT, null, true));
		list.add(new DataColumn(T_CREATE_TIME, DataType.TEXT, null, true));
		list.add(new DataColumn(T_OWNER, DataType.TEXT, null, true));
		list.add(new DataColumn(T_IS_PRIV, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_UPLOAD_STATE, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_UPLOAD_RETRY_COUNT, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_UPLOAD_CONNECT_TIMEOUT, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_UPLOAD_READ_TIMEOUT, DataType.INTEGER, null, true));
		list.add(new DataColumn(T_HTTP_HEAD, DataType.TEXT, null, true));
		list.add(new DataColumn(T_UPLOAD_TYPE, DataType.INTEGER, 0, true));
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
		/*if (oldVersion <= 1) {
			//增加type、version字段
			ArrayList<DataColumn> list = new ArrayList<DataColumn>();
			list.add(new DataColumn(T_UPLOAD_TYPE, DataType.INTEGER, 0, true));
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
		}*/
	}

	@Override
	public void createDao(SQLiteDatabase db) {
		Log.v(TAG, "createDao");
		UploadTable[] createTables = UploadTable.values();
		for(UploadTable table : createTables) {
			createTable(db, table.name());
		}
	}

	@Override
	public void upgradeDao(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
		    UploadTable[] createTables = UploadTable.values();
			for(UploadTable table : createTables) {
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
