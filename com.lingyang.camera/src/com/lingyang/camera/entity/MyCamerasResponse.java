package com.lingyang.camera.entity;

import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;

import java.util.List;

public class MyCamerasResponse extends BaseResponse {

    private static final long serialVersionUID = 1L;
    public List<Camera> data;

    public List<Camera> getData() {
        return data;
    }
}
