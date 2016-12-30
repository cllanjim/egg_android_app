package com.lingyang.camera.config;

import android.net.Uri;
import android.provider.MediaStore.Images;

public class Const {

    public static final String CPU_INFO_MAX_FREQ_FILE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
    // 屏幕锁住时间
    public static final long LOCK_SCREEN = 10000;
    /**
     * 默认云登录密码
     */
    public static final String CLOUD_DEFALUTPASSWORD = "ABCDEFG";
    /**
     * 默认云登录密码
     */
    public static final String DEFAULT_AP_PASSWORD = "cmsiermu2013";
    public static final String PACKAGE_NAME = "com.lingyang.camera";
    /**
     * TENCENT bugly report appid
     */
    public static final String TENCENT_REPORT_APPID = "900005407";
    /**
     * TENCENT bugly report appkey
     */
    public static final String TENCENT_REPORT_APPKEY = "15zFyK4ic2uiBmTp";
    public static final String TOPVDN_CLOUD_APPID = "Test";
//    public static final String APP_SERVER_HOST_IP = "223.202.103.135:8890";
    public static final String APP_SERVER_HOST_IP = "console.topvdn.com";
//    public static final String APP_SERVER_HOST = "http://122.226.181.250:80";//台州
//    public static final String APP_SERVER_HOST = "http://192.168.2.81:80";
//    public static final String APP_SERVER_HOST = "http://223.202.103.135:8899";
    public static final String APP_SERVER_HOST = "http://console.topvdn.com";
    public static final String TOPVDN_OFFICIAL_WEB  = "http://www.topvdn.com/index.html";
    public static final String APP_DOWNLOAD_URL  = "http://223.202.103.135:8066/utils/apk/newest";
    /**
     * 上传日志服务器
     */
    public static final String APP_SERVER_UPLOAD_LOG = String.format("%s/v1/%s/users/log/upload",
            Const.APP_SERVER_HOST, Const.TOPVDN_CLOUD_APPID);
    public static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
    public static final String IMAGE_MIME_TYPE = "image/jpeg";
    /**
     * httpclient相关配置
     */
    public static final String CLIENT_VERSION_HEADER = "User-Agent";
    public static final int TIMEOUT = 30;
    public static final String REFRESH_LIST_TIME = "refresh_list_time";
    // 缓冲区最小大小 单位M
    public static final long MINAVAILABLESPARE = 10L;
    // 录像时允许最小内存余量 单位M
    public static final long MINAVAILABLESPARE_RECORD = 200L;
    public static final String SSO_LOGIN_ACCOUNTS_KEY = "accounts";
    /**
     * 分段录像请求时长5分钟
     */
    public static final int REQUEST_RECORD_RANAGE = 60 * 5;
    public static final int PAGE_SIZE = 20;
    /**
     * 表示本地文件传输协议(file协议)
     */
    public static final String FILE_PROTOCOL = "file://";
    /**
     * setreul 的时候，附带数据，返回需要更新的是101，直接返回时100
     */
    public static final int SET_RESULT_REQUEST_CODE = 100;
    public static final int SET_RESULT_RESPONSE_CODE_ATTACH_DATA = 101;
    public static final int SET_RESULT_RESPONSE_CODE_NOT_DATA = 102;
    public static boolean DEBUG = true;
    public static boolean TEST_FLAG = false;
    public static String USER_QID = "";
    /**
     * TODO 是否需要邮箱注册 默认为需要邮箱注册，当不需要邮箱注册时，可以设置下列值
     * Constant.VALUE_ADD_ACCOUNT_HAS_EMAIL //需要邮箱注册
     * Constant.VALUE_ADD_ACCOUNT_NO_EMAIL //不需要邮箱注册
     */
    public static int ACCOUNT_REGISTER_NEED_EMAIL;
    /**
     * TODO 帐号注册类型 默认为注册邮箱需要激活，当不需要激活时，可以设置相应的值
     * Constant.VALUE_ADD_ACCOUNT_EMAIL_REGISTER //邮箱注册，不需要激活
     * Constant.VALUE_ADD_ACCOUNT_EMAIL_REGISTER_ACTIVE //邮箱注册，需要激活
     */
    public static int ACCOUNT_REGISTER_EMAIL_ACTIVE;

