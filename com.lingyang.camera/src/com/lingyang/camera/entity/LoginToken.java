package com.lingyang.camera.entity;

public class LoginToken extends BaseResponse {

	private static final long serialVersionUID = -6678302458139835127L;
	public UserToken data;

	public UserToken getData() {
		return data;
	}

	public static class UserToken {
		public String access_token;//授权后的令牌
		public long expire;//token 过期时间戳
		public String uname;
		public String phonenumber;
		public String faceimage;//用户头像
		public String nickname;//用户昵称
		public String control;//
		public String init_string;//
		public String phone_connect_addr;//
		public String user_token;//
		public String user_token_expire;//
	}
}
