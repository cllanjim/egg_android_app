package com.lingyang.camera.entity;


public class ChangeNickNameEntity extends BaseResponse {

    private static final long serialVersionUID = 1L;
    public NickNameCls data;

    public NickNameCls getData() {
        return data;
    }

    public static class NickNameCls {
        public String nickname;
    }

}
