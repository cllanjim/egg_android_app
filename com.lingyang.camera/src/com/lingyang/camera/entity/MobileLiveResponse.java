package com.lingyang.camera.entity;


public class MobileLiveResponse extends BaseResponse {

    public MobileLive data;

    public MobileLive getData() {
        return data;
    }

    public static class MobileLive{
        public String push_addr;
        public String rtmp_addr;
        public String total_watched_nums;
    }
}
