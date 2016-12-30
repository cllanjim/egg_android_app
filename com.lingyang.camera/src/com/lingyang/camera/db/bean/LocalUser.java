package com.lingyang.camera.db.bean;

import com.google.gson.Gson;

/**
 * 文件名：LocalUser
 * 描述：
 * 此类是本地个人信息实体类
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class LocalUser {

    private String nickName;
    private String head;

    private String uid;
    private String accessToken;
    private Long expire;
    private String mobile;
    private String IP;
    private String password;
    private long lastLoginTime;
    private String control;
    private String initString;
    private String phoneConnectAddr;
    private String userToken;
    private String userTokenExpire;

    public LocalUser(String name, String head, String password, String ip, String mobile,
                     Long expire, String uid, String access_token,
                     String control,
                     String init_string,
                     String phone_connect_addr,
                     String user_token,
                     String user_token_expire) {
        nickName = name;
        this.head = head;
        this.mobile = mobile;
        this.password = password;

        IP = ip;
        this.expire = expire;
        this.uid = uid;
        accessToken = access_token;
        lastLoginTime = System.nanoTime();

        this.control = control;
        initString = init_string;
        phoneConnectAddr = phone_connect_addr;
        userToken = user_token;
        userTokenExpire = user_token_expire;
    }

    public static String GetPasswordString() {
        return "Password";
    }

    public static String GetUidString() {
        return "Uid";
    }

    public static String getAccessTokenString() {
        return "AccessToken";
    }

    public static String getExpireString() {
        return "Expire";
    }

    public static String getNickNameString() {
        return "NickName";
    }

    public static String getHeadString() {
        return "Head";
    }

    public static String getEmailString() {
        return "Email";
    }

    public static String getIsBindEmailString() {
        return "IsBindEmail";
    }

    public static String getMobileString() {
        return "Mobile";
    }

    public static String getIPString() {
        return "ip";
    }

    public static String getLastLoginTimeString() {
        return "LastLoginTime";
    }

    public static String getControlString() {
        return "Control";
    }

    public static String getInitStringString() {
        return "InitString";
    }

    public static String getPhoneConnectAddrString() {
        return "PhoneConnectAddr";
    }

    public static String getUserTokenString() {
        return "UserToken";
    }

    public static String getUserTokenExpireString() {
        return "UserTokenExpire";
    }


    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getInitString() {
        return initString;
    }

    public void setInitString(String initString) {
        this.initString = initString;
    }

    public String getPhoneConnectAddr() {
        return phoneConnectAddr;
    }

    public void setPhoneConnectAddr(String phoneConnectAddr) {
        this.phoneConnectAddr = phoneConnectAddr;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getUserTokenExpire() {
        return userTokenExpire;
    }

    public void setUserTokenExpire(String userTokenExpire) {
        this.userTokenExpire = userTokenExpire;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void setUId(String mUId) {
        this.uid = mUId;
    }

    public void setIP(String ip) {
        this.IP = ip;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String mNickName) {
        this.nickName = mNickName;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String mHead) {
        this.head = mHead;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String mAccessToken) {
        this.accessToken = mAccessToken;
    }

    public void setMobile(String mMobile) {
        this.mobile = mMobile;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long mExpire) {
        this.expire = mExpire;
    }

    public String getUid() {
        return uid;
    }

    public String getMobile() {
        return mobile;
    }

    public String getIP() {
        return IP;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long mLastLoginTime) {
        this.lastLoginTime = mLastLoginTime;
    }


}