   public static int SOUNDWAVE_FREQUENCY[] = {8000, 8200, 8400, 8600, 8800, 9000, 9200, 9400, 9600,
           9800, 10000, 10200, 10400, 10600, 10800, 11000, 11200, 11400, 11600  };
//   public static int SOUNDWAVE_FREQUENCY[] = { 12000, 12200, 12400, 12600, 12800, 13000, 13200,
//            13400, 13600, 13800, 14000, 14200, 14400, 14600, 14800, 15000,
//            15200, 15400, 16600 };
//   public static int SOUNDWAVE_FREQUENCY[] = { 12000, 12200, 12400, 12600, 12800, 13000, 13200,
//            13400, 13600, 13800, 14000, 14200, 14400, 14600, 14800, 15000,
//            15200, 15400, 16600 };

    /**
     * @author 波 intent action
     */
    public static class Actions {

        // Actions
        public static final String ACTION_ACTIVITY_LOGIN = "com.lingyang.camera.ui.activity.LoginActivity";
        public static final String ACTION_ACTIVITY_MAIN = "com.lingyang.camera.ui.activity.MainActivity";
        public static final String ACTION_ACTIVITY_REGISTER = "com.lingyang.camera.ui.activity.RegistActivity";
        public static final String ACTION_ACTIVITY_MYRECORD = "com.lingyang.camera.ui.activity.MyRecordActivity";
        public static final String ACTION_ACTIVITY_LIVE = "com.lingyang.camera.ui.activity.LiveActivity";
        public static final String ACTION_ACTIVITY_RECORDPLAY = "com.lingyang.camera.ui.activity.RecordPlayerActivity";
        public static final String ACTION_ACTIVITY_SHARE = "com.lingyang.camera.ui.activity.ShareActivity";
        public static final String ACTION_ACTIVITY_BINDCAMERA = "com.lingyang.camera.ui.activity.BindCameraActivity";
        public static final String ACTION_ACTIVITY_ME = "com.lingyang.camera.ui.activity.MeActivity";
        public static final String ACTION_ACTIVITY_SETTING = "com.lingyang.camera.ui.activity.SettingActivity";
        public static final String ACTION_ACTIVITY_SETTING_WIFI = "com.lingyang.camera.ui.activity.CameraSettingsWifiActivity";
        public static final String ACTION_ACTIVITY_SETTING_WIFI_ADDCAMERA = "com.lingyang.camera.ui.activity.AddCameraActivity";
        public static final String ACTION_ACTIVITY_FINDPASSWORD = "com.lingyang.camera.ui.activity.FindPasswordActivity";
        public static final String ACTION_SERVICE_UPGRADE = "com.lingyang.camera.service.UpgradeService";
        public static final String ACTION_BROADCAST_DOWNLOAD = "tvbox.settings.ACTION_DOWNLOADBROADCAST";
        public static final String ACTION_ACTIVITY_FIRST_OF_ADD_DEVICE = "com.lingyang.camera.ui.activity.FirstOfAddDeviceActivity";
        public static final String ACTION_ACTIVITY_SECOND_OF_ADD_DEVICE = "com.lingyang.camera.ui.activity.SecondOfAddDeviceActivity";
        public static final String ACTION_ACTIVITY_THIRD_OF_ADD_DEVICE = "com.lingyang.camera.ui.activity.ThirdOfAddDeviceActivity";
        public static final String ACTION_ACTIVITY_SOUNDWAVE = "com.lingyang.camera.ui.activity.VoiceBindingActivity";
        public static final String ACTION_ACTIVITY_FOURTH_OF_ADD_DEVICE = "com.lingyang.camera.ui.activity.FourthOfAddDeviceActivity";
        public static final String ACTION_ACTIVITY_USER_INFO = "com.lingyang.camera.ui.activity.UserInfoActivity";
        public static final String ACTION_ACTIVITY_EDIT_NICKNAME = "com.lingyang.camera.ui.activity.EditNickNameActivity";
        public static final String ACTION_ACTIVITY_MY_FILE_LIST = "com.lingyang.camera.ui.activity.MyFileListActivity";
        public static final String ACTION_ACTIVITY_MY_ATTENTION_LIST = "com.lingyang.camera.ui.activity.MyAttentionActivity";
        public static final String ACTION_ACTIVITY_ABOUT = "com.lingyang.camera.ui.activity.AboutActivity";
        public static final String ACTION_ACTIVITY_SET = "com.lingyang.camera.ui.activity.SetActivity";
        public static final String ACTION_ACTIVITY_SET_DEFINITIONG_SELECTOR = "com.lingyang.camera.ui.activity.SetDefinitionSelectorActivity";
        public static final String ACTION_ACTIVITY_PLAYER = "com.lingyang.camera.ui.activity.PlayerActivity";
        public static final String ACTION_ACTIVITY_USE_MOBILE_LIVE = "com.lingyang.camera.ui.activity.PrepareMobileLiveActivity";
        public static final String ACTION_ACTIVITY_USE_MOBILE_LIVE2 = "com.lingyang.camera.ui.activity.MobileLiveActivity";
        public static final String ACTION_ACTIVITY_MOBILE_LIVE_PLAYER = "com.lingyang.camera.ui.activity.MobilePlayerActivity";
        public static final String ACTION_ACTIVITY_MOBILE_INTERCONNECT = "com.lingyang.camera.ui.activity.MobileInterconnectActivity";
        public static final String ACTION_ACTIVITY_CONTACTS = "com.lingyang.camera.ui.activity.ContactsActivity";
        public static final String ACTION_ACTIVITY_CALLED = "com.lingyang.camera.ui.activity.CalledActivity";
        public static final String ACTION_ACTIVITY_REGIST_AND_RESET_PWD = "com.lingyang.camera.ui.activity.RegistAndResetPasswordActivity";
        public static final String ACTION_SERVICE_BOOTSTART = "com.lingyang.camera.service.BootStartService";
        public static final String ACTION_ACTIVITY_MYTEST = "com.lingyang.camera.ui.activity.MyTestActivity";
        public static final String ACTION_ACTIVITY_PUBLIC_CAMERA = "com.lingyang.camera.ui.activity.PublicCameraActivity";
        public static final String ACTION_ACTIVITY_SEARCH = "com.lingyang.camera.ui.activity.SearchCameraActivity";


