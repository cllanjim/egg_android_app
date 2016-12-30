package com.lingyang.base.utils.downloadmanager.excutor;

import android.util.SparseArray;

import com.lingyang.base.utils.downloadmanager.KeyConstants;

public class DownloadState {
	
	public final static int STATE_FAIL = KeyConstants.DOWNLOAD_STATE_FAIL;
	public final static int STATE_SUC = KeyConstants.DOWNLOAD_STATE_SUC;
	public final static int STATE_PENDING = KeyConstants.DOWNLOAD_STATE_PENDING;
	public final static int STATE_INTO_DOWNLOADING_QUEUE = KeyConstants.DOWNLOAD_STATE_INTO_DOWNLOADING_QUEUE;
	public final static int STATE_PREPARE = KeyConstants.DOWNLOAD_STATE_PREPARE;
	public final static int STATE_START = KeyConstants.DOWNLOAD_STATE_START;
	public final static int STATE_DOWNLOADING = KeyConstants.DOWNLOAD_STATE_DOWNLOADING;
	public final static int STATE_PAUSE = KeyConstants.DOWNLOAD_STATE_PAUSE;
	public final static int STATE_NONE = KeyConstants.DOWNLOAD_STATE_NONE;

	/**
	 * -2	无状态
	 * -1	失败
	 *  0 	下载完成
	 *  1	准备中
	 *  2	下载开始
	 *  3 	下载中，进度更新
	 *  4 	下载等待, 排队中
	 *  5 	下载停止
	 */
	private int state;
	
	private int retryCount;
	
	private String downloadId;
	
	private String uri;
	
	private int progress;
	
	private long downloadLen;
	
	private long totalLen;
	
	private int errorReason;
	
	private Object tag;
	
    private SparseArray<Object> mKeyedTags;
    
    private DownloadExecutorTarget downloadTarget;
    
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	public int getErrorReason() {
		return errorReason;
	}

	public void setErrorReason(int errorReason) {
		this.errorReason = errorReason;
	}
	
	public long getDownloadLen() {
		return downloadLen;
	}

	public void setDownloadLen(long downloadLen) {
		this.downloadLen = downloadLen;
	}

	public long getTotalLen() {
		return totalLen;
	}

	public void setTotalLen(long totalLen) {
		this.totalLen = totalLen;
	}
	
	public String getDownloadId() {
		return downloadId;
	}

	public void setDownloadId(String id) {
		this.downloadId = id;
	}

	public Object getTag() {
        return tag;
    }

    public void setTag(final Object tag) {
        this.tag = tag;
    }
    
    public Object getTag(int key) {
        if (mKeyedTags != null) return mKeyedTags.get(key);
        return null;
    }
    
    public void setTag(int key, Object tag) {
        if (mKeyedTags == null) {
            mKeyedTags = new SparseArray<Object>();
        }

        mKeyedTags.put(key, tag);
    }

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	
	protected void setDownloadExecutorTarget(DownloadExecutorTarget target) {
		this.downloadTarget = target;
	}

	public DownloadExecutorTarget getDownloadExecutorTarget() {
		return this.downloadTarget;
	}
}

