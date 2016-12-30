package com.lingyang.camera.entity;


import com.lingyang.camera.R;


public class GetCameraSetResponse extends BaseResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CameraSet data;

	public CameraSet getData() {
		return data;
		
	}

	/**
	 * rate; 1:1M码率（高清） 2:500K码率（标清） 3：300K码率（流畅）
	 *  silence; 是否开启音频 0：关闭音频，1:打开音频
	 * camera_type; 0:私有协议，2:公众摄像头，3:云存储摄像头，4:公众云存储摄像头
	 * 
	 */
	public static class CameraSet {
		public int rate;
		public int silence;
		public int camera_type;
		
		public static final int RATE_HD=1;
		public static final int RATE_SD=2;
		public static final int RATE_FLUENT=3;
		public static final int SILENCE_FALSE=0;
		public static final int SILENCE_TRUE=1;
		public static final int TYPE_PRIVATE=0;
		public static final int TYPE_PUBLIC=2;
		public static final int TYPE_CLOUD=3;
		public static final int TYPE_PUBLIC_AND_CLOUD=4;
		
		

		public int getRate() {
			int resource = R.string.set_null;
			switch (rate) {
			case 1:
				resource=R.string.set_definition_hd;
				break;
			case 2:
				resource=R.string.set_definition_sd;
				break;
			case 3:
				resource=R.string.set_definition_fluent;
				break;

			default:
				break;
			}
			return resource;
		}
		
		public boolean isSilence(){
			return silence==1?true:false;
		}
		
		public boolean isPublic(){
			if(camera_type==2||camera_type==4){
				return true;
			}
			return false;
		}
		
		public boolean  isCloud(){
			if(camera_type==3||camera_type==4){
				return true;
			}
			return false;
		}

	}

}
