package com.lingyang.camera.entity;

import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;

import java.util.Arrays;
import java.util.List;

public class PublicCameraResponse extends BaseResponse {

	private static final long serialVersionUID = 775402213164633275L;
	public Camera[] cameras;

	public List<Camera> getCameras() {
		return Arrays.asList(cameras);
	}
}
