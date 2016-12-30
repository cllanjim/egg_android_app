package com.lingyang.camera.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.db.BasicSQLiteHelper;
import com.lingyang.base.utils.db.DataColumn;
import com.lingyang.base.utils.db.DbTableUtils;
import com.lingyang.base.utils.db.IDatabaseDao;
import com.lingyang.camera.db.bean.LocalRecord;
import com.lingyang.camera.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：LocalRecordWrapper
 * 描述：
 * 此类是本地视频和截图存储业务类，增删改查
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class LocalRecordWrapper implements IDatabaseDao {

	private final String TABLE_NAME = "LocalMedia";
	private static BasicSQLiteHelper mDbEntity;

	/**
	 * [该空构造用于反射初始化]
	 */
	public LocalRecordWrapper() {
	}

	private static LocalRecordWrapper mLocalRecordWrapper;

	public synchronized static LocalRecordWrapper getInstance() {
		if (mLocalRecordWrapper == null) {
			mLocalRecordWrapper = new LocalRecordWrapper();
			mDbEntity = MainDatabaseHelper.getInstance(Utils.getContext());
		}
		return mLocalRecordWrapper;
	}

	public long rawQuery(int type, String uid) {
		String sql;
		if (type == LocalRecord.TYPE_MEDIA_ALL) {
			sql = "select count(*) from  " + TABLE_NAME
					+ " where " + LocalRecord.getUidString() + " = '" + uid+ "'";
		} else if (type == LocalRecord.TYPE_MEDIA_PHOTO) {
			sql = "select count(*) from " + TABLE_NAME
					+ " where " + LocalRecord.getTypeString() + " = "
					+ LocalRecord.TYPE_MEDIA_PHOTO + " and "
					+ LocalRecord.getUidString() + " = '" + uid+ "'";
		} else {
			sql = "select count(*) from " + TABLE_NAME
					+ " where " + LocalRecord.getTypeString() + " = "
					+ LocalRecord.TYPE_MEDIA_VIDEO + " and "
					+ LocalRecord.getUidString() + " = '" + uid + "'";
		}
		Cursor c =null;
		long count = 0;
		try {
			c = mDbEntity.rawQuery(sql, null);
			if (c != null && c.moveToFirst()) {
				count = c.getLong(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
		return count;
	}

	public Boolean addLocalMedia(LocalRecord localMedia) {
		CLog.v("addLocalMedia:" + TABLE_NAME);
		ContentValues initialValues = new ContentValues();
		initialValues.put(LocalRecord.getMediaNameString(),
				localMedia.getMediaName());
		initialValues.put(LocalRecord.getTypeString(), localMedia.getType());
		initialValues.put(LocalRecord.getCidString(), localMedia.getCid());
		initialValues.put(LocalRecord.getUidString(), localMedia.getUid());
		initialValues.put(LocalRecord.getDurationString(),
				localMedia.getDuration());
		initialValues.put(LocalRecord.getFilePathString(),
				localMedia.getFilePath());
		initialValues.put(LocalRecord.getTimestampString(),
				localMedia.getTimeStamp());
		initialValues.put(LocalRecord.getCreateTimeString(),
				localMedia.getCreateTime());
		initialValues.put(LocalRecord.getImagePathString(),
				localMedia.getImagePath());
		return mDbEntity.insert(TABLE_NAME, null, initialValues) > -1;
	}

	public Boolean delMediaFile(LocalRecord localMedia) {

		return mDbEntity.delete(TABLE_NAME, LocalRecord.getMediaNameString()
				+ "=?", new String[]{localMedia.getMediaName()}) > -1;
	}

	/**
	 * @param type
	 *            {@link LocalRecord#TYPE_MEDIA_ALL} or
	 *            {@link LocalRecord#TYPE_MEDIA_VIDEO} or
	 *            {@link LocalRecord#TYPE_MEDIA_PHOTO}
	 * @return 返回本地视频或截图列表
	 */
	public List<LocalRecord> getLocalMediaList(int type, int pageSize,
			int currentSize) {

		List<LocalRecord> list = new ArrayList<LocalRecord>();
		Cursor c = null;
		String limit = currentSize + "," + pageSize;
		try {
			if (LocalRecord.TYPE_MEDIA_ALL == type) {
				c = mDbEntity.query(TABLE_NAME, null, LocalRecord.getUidString()
						+ "=?", new String[] { LocalUserWrapper.getInstance()
						.getLocalUser().getUid() }, null, null,
						LocalRecord.getCreateTimeString() + " DESC ", limit);
			} else {
				c = mDbEntity.query(TABLE_NAME,null,LocalRecord.getTypeString() + "=? and "
								+ LocalRecord.getUidString() + "=? ",new String[] { type + "",
								LocalUserWrapper.getInstance().getLocalUser().getUid() }, null,
						       null,LocalRecord.getCreateTimeString() + " DESC ", limit);
			}
			while (c.moveToNext()) {
				LocalRecord localMedia = new LocalRecord(
						c.getString(c.getColumnIndex(LocalRecord
								.getMediaNameString())),
						c.getLong(c.getColumnIndex(LocalRecord
								.getTimestampString())),
						c.getInt(c.getColumnIndex(LocalRecord
								.getDurationString())),
						c.getInt(c.getColumnIndex(LocalRecord.getTypeString())),
						c.getString(c.getColumnIndex(LocalRecord
								.getFilePathString())),
						c.getString(c.getColumnIndex(LocalRecord
								.getImagePathString())),
						c.getLong(c.getColumnIndex(LocalRecord
								.getCreateTimeString())),
						c.getString(c.getColumnIndex(LocalRecord.getCidString())),
						c.getString(c.getColumnIndex(LocalRecord.getUidString())));
				list.add(localMedia);
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
	public void createDao(SQLiteDatabase db) {
		CLog.v("create " + TABLE_NAME);
		DataColumn dataColumn1 = new DataColumn(
				LocalRecord.getMediaNameString(), DataColumn.DataType.TEXT,
				false, false);
		DataColumn dataColumn2 = new DataColumn(
				LocalRecord.getTimestampString(),
				DataColumn.DataType.TIMESTAMP, false, false);
		DataColumn dataColumn3 = new DataColumn(
				LocalRecord.getDurationString(), DataColumn.DataType.INTEGER,
				false, false);
		DataColumn dataColumn4 = new DataColumn(LocalRecord.getTypeString(),
				DataColumn.DataType.INTEGER, false, false);
		DataColumn dataColumn5 = new DataColumn(
				LocalRecord.getFilePathString(), DataColumn.DataType.TEXT,
				false, false);
		DataColumn dataColumn6 = new DataColumn(
				LocalRecord.getImagePathString(), DataColumn.DataType.TEXT,
				false, false);
		DataColumn dataColumn7 = new DataColumn(
				LocalRecord.getCreateTimeString(),
				DataColumn.DataType.TIMESTAMP, false, false);
		DataColumn dataColumn8 = new DataColumn(LocalRecord.getCidString(),
				DataColumn.DataType.TEXT, false, false);
		DataColumn dataColumn9 = new DataColumn(LocalRecord.getUidString(),
				DataColumn.DataType.TEXT, false, false);

		List<DataColumn> list = new ArrayList<DataColumn>();
		list.add(dataColumn1);
		list.add(dataColumn2);
		list.add(dataColumn3);
		list.add(dataColumn4);
		list.add(dataColumn5);
		list.add(dataColumn6);
		list.add(dataColumn7);
		list.add(dataColumn8);
		list.add(dataColumn9);

		DbTableUtils.createTable(db, TABLE_NAME, list);
	}

	@Override
	public void upgradeDao(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			CLog.v("update " + TABLE_NAME);
			boolean isExist = isExistTable(db, TABLE_NAME);
			if (!isExist) {
				createDao(db);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isExistTable(SQLiteDatabase db, String tableName) {
		String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='"
				+ tableName + "'";
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
				cursor=null;
			}
		}
		return false;
	}
}
