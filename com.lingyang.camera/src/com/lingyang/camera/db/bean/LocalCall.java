package com.lingyang.camera.db.bean;

/**
 * 文件名：LocalCall
 * 描述：
 * 此类是本地通讯录实体类
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class LocalCall {

    public static final int DIAL_OUT = 1;
    public static final int DIAL_ON = 2;
    private String mNickName;
    private String mHead;
    private String mMobile;
    private long mTime;
    private String mUid;
    private String mCalledUid;
    private int mType;

    public boolean isOnline() {
        return mIsOnline;
    }

    public void setIsOnline(boolean isOnline) {
        mIsOnline = isOnline;
    }

    private boolean mIsOnline;

    public LocalCall(String mNickName, String mHead, String mMobile, long mTime, String mUid, String mCalledUid, int type) {
        this.mNickName = mNickName;
        this.mHead = mHead;
        this.mMobile = mMobile;
        this.mTime = mTime;
        this.mUid = mUid;
        this.mCalledUid = mCalledUid;
        this.mType = type;
    }

    public static String getNickNameString() {
        return "NICKNAME";
    }

    public static String getHeadString() {
        return "HEAD";
    }

    public static String getMobileString() {
        return "MOBILE";
    }

    public static String getUidString() {
        return "UID";
    }

    public static String getTimeString() {
        return "TIME";
    }

    public static String getCalledUidString() {
        return "CALLEDUID";
    }

    public static String getTypeString() {
        return "TYPE";
    }

    public String getmHead() {
        return mHead;
    }

    public void setmHead(String mHead) {
        this.mHead = mHead;
    }

    public String getmNickName() {
        return mNickName;
    }

    public void setmNickName(String mNickName) {
        this.mNickName = mNickName;
    }

    public String getmMobile() {
        return mMobile;
    }

    public void setmMobile(String mMobile) {
        this.mMobile = mMobile;
    }

    public long getmTime() {
        return mTime;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
    }

    public String getmUid() {
        return mUid;
    }

    public void setmUid(String cId) {
        this.mUid = cId;
    }

    public String getmCalledUid() {
        return mCalledUid;
    }

    public void setmCalledUid(String mCalledUid) {
        this.mCalledUid = mCalledUid;
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }


}