        //绑定/解绑刷新
        public static final String ACTION_IS_BIND_REFRESH = "action_is_bind_refresh";
        //关注/取关刷新
        public static final String ACTION_IS_ATTENTION_REFRESH = "action_is_attention_refresh";
        //设为公众/私有刷新
        public static final String ACTION_IS_PUBLIC_REFRESH = "action_is_public_refresh";

        public static final String ACTION_NET_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
        //手机消息广播
        public static final String ACTION_MOBILE_MESSAGE = "com.lingyang.camera.MobileMsg";

        public static final String ACTION_DEVICE_BIND_CONFIRM = "com.lingyang.camera.DeviceBindConfirm";


        public static final String ACTION_MSG_POPCONFIGRESULT = "com.lingyang.camera.popconfigresult";
        public static final String ACTION_MSG_REFRESH_STATE = "com.lingyang.camera.refreshstate";
        public static String ACTION_REFRESH_CONTACTS_BROADCAST = "action_refresh_contacts_broadcast";
    }

    public static class MapKeyConst {

        public static final String KEY_CID = "CID";
        public static final String KEY_UID = "UID";

    }

    public static class IntentKeyConst {
        public static final String KEY_CAMERA = "KEY_CAMERA";
        /**
         * 是否直接从登录页跳转主页
         */
        public static final String KEY_ISFIRSTLOGIN = "ISFIRSTLOGIN";
        public static final String KEY_CID = "CID";
        public static final String KEY_CNAME = "CNAME";
        public static final String KEY_CAMERA_TYPE = "CTYPE";
        public static final String KEY_SET_IS_UNBIND = "SET_ISUNBIND";
        public static final String KEY_NICKNAME = "NICKNAME";
        public static final String KEY_CAMERA_OWNER_NICKNAME = "CAMERA_OWNER_NICKNAME";
        public static final String KEY_CAMERA_COVER = "CAMERA_COVER";
        public static final String KEY_LIVE_SHARE = "LIVE_SHARE";
        public static final String KEY_LIVE_RTMP = "LIVE_RTMP";
        public static final String KEY_LIVE_TYPE = "LIVE_TYPE";
        public static final String KEY_DOWNSTATE = "INTENT_KEY_DOWNSTAGE";
        public static final String KEY_PLAYTYPE = "KEY_PLAYTYPE";
        public static final String KEY_ADDRESS = "KEY_ADDRESS";
        public static final String KEY_ONLINENUM = "KEY_ONLINENUM";
        public static final String KEY_VIEWTIMES = "KEY_VIEWTIMES";
        public static final String KEY_DOWNLOADSERVICE_MD5 = "KEY_DOWNLOADSERVICE_MD5";
        public static final String KEY_DOWNLOADSERVICE_FILENAME = "KEY_DOWNLOADSERVICE_FILENAME";
        public static final String KEY_DOWNLOADSERVICE_DOWNLOADURL = "KEY_DOWNLOADSERVICE_DOWNLOADURL";
        //
        public static final String KEY_CAMERA_RATE = "CAMERA_RATE";
        public static final String KEY_EDIT_FORM_WHERE = "KEY_EDIT_FORM_WHERE";
        public static final String KEY_PHONE_NUMBER = "KEY_PHONE_NUMBER";
        public static final int EDIT_FROM_CAMERA = 1;
        public static final int EDIT_FROM_USER = 2;

