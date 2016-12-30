package com.lingyang.camera.db.bean;

/**
 * 文件名：LocalRecord
 * 描述：
 * 此类是本地视频和截图实体类
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class LocalRecord {

    public static final int TYPE_MEDIA_ALL = 0;
    public static final int TYPE_MEDIA_VIDEO = 2;
    public static final int TYPE_MEDIA_PHOTO = 1;
    private String mMediaName;
    private long mTimeStamp;
    private int mDuration;
    private int mType;
    private String mFilePath;
    private String mImagePath;
    private long mCreateTime;
    private String mCid;
    private String mUid;

    /**
     * @param mMediaName 视频名称
     * @param mTimestamp 时间戳
     * @param mDuration 时长
     * @param mType       {@link #TYPE_MEDIA_VIDEO} OR {@link #TYPE_MEDIA_PHOTO}
     * @param mFilePath 本地地址
     * @param mImagePath 封面/截图地址
     * @param mCreateTime 创建时间
     * @param mCid 设备id
     * @param mUid 用户id
     */
    public LocalRecord(String mMediaName, long mTimestamp, int mDuration, int mType,
                       String mFilePath, String mImagePath, long mCreateTime, String mCid, String mUid) {
        super();
        this.mMediaName = mMediaName;
        this.mTimeStamp = mTimestamp;
        this.mDuration = mDuration;
        this.mType = mType;
        this.mFilePath = mFilePath;
        this.mImagePath = mImagePath;
        this.mCreateTime = mCreateTime;
        this.mCid = mCid;
        this.mUid = mUid;
    }

    public static String getMediaNameString() {
        return "MediaName";
    }

    public static String getTimestampString() {
        return "Timestamp";
    }

    public static String getDurationString() {
        return "Duration";
    }

    public static String getTypeString() {
        return "Type";
    }

    public static String getFilePathString() {
        return "FilePath";
    }

    public static String getImagePathString() {
        return "ImagePath";
    }

    public static String getCreateTimeString() {
        return "CreateTime";
    }

    public static String getCidString() {
        return "Cid";
    }

    public static String getUidString() {
        return "Uid";
    }

    public String getMediaName() {
        return mMediaName;
    }

    public void setMediaName(String mMediaName) {
        this.mMediaName = mMediaName;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(long mTimestamp) {
        this.mTimeStamp = mTimestamp;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String mFilePath) {
        this.mFilePath = mFilePath;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long mCreateTime) {
        this.mCreateTime = mCreateTime;
    }

    public String getCid() {
        return mCid;
    }

    public void setCid(String cid) {
        this.mCid = cid;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }
}
