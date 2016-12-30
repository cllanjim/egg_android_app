package com.lingyang.camera.entity;


public class UserInfo extends BaseResponse{

	private static final long serialVersionUID = 1L;
	
	public userInfo data;
	
	public  userInfo getData(){
		return data;
	}
	
	public static class userInfo{
		public String nickname;
		public String uid;
		public String faceimage;
		public String toString() {
			return "userInfo [nickname=" + nickname + ", uid=" + uid
					+ ", faceimage=" + faceimage + "]";
		}
		
	}
	
	

}
