package com.lingyang.base.utils.http;

import com.lingyang.base.utils.uploadmanager.OnUploadRequestListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UploadRequest extends Request {

	private List<UploadFileBean> uploadFileList;

	private boolean isMulti = false;

	public boolean isMulti() {
		return isMulti;
	}

	public void setMulti(boolean isMulti) {
		this.isMulti = isMulti;
	}

	public List<UploadFileBean> getUploadFileList() {
		return uploadFileList;
	}

	public void setUploadFileList(List<UploadFileBean> uploadFileList) {
		this.uploadFileList = uploadFileList;
	}
	
	public void setUploadFile(String filePath) {
		this.uploadFileList = new ArrayList<UploadFileBean>();
		UploadFileBean bean = new UploadFileBean();
		bean.setFilePath(filePath);
		uploadFileList.add(bean);
	}

	public static class UploadFileBean {
	    String id;
		String filePath;
		String name;
		Map<String, String> headMap;
		
		public String getFilePath() {
			return filePath;
		}
		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
		public Map<String, String> getHeadMap() {
			return headMap;
		}
		public void setHeadMap(Map<String, String> headMap) {
			this.headMap = headMap;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
		
	}
	
	private OnUploadRequestListener onUploadRequestListener;
	public void setOnUploadRequestListener(OnUploadRequestListener onUploadRequestListener) {
        this.onUploadRequestListener = onUploadRequestListener;
    }
	public OnUploadRequestListener getOnUploadRequestListener() {
        return onUploadRequestListener;
    }
}

