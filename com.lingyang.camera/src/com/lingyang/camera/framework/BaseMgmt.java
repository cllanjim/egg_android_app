package com.lingyang.camera.framework;

import android.content.Context;

import com.lingyang.base.utils.http.OnRequestListener;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.exception.LogUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseMgmt {

    public static final String KEY_CID = "cid";
    public static final String KEY_UNAME = "uname";
    public static final String KEY_EXPIRE = "expire";
    public static final String KEY_TOKEN = "access_token";
    public static final String KEY_CNAME = "cname";
    public static final String KEY_CSN = "sn";
    public final String APP_REQUEST_URL = String.format("%s/v1/%s",
            Const.APP_SERVER_HOST, Const.TOPVDN_CLOUD_APPID);

    //    public final String APP_REQUEST_URL = String.format("%s/app",
//            Const.APP_SERVER_HOST);
    public OnRequestListener mRequestListener = new OnRequestListener() {
        @Override
        public void onResponse(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
            long responseTime = System.currentTimeMillis();
            LogUtil.getInstance().saveHttpLog2File(mRequestStartTime,
                    responseTime,
                    request,
                    state,
                    request.getRequestTime(),
                    headMap,
                    mHeadParams,
                    mPostParams,
                    mUrlParams);
            response(url, state, result, type, request, headMap);
        }
    };
    protected Context mContext;
    protected Map<String, String> mHeadParams;
    protected Map<String, String> mPostParams;
    protected Map<String, String> mUrlParams;
    protected Request request;
    protected Runnable mRequestRunnable = new Runnable() {

        @Override
        public void run() {
            mRequestStartTime = System.currentTimeMillis();
            request();
        }
    };
    private long mRequestStartTime;

    public BaseMgmt() {
        request = new Request(getRequestUrl());
        mHeadParams = new HashMap<String, String>();
        mHeadParams.put("User-Agent", "Dalvik/1.6.0 (Linux; U; Android 4.3; GT-I9308 Build/JSS15J)");
        mHeadParams.put("accept-encoding", "gzip");
        request.setHttpHead(mHeadParams);
    }

    protected abstract String getRequestUrl();

    protected abstract void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap);

    protected void addValidPostParams() {
        mPostParams = new HashMap<String, String>();
        mPostParams.put(KEY_UNAME, LocalUserWrapper.getInstance().getLocalUser().getUid());
        mPostParams.put(KEY_EXPIRE, LocalUserWrapper.getInstance().getLocalUser().getExpire() + "");
        mPostParams.put(KEY_TOKEN, LocalUserWrapper.getInstance().getLocalUser().getAccessToken());
        request.setPostData(mPostParams);
    }

    protected void addValidUrlParams() {
        mUrlParams = new HashMap<String, String>();
        mUrlParams.put(KEY_UNAME, LocalUserWrapper.getInstance().getLocalUser().getUid());
        mUrlParams.put(KEY_EXPIRE, LocalUserWrapper.getInstance().getLocalUser().getExpire() + "");
        mUrlParams.put(KEY_TOKEN, LocalUserWrapper.getInstance().getLocalUser().getAccessToken());
        request.setUriParam(mUrlParams);
    }

    protected boolean addUserToken() {
        mHeadParams.put("X-User-Token", LocalUserWrapper.getInstance().getLocalUser().getUserToken());
        return true;
    }

    protected boolean addUserTokenAndDeviceToken(String deviceID) {
        mHeadParams.put("X-User-Token", LocalUserWrapper.getInstance().getLocalUser().getUserToken());
//        mHeadParams.put("X-Access-Token", CloudOpenAPI.getInstance().getDeviceAccessToken(deviceID));
        return true;
    }

    protected abstract void request();

}
