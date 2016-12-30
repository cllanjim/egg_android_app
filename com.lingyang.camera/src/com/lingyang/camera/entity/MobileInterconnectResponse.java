package com.lingyang.camera.entity;

import com.google.gson.Gson;

import java.io.Serializable;

public class MobileInterconnectResponse extends BaseResponse {
    private static final long serialVersionUID = 1L;
    public Mobile data;

    public Mobile getData() {
        return data;
    }

    public static class Mobile implements Serializable {
//        public static final int TYPE_IDLE = 0;
//        public static final int TYPE_BUSY = 1;
//        public int state;//0 正常  1  通话中
        public String nickname = "";
        public String p2pUrl = "";
        public String phoneNumber = "";
        public String sid = "";
        public String message = "";
        public String uid = "";

        @Override
        public String toString() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }
}
