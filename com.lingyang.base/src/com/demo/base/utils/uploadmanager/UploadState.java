package com.lingyang.base.utils.uploadmanager;

import com.lingyang.base.utils.downloadmanager.KeyConstants;


public class UploadState {
    
    public final static int STATE_FAIL = KeyConstants.DOWNLOAD_STATE_FAIL;
    public final static int STATE_SUC = KeyConstants.DOWNLOAD_STATE_SUC;
    public final static int STATE_PENDING = KeyConstants.DOWNLOAD_STATE_PENDING;
    public final static int STATE_INTO_UPLOADING_QUEUE = KeyConstants.DOWNLOAD_STATE_INTO_DOWNLOADING_QUEUE;
    public final static int STATE_PREPARE = KeyConstants.DOWNLOAD_STATE_PREPARE;
    public final static int STATE_START = KeyConstants.DOWNLOAD_STATE_START;
    public final static int STATE_UPLOADING = KeyConstants.DOWNLOAD_STATE_DOWNLOADING;
    public final static int STATE_PAUSE = KeyConstants.DOWNLOAD_STATE_PAUSE;
    public final static int STATE_NONE = KeyConstants.DOWNLOAD_STATE_NONE;

}
