package com.lingyang.camera.entity;

import java.io.Serializable;

/**
 * [下载对象]<BR>
 * 
 * @author 刘波
 */
public class DownloadStateIntent implements Serializable {

	private static final long serialVersionUID = 1L;

	public String uri;
	public long downloadLen;
	public long totalLen;
	public int progress;
	public int state;
	public String fileName;

	public DownloadStateIntent(String uri, long downloadLen, long totalLen, int progress,
			int state, String fileName) {
		super();
		this.uri = uri;
		this.downloadLen = downloadLen;
		this.totalLen = totalLen;
		this.progress = progress;
		this.state = state;
		this.fileName = fileName;
	}

}
