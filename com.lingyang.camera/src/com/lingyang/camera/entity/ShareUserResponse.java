package com.lingyang.camera.entity;

import java.util.List;

public class ShareUserResponse extends BaseResponse {

    private static final long serialVersionUID = 775402213164633275L;
    public List<ShareUser> data;

    public static class ShareUser {
        public String shared_uname;
        public String nickname;
        public String faceimage;
        public String phonenumber;
    }
}
