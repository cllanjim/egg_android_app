package com.lingyang.camera.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.db.DataColumn;
import com.lingyang.base.utils.db.DbTableUtils;
import com.lingyang.base.utils.db.IDatabaseDao;
import com.lingyang.camera.db.bean.CoverUrlRecord;
import com.lingyang.camera.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名: CoverUrlRecordWrapper
 * 描    述: [该类的简要描述]
 * 创建人: 杜舒
 * 创建时间: 2016/4/26
 */
public class CoverUrlRecordWrapper implements IDatabaseDao {
    private static CoverUrlRecordWrapper mCoverUrlRecordWrapper;
    private static MainDatabaseHelper mDbEntity;
    private final String TABLE＿NAME = "CoverUrlRecord";

    public CoverUrlRecordWrapper() {
    }

    public synchronized static CoverUrlRecordWrapper getInstance() {
        if (mCoverUrlRecordWrapper == null) {
            mCoverUrlRecordWrapper = new CoverUrlRecordWrapper();
            mDbEntity = MainDatabaseHelper.getInstance(Utils.getContext());
        }
        return mCoverUrlRecordWrapper;
    }

    public Boolean saveCoverUrl(CoverUrlRecord coverUrlRecord) {
        CLog.v("saveCoverUrl " + coverUrlRecord);
        ContentValues initialValues = new ContentValues();
        initialValues.put(CoverUrlRecord.GetCidString(), coverUrlRecord.getCid());
        initialValues.put(CoverUrlRecord.GetCoverUrlString(), coverUrlRecord.getCoverUrl());
        initialValues.put(CoverUrlRecord.GetTimestampString(), coverUrlRecord.getTimestamp());
        return mDbEntity.insert(TABLE＿NAME, null, initialValues) > -1;
    }

    public Boolean updateCoverUrl(CoverUrlRecord coverUrlRecord) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(CoverUrlRecord.GetCidString(), coverUrlRecord.getCid());
        initialValues.put(CoverUrlRecord.GetCoverUrlString(), coverUrlRecord.getCoverUrl());
        initialValues.put(CoverUrlRecord.GetTimestampString(), coverUrlRecord.getTimestamp());
        return mDbEntity.update(TABLE＿NAME, initialValues, CoverUrlRecord.GetCidString() + "=?",
                new String[]{coverUrlRecord.getCid()}) > 0;
    }

    public CoverUrlRecord getCoverUrlRecord(String cid) {
        CoverUrlRecord coverUrlRecord = null;
        Cursor c = null;
        try {
            c = mDbEntity.query(TABLE＿NAME, null, CoverUrlRecord.GetCidString() + "=?",
                    new String[]{cid}, null, null, null, "1");
            if (c != null && c.moveToFirst()) {
                {
                    coverUrlRecord = new CoverUrlRecord(
                            c.getString(c.getColumnIndex(CoverUrlRecord.GetCidString())),
                            c.getString(c.getColumnIndex(CoverUrlRecord.GetCoverUrlString())),
                            c.getString(c.getColumnIndex(CoverUrlRecord.GetTimestampString())));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        return coverUrlRecord;
    }

    @Override
    public void createDao(SQLiteDatabase db) {
        CLog.v("create CoverUrlRecord");
        DataColumn dataColumn1 = new DataColumn(CoverUrlRecord.GetCidString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn2 = new DataColumn(CoverUrlRecord.GetCoverUrlString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn3 = new DataColumn(CoverUrlRecord.GetTimestampString(),
                DataColumn.DataType.TEXT, false, true);
        List<DataColumn> list = new ArrayList<DataColumn>();
        list.add(dataColumn1);
        list.add(dataColumn2);
        list.add(dataColumn3);
        DbTableUtils.createTable(db, TABLE＿NAME, list);
    }


    @Override
    public void upgradeDao(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            CLog.v("upgradeDao oldVersion:" + oldVersion + " newVersion:" + newVersion);
            boolean isExist = isExistTable(db, TABLE＿NAME);
            if (!isExist) {
                createDao(db);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isExistTable(SQLiteDatabase db, String tableName) {
        String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name =' "
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
            }
        }
        return false;
    }

}
