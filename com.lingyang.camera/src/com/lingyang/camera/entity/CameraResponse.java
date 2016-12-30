package com.lingyang.camera.entity;


import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

public class CameraResponse extends BaseResponse {

    private static final long serialVersionUID = 8410472694113535938L;
    public MyCameras data;

    public MyCameras getData() {
        return data;
    }

    public static class MyCameras implements Serializable {

        public Camera[] mine;
        public Camera[] share_to_me;
        @JsonProperty("public")
        public Camera[] public_cameras;

        public static class Camera implements Serializable {

            public static final int TYPE_PRIVATE = 0;
            public static final int TYPE_PUBLIC = 1;
            public static final int CAMERA_TYPE_ALL = 0;
            public static final int CAMERA_TYPE_CAMERA = 1;
            public static final int CAMERA_TYPE_MOBILE = 2;
            //                离线：              离线     ０
//                纯私有无推流：      在线           100
//                纯私有推流，不可连：工作中       101
//                RTMP 私有推流：    在线           100
//                RTMP 公众推流：    直播中    102
//                中间不可用状态：    未就绪       103
//                修改配置：          配置中    104
            public static final int DEVICE_STATUS_OFFLINE = 0;
            public static final int DEVICE_STATUS_PREPARED = 100;
            public static final int DEVICE_STATUS_WORKING = 101;
            public static final int DEVICE_STATUS_LIVING = 102;
            public static final int DEVICE_STATUS_UNPREPARED = 103;
            public static final int DEVICE_STATUS_CONFIGURING = 104;


            public boolean is_online;
            public String cid = "";
            public int state;
            public String uname = "";
            public String cname = "";
            public String cover_url = "";
            public String nickname = "";
            public String rtmp_addr = "";
            public String play_addr = "";
            public int shared;
            public int followed;
            public boolean is_followed;
            public String address = "";
            public String faceimage = "";
            public int total_watched_nums;
            public int online_nums;
            public int camera_type;// 0 全部   1 fixed_    2 mobile_
            public String camera_label;//摄像机厂家或手机类型
            public boolean isConfiging;
            public String hls;
            /**
             * {@link #TYPE_PUBLIC} OR {@link #TYPE_PRIVATE}
             */
            public int type;
            private CameraOwner cameraOwner;

            public CameraOwner getCameraOwner() {
                return cameraOwner;
            }

            public void setCameraOwner(CameraOwner ownertype) {
                cameraOwner = ownertype;
            }

            public String getStatus(int status) {
//                 //                离线：              离线     ０
//                纯私有无推流：      在线           100
//                纯私有推流，不可连：工作中       101
//                RTMP 私有推流：    在线           100
//                RTMP 公众推流：    直播中    102
//                中间不可用状态：    未就绪       103
//                修改配置：          配置中    104
//                public static final int DEVICE_STATUS_OFFLINE = 0;
//                public static final int DEVICE_STATUS_PREPARED = 100;
//                public static final int DEVICE_STATUS_WORKING = 101;
//                public static final int DEVICE_STATUS_LIVING = 102;
//                public static final int DEVICE_STATUS_UNPREPARED = 103;
//                public static final int DEVICE_STATUS_CONFIGURING = 104;
                switch (status) {
                    case DEVICE_STATUS_OFFLINE:
                        return "离线";
                    case DEVICE_STATUS_PREPARED:
                        return "在线";
                    case DEVICE_STATUS_LIVING:
                        return "直播中";
                    case DEVICE_STATUS_UNPREPARED:
                        return "未就绪";
                    case DEVICE_STATUS_WORKING:
                        return "工作中";
                    case DEVICE_STATUS_CONFIGURING:
                        return "配置中";
                    default:
                        return "离线";
                }
            }

            public boolean getIsOnline() {
                return is_online;
            }

            public void setIsOnline(boolean isOnline) {
                is_online = isOnline;
            }

            public String getCName() {
                if (cname == null) {
                    return "";
                }
                return cname;
            }

            /**
             * 摄像机所有者类型
             */
            public static enum CameraOwner {
                CAMERA_MINE(1), CAMERA_SHARA_TO_ME(2), CAMERA_PUBLIC(3);

                private int value;

                CameraOwner(int value) {
                }

                public int getValue() {
                    return value;
                }
            }
        }

    }


}
