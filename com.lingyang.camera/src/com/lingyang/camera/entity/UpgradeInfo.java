package com.lingyang.camera.entity;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * [app升级]<br/>
 * 
 * @author 刘波
 */
public class UpgradeInfo extends BaseResponse {

	private static final long serialVersionUID = -4556030505398975397L;
	public Info data;

	public Info getData() {
		return data;
	}

	public static class Info {

		public String name;
		public boolean upgrade;
		public boolean force_upgrade;
		@JsonProperty("version_name")
		public String versionname;
		@JsonProperty("version_code")
		public int versionCode;
		@JsonProperty("version_date")
		public String versiondate;
		public String url;
		public String readme;
		public String md5;
	}

}
