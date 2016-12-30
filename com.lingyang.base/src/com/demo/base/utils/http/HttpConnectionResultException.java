package com.lingyang.base.utils.http;

import java.io.IOException;
import java.util.Map;

/**
 * [请求Result异常]
 * 请求ResponseCode不为200的时候发此异常
 */
public class HttpConnectionResultException extends IOException{

	private int errorCode;
	
	protected Map<String, String> resultMap;
	
	public HttpConnectionResultException(int errorCode) {
		super(errorCode+"");
		this.errorCode = errorCode;
	}
	
	@Override
	public String toString() {
		return "请求错误,返回码" + super.getMessage();
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public Map<String, String> getResultMap() {
		return resultMap;
	}
}

