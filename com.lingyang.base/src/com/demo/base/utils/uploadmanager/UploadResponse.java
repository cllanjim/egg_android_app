package com.lingyang.base.utils.uploadmanager;

import com.lingyang.base.utils.http.UploadRequest.UploadFileBean;

import java.util.ArrayList;
import java.util.List;

public class UploadResponse {
    public List<UploadFileBean> fileList = new ArrayList<UploadFileBean>();
    public int state;
    public long fileTotalSize;
    public long fileUploadSize;
	public int uploadRate;
}
