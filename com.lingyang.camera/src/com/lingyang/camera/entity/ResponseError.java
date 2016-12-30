package com.lingyang.camera.entity;

import java.io.Serializable;

public class ResponseError implements Serializable {

	private static final long serialVersionUID = 7474058941545480471L;
	public String error_code;
	public String error_msg;

	public void setMsg(String error_msg) {
		this.error_msg = error_msg;
	}

	public void setErrorCode(String error_code) {
		this.error_code = error_code;
	}

}
