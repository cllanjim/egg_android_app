package com.antelope.sdk.capturer;

import com.antelope.sdk.service.ACService;

/**
* @author liaolei 
* @version 创建时间：2016年11月28日 
* 类说明
*/
public class ACImageResolution {
	public static final int R480=1;
	public static final int R720P=2;
	public static final int R1080P=3;
	
	private static class SingletonInstanceHolder {
		private static final ACImageResolution instance = new ACImageResolution();
	}
	
	public static ACImageResolution getInstance(){
		return SingletonInstanceHolder.instance;
	}
	
	public ACResolution getResolution(int r){
		if(r==R480){
			return Resolution480;
		}else if(r==R720P){
			return Resolution720;
		}else if(r==R1080P){
			return Resolution1080;
		}
		return null;
	}
	
	public static class ACResolution{
		public int width;
		public int height;
		
		public ACResolution(int w,int h){
			this.width=w;
			this.height=h;
		}
	}
	
	private static final ACResolution Resolution480 = new ACResolution(640, 480);
	private static final ACResolution Resolution720 = new ACResolution(1280, 720);
	private static final ACResolution Resolution1080 = new ACResolution(1920, 1080);
}
