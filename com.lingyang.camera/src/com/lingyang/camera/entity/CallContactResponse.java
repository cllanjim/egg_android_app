package com.lingyang.camera.entity;

import java.util.List;

/**
 * Created by LiaoLei on 2015/11/19.
 */
public class CallContactResponse extends BaseResponse {

    public List<CallContact> data;

    public List<CallContact> getData(){
        return data;
    }

    public static class CallContact{
        public String name;
        public String face_image;
        public String nickname;
        public String phonenumber;
    }

}