        public static final String KEY_ISBIND = "ISBIND";
        public static final String KEY_EMAIL = "EMAIL";
        public static final String KEY_SN = "sn";
        public static final String KEY_FILE_TYPE = "type";
        public static final String KEY_WIFI = "wifi";

        //广播刷新我的列表
        public static final String KEY_IS_ATTENTION = "IS_ATTENTION";
        public static final String KEY_FROM_WHERE = "REFRESH_FROM_WHERE";
        public static final int REFRESH_FROM_SET = 1;
        public static final int REFRESH_FROM_LIVE = 2;
        public static final int REFRESH_FROM_MYATTENTION = 3;
        public static final int REFRESH_FROM_PUBLIC = 4;
        public static final int REFRESH_FROM_ATTENTION = 5;
        public static final int REFRESH_FROM_PLAYER = 6;
        public static final int REFRESH_FROM_BIND = 7;
        public static final int REFRESH_FROM_UNSHARE = 8;
        public static final int REFRESH_FROM_SHARE = 9;
        public static final int REFRESH_FROM_MOBILE_INTERCONNECTION = 10;
        public static final int REFRESH_FROM_CALL_ACTIVITY = 11;


        public static final String KEY_P2P_URL = "KEY_P2P_URL";
        public static final String KEY_CLOSE_PLAY_AND_LIVE= "KEY_CLOSE_PLAY_AND_LIVE";
        //互联自定义消息
        public static final String KEY_VIDEO_CALL = "CALL";
        public static final String KEY_CONNECT_FAIL = "FAIL";
        public static final String KEY_CONNECT_REFUSE = "REFUSE";
        public static final String KEY_CONNECT_HANG_UP = "HANG_UP";
        public static final String KEY_SESSION_ID = "SESSION_ID";
        public static final String KEY_NO_ANSWER = "NO_ANSWER";
        public static final String KEY_ANOTHER_CALL = "ANOTHER_CALL";
        public static final String KEY_LINE_IS_BUSY = "BUSY";
        public static final String KEY_SEND_BUSY = "KEY_SEND_BUSY";

        public static final String KEY_DELETE_CAMERA = "DELETE_CAMERA";
        public static final String KEY_SHARE_CAMERA = "SHARE_CAMERA";
        public static final String KEY_SHARE_OR_UNSHARE_CAMERA = "KEY_SHARE_OR_UNSHARE_CAMERA";


        public static final String KEY_CONNECTION_MSG = "KEY_CONNECTION_MSG";
        public static final String KEY_CONNECTION_ACCEPTED = "ConnectionAcceptted";
        public static final String KEY_CONNECTION_CLOSED = "ConnectionClosed";
        public static final String KEY_MOBILE_MSG = "KEY_MOBILE_MSG";
        public static final String KEY_MOBILE_MESSAGE = "KEY_MOBILE_MESSAGE";
        public static final String KEY_UN_SHARE_CAMERA = "KEY_UN_SHARE_CAMERA";
        public static final String KEY_FROM_OTHER = "FROM_OTHER";

        public static final String KEY_COME_FEOM_WHERE = "COME_FEOM_WHERE_TO_REGIST_AND_RESET_PWD";

        public static final String KEY_MSG_DEVICE_BIND_CONFIRM = "KEY_MSG_DEVICE_BIND_CONFIRM";
        public static final String KEY_MSG_POPCONFIGRESULT = "KEY_MSG_POPCONFIGRESULT";
        public static final String KEY_MSG_REFRESH_STATE = "KEY_MSG_REFRESH_STATE";
        public static String KEY_ONLINE_USER_LIST="KEY_ONLINE_USER_LIST";
    }
}
