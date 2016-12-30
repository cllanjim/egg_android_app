package com.lingyang.camera.entity;


public class BindCameraEntity extends BaseResponse {

    private static final long serialVersionUID = 1L;

    public BindEntity data;

    public BindEntity getData() {
        return data;
    }

    public static class BindEntity {
        public String cid;
//        public String cam_token;
    }

}
