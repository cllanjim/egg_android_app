package com.lingyang.camera.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.db.BasicSQLiteHelper;
import com.lingyang.base.utils.db.DataColumn;
import com.lingyang.base.utils.db.DbTableUtils;
import com.lingyang.base.utils.db.IDatabaseDao;
import com.lingyang.camera.db.bean.LocalCall;
import com.lingyang.camera.util.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * 文件名：LocalCallListWrapper
 * 描述：
 * 此类是本地通讯录数据库业务类，增删改查
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class LocalCallListWrapper implements IDatabaseDao {

    private static BasicSQLiteHelper DBHelper;
    private static LocalCallListWrapper mWrapper;
    private final String TABLE_NAME = "LOCALCALL";

    public LocalCallListWrapper() {

    }

    public synchronized static LocalCallListWrapper getInstance() {

        if (mWrapper == null) {
            mWrapper = new LocalCallListWrapper();
            DBHelper = MainDatabaseHelper.getInstance(Utils.getContext());
        }
        return mWrapper;
    }

    public List<LocalCall> getList(String uid) {

        List<LocalCall> list = new ArrayList<LocalCall>();
        Cursor c = null;
        try {
            c = DBHelper.query(TABLE_NAME, null, LocalCall.getUidString() + "=?", new String[]{uid}, null, null, LocalCall.getTimeString() + " DESC", null);
            while (c.moveToNext()) {
                LocalCall localCall = new LocalCall(c.getString(c.getColumnIndex(LocalCall.getNickNameString()))
                        , c.getString(c.getColumnIndex(LocalCall.getHeadString()))
                        , c.getString(c.getColumnIndex(LocalCall.getMobileString()))
                        , c.getLong(c.getColumnIndex(LocalCall.getTimeString()))
                        , c.getString(c.getColumnIndex(LocalCall.getUidString()))
                        , c.getString(c.getColumnIndex(LocalCall.getCalledUidString()))
                        , c.getInt(c.getColumnIndex(LocalCall.getTypeString())));

                list.add(localCall);
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

    public boolean addLocalCall(LocalCall localCall) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(LocalCall.getNickNameString(), localCall.getmNickName());
        contentValues.put(LocalCall.getHeadString(), localCall.getmHead());
        contentValues.put(LocalCall.getMobileString(), localCall.getmMobile());
        contentValues.put(LocalCall.getTimeString(), localCall.getmTime());
        contentValues.put(LocalCall.getUidString(), localCall.getmUid());
        contentValues.put(LocalCall.getCalledUidString(), localCall.getmCalledUid());
        contentValues.put(LocalCall.getTypeString(), localCall.getmType());

        return DBHelper.insert(TABLE_NAME, null, contentValues) > -1;
    }

    /**
     * 查询联系人是否存在
     * @param calledUid 被叫人Uid
     * @return 联系人是否存在
     */
    public boolean LocalCallIsExit(String calledUid) {
        String sql = "select * from " + TABLE_NAME + " where " + LocalCall.getCalledUidString() + " = '" + calledUid + "'";
        Cursor cursor = null;
        try {
            cursor = DBHelper.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }

        }
        return false;
    }

    public boolean updateLocalCall(LocalCall localCall) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(LocalCall.getNickNameString(), localCall.getmNickName());
        contentValues.put(LocalCall.getHeadString(), localCall.getmHead());
        contentValues.put(LocalCall.getMobileString(), localCall.getmMobile());
        contentValues.put(LocalCall.getTimeString(), localCall.getmTime());
        contentValues.put(LocalCall.getUidString(), localCall.getmUid());
        contentValues.put(LocalCall.getCalledUidString(), localCall.getmCalledUid());
        contentValues.put(LocalCall.getTypeString(), localCall.getmType());

        return DBHelper.update(TABLE_NAME, contentValues, LocalCall.getCalledUidString() + " =?", new String[]{localCall.getmCalledUid()}) > 0;
    }

    /**
     * 创建表
     */
    @Override
    public void createDao(SQLiteDatabase db) {

        DataColumn dc1 = new DataColumn(LocalCall.getNickNameString(), DataColumn.DataType.TEXT, false, false);
        DataColumn dc2 = new DataColumn(LocalCall.getHeadString(), DataColumn.DataType.TEXT, false, false);
        DataColumn dc3 = new DataColumn(LocalCall.getMobileString(), DataColumn.DataType.TEXT, false, false);
        DataColumn dc4 = new DataColumn(LocalCall.getUidString(), DataColumn.DataType.TEXT, false, false);
        DataColumn dc5 = new DataColumn(LocalCall.getTimeString(), DataColumn.DataType.TIMESTAMP, false, false);
        DataColumn dc6 = new DataColumn(LocalCall.getCalledUidString(), DataColumn.DataType.TEXT, false, false);
        DataColumn dc7 = new DataColumn(LocalCall.getTypeString(), DataColumn.DataType.INTEGER, false, false);

        List<DataColumn> list = new ArrayList<DataColumn>();
        list.add(dc1);
        list.add(dc2);
        list.add(dc3);
        list.add(dc4);
        list.add(dc5);
        list.add(dc6);
        list.add(dc7);

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
            }
        }
        return false;
    }
}
