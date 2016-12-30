package com.lingyang.camera.preferences;

import com.lingyang.base.utils.BasePreference;

/**
 * @author �?
 */
public class MyPreference extends BasePreference {

    public static final int APP_VERSION = 0;
    public static final int LOGIN_STATE = 1;
    public static final int LOGIN_EMAIL = 2;
    public static final int LOGIN_PHONE = 3;
    public static final int LOGIN_PASSWORD = 4;
    public static final int SNAPSHOT_OF_COVER = 5;

    private static MyPreference mPreference;

    private MyPreference() {
    }

    public synchronized static MyPreference getInstance() {
        if (mPreference == null) {
            mPreference = new MyPreference();
        }
        return mPreference;
    }

    @Override
    protected String getPreferenceName() {
        return "lingyangcamera";
    }

    private String preferenceKey;
    public void setPreferenceKey(String preferenceKey){
        this.preferenceKey=preferenceKey;
    }

    @Override
    protected String getKey(int type) {
        switch (type) {
            case APP_VERSION:
                return "appversion";
            case LOGIN_STATE:
                return "LoginState";
            case LOGIN_EMAIL:
                return "login_email";
            case LOGIN_PHONE:
                return "login_phone";
            case LOGIN_PASSWORD:
                return "login_password";
            case SNAPSHOT_OF_COVER:
                if(preferenceKey!=null)
                return preferenceKey;
                else return "";
            default:
                return null;

        }

    }
}
