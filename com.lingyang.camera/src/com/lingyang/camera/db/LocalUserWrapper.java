package com.lingyang.camera.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.db.BasicSQLiteHelper;
import com.lingyang.base.utils.db.DataColumn;
import com.lingyang.base.utils.db.DbTableUtils;
import com.lingyang.base.utils.db.IDatabaseDao;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：LocalRecordWrapper
 * 描述：
 * 此类是个人信息本地存储业务类，增删改查
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class LocalUserWrapper implements IDatabaseDao {

    private static BasicSQLiteHelper mDbEntity;
    private static LocalUserWrapper mLocalUserWrapper;
    private final String TABLE＿NAME = "LocalUser";
    private LocalUser currentUser;

    /**
     * [该空构造用于反射初始化]
     */
    public LocalUserWrapper() {

    }

    public synchronized static LocalUserWrapper getInstance() {
        if (mLocalUserWrapper == null) {
            mLocalUserWrapper = new LocalUserWrapper();
            mDbEntity = MainDatabaseHelper.getInstance(Utils.getContext());
        }
        return mLocalUserWrapper;
    }

    public LocalUser getLocalUser() {
        if (currentUser == null) {
            String phone = MyPreference.getInstance().getString(
                    Utils.getContext(), MyPreference.LOGIN_PHONE, "");
            String password = MyPreference.getInstance().getString(
                    Utils.getContext(), MyPreference.LOGIN_PASSWORD, "");
            if (phone != null && !phone.equals("") && password != null && !password.equals("")) {
                setLocalUser(exsitUserByPhone(phone, password));
            }
        }
        return currentUser;
    }

    public void setLocalUser(LocalUser localUser) {
        currentUser = localUser;
        if (currentUser != null) {
            try {
                CrashReport.setUserId(currentUser.getUid() + "^"
                        + currentUser.getNickName() + "^"
                        + currentUser.getMobile()); // 设置用户的唯一标识
            } catch (Exception e) {
            }

        }
    }

    public LocalUser exsitUserByPhone(String phone, String password) {

        LocalUser localUser = null;
        Cursor c = null;
        try {
            c = mDbEntity.query(TABLE＿NAME, null, LocalUser.getMobileString()
                    + "=? and " + LocalUser.GetPasswordString() + "=?", new String[]{phone, password}, null, null, null, "1");
            if (c != null && c.moveToFirst()) {
                {
                    localUser = new LocalUser(
                            c.getString(c.getColumnIndex(LocalUser.getNickNameString())),
                            c.getString(c.getColumnIndex(LocalUser.getHeadString())),
                            c.getString(c.getColumnIndex(LocalUser.GetPasswordString())),
                            c.getString(c.getColumnIndex(LocalUser.getIPString())),
                            c.getString(c.getColumnIndex(LocalUser.getMobileString())),
                            c.getLong(c.getColumnIndex(LocalUser.getExpireString())),
                            c.getString(c.getColumnIndex(LocalUser.GetUidString())),
                            c.getString(c.getColumnIndex(LocalUser.getAccessTokenString())),

                            c.getString(c.getColumnIndex(LocalUser.getControlString())),
                            c.getString(c.getColumnIndex(LocalUser.getInitStringString())),
                            c.getString(c.getColumnIndex(LocalUser.getPhoneConnectAddrString())),
                            c.getString(c.getColumnIndex(LocalUser.getUserTokenString())),
                            c.getString(c.getColumnIndex(LocalUser.getUserTokenExpireString()))
                    );
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
        return localUser;
    }

    public Boolean addUser(LocalUser localUser) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LocalUser.getNickNameString(), localUser.getNickName());
        initialValues.put(LocalUser.getHeadString(), localUser.getHead());
        initialValues.put(LocalUser.GetPasswordString(), localUser.getPassword());
        initialValues.put(LocalUser.getMobileString(), localUser.getMobile());
        initialValues.put(LocalUser.getIPString(), localUser.getIP());
        initialValues.put(LocalUser.GetUidString(), localUser.getUid());
        initialValues.put(LocalUser.getAccessTokenString(), localUser.getAccessToken());
        initialValues.put(LocalUser.getExpireString(), localUser.getExpire());
        initialValues.put(LocalUser.getLastLoginTimeString(), localUser.getLastLoginTime());


        initialValues.put(LocalUser.getControlString(), localUser.getControl());
        initialValues.put(LocalUser.getInitStringString(), localUser.getInitString());
        initialValues.put(LocalUser.getPhoneConnectAddrString(), localUser.getPhoneConnectAddr());
        initialValues.put(LocalUser.getUserTokenString(), localUser.getUserToken());
        initialValues.put(LocalUser.getUserTokenExpireString(), localUser.getUserTokenExpire());
        return mDbEntity.insert(TABLE＿NAME, null, initialValues) > -1 ? true
                : false;
    }

    public Boolean updateUser(LocalUser localUser) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(LocalUser.getNickNameString(), localUser.getNickName());
        initialValues.put(LocalUser.getHeadString(), localUser.getHead());
        initialValues.put(LocalUser.GetPasswordString(), localUser.getPassword());
        initialValues.put(LocalUser.getMobileString(), localUser.getMobile());
        initialValues.put(LocalUser.getIPString(), localUser.getIP());
        initialValues.put(LocalUser.GetUidString(), localUser.getUid());
        initialValues.put(LocalUser.getAccessTokenString(), localUser.getAccessToken());
        initialValues.put(LocalUser.getExpireString(), localUser.getExpire());
        initialValues.put(LocalUser.getLastLoginTimeString(), localUser.getLastLoginTime());

        initialValues.put(LocalUser.getControlString(), localUser.getControl());
        initialValues.put(LocalUser.getInitStringString(), localUser.getInitString());
        initialValues.put(LocalUser.getPhoneConnectAddrString(), localUser.getPhoneConnectAddr());
        initialValues.put(LocalUser.getUserTokenString(), localUser.getUserToken());
        initialValues.put(LocalUser.getUserTokenExpireString(), localUser.getUserTokenExpire());
        return mDbEntity.update(TABLE＿NAME, initialValues, LocalUser.getMobileString() + "=?",
                new String[]{localUser.getMobile()}) > 0 ? true : false;
    }

    @Override
    public void createDao(SQLiteDatabase db) {
        CLog.v("create LocalUser");
        DataColumn dataColumn1 = new DataColumn(LocalUser.getNickNameString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn2 = new DataColumn(LocalUser.GetPasswordString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn3 = new DataColumn(LocalUser.getHeadString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn4 = new DataColumn(LocalUser.getMobileString(),
                DataColumn.DataType.TEXT, false, false);
        DataColumn dataColumn5 = new DataColumn(LocalUser.getIPString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn6 = new DataColumn(LocalUser.getExpireString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn7 = new DataColumn(LocalUser.GetUidString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn8 = new DataColumn(LocalUser.getAccessTokenString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn9 = new DataColumn(LocalUser.getLastLoginTimeString(),
                DataColumn.DataType.TEXT, false, true);

        DataColumn dataColumn10 = new DataColumn(LocalUser.getControlString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn11 = new DataColumn(LocalUser.getInitStringString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn12 = new DataColumn(LocalUser.getPhoneConnectAddrString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn13 = new DataColumn(LocalUser.getUserTokenString(),
                DataColumn.DataType.TEXT, false, true);
        DataColumn dataColumn14 = new DataColumn(LocalUser.getUserTokenExpireString(),
                DataColumn.DataType.TEXT, false, true);


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
        list.add(dataColumn10);
        list.add(dataColumn11);
        list.add(dataColumn12);
        list.add(dataColumn13);
        list.add(dataColumn14);

        DbTableUtils.createTable(db, TABLE＿NAME, list);
    }

    @Override
    public void upgradeDao(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            CLog.v("upgradeDao oldVersion:" + oldVersion + " newVersion:" + newVersion);
            boolean isExist = isExistTable(db, TABLE＿NAME);
            if (!isExist) {
                createDao(db);
                return;
            }
            for (int j = oldVersion + 1; j <= newVersion; j++) {
                switch (j) {
                    case MainDatabaseHelper.DATABASE_VERSION_3:
                        String sqlUpgrade1 = "ALTER TABLE " + TABLE＿NAME + " ADD COLUMN "
                                + LocalUser.getHeadString() + " TEXT ";
                        String sqlUpgrade2 = "ALTER TABLE " + TABLE＿NAME + " ADD COLUMN "
                                + LocalUser.getIsBindEmailString() + " TEXT ";
                        db.execSQL(sqlUpgrade1);
                        db.execSQL(sqlUpgrade2);
                        CLog.v("update LocalUser end " + j);
                        break;

                    case MainDatabaseHelper.DATABASE_VERSION_7:
                        String sqlUpgrade3 = "ALTER TABLE " + TABLE＿NAME + " ADD COLUMN "
                                + LocalUser.GetPasswordString() + " TEXT ";
                        db.execSQL(sqlUpgrade3);
                        CLog.v("update LocalUser end " + j);
                        break;

                    case MainDatabaseHelper.DATABASE_VERSION_NEWEST:
                        String sqlUpgrade4 = "ALTER TABLE " + TABLE＿NAME + " ADD COLUMN "
                                + LocalUser.getControlString() + " TEXT ";
                        String sqlUpgrade5 = "ALTER TABLE " + TABLE＿NAME + " ADD COLUMN "
                                + LocalUser.getInitStringString() + " TEXT ";
                        String sqlUpgrade6 = "ALTER TABLE " + TABLE＿NAME + " ADD COLUMN "
                                + LocalUser.getPhoneConnectAddrString() + " TEXT ";
                        String sqlUpgrade7 = "ALTER TABLE " + TABLE＿NAME + " ADD COLUMN "
                                + LocalUser.getUserTokenString() + " TEXT ";
                        String sqlUpgrade8 = "ALTER TABLE " + TABLE＿NAME + " ADD COLUMN "
                                + LocalUser.getUserTokenExpireString() + " TEXT ";
                        db.execSQL(sqlUpgrade4);
                        db.execSQL(sqlUpgrade5);
                        db.execSQL(sqlUpgrade6);
                        db.execSQL(sqlUpgrade7);
                        db.execSQL(sqlUpgrade8);
                        CLog.v("update LocalUser end " + j);
                        break;
                }
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
