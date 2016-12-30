package com.lingyang.camera.db.bean;

/**
 * 文件名: CoverUrlRecord
 * 描    述: [该类的简要描述]
 * 创建人: 杜舒
 * 创建时间: 2016/4/26
 */
public class CoverUrlRecord {
    private String mCid;
    private String mCoverUrl;
    private String timestamp;

    public CoverUrlRecord(String cid, String coverUrl, String timestamp) {
        mCid = cid;
        mCoverUrl = coverUrl;
        this.timestamp = timestamp;
    }
    public static String GetCidString() {
        return "Cid";
    }
    public static String GetCoverUrlString() {
        return "CoverUrl";
    }
    public static String GetTimestampString() {
        return "Timestamp";
    }
    public String getCid() {

        return mCid;
    }

    public void setCid(String cid) {
        mCid = cid;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CoverUrlRecord{" +
                "mCid='" + mCid + '\'' +
                ", mCoverUrl='" + mCoverUrl + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
