package com.lingyang.camera.db;

import android.content.Context;

import com.lingyang.base.utils.db.BasicSQLiteHelper;
import com.lingyang.base.utils.db.IDatabaseDao;

import java.util.HashSet;

/**
 * 文件名：LocalRecordWrapper
 * 描述：
 * 此类是数据库业务帮助类
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class MainDatabaseHelper extends BasicSQLiteHelper {

    public static final int DATABASE_VERSION_3 = 3;
    public static final int DATABASE_VERSION_7 = 7;
    public static final int DATABASE_VERSION_NEWEST = 8;
    private static final String DATABASE_NAME = "main.db";
    private static MainDatabaseHelper mMainDatabaseHelper;

    private MainDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION_NEWEST);
    }

    public synchronized static MainDatabaseHelper getInstance(Context context) {
        if (mMainDatabaseHelper == null) {
            mMainDatabaseHelper = new MainDatabaseHelper(context);
        }
        return mMainDatabaseHelper;
    }

    @Override
    protected HashSet<Class<? extends IDatabaseDao>> getTableDaoClass() {
        HashSet<Class<? extends IDatabaseDao>> set = new HashSet<Class<? extends IDatabaseDao>>();
        set.add(LocalUserWrapper.class);
        set.add(LocalRecordWrapper.class);
        set.add(LocalCallListWrapper.class);
        set.add(CoverUrlRecordWrapper.class);
        return set;
    }
}
