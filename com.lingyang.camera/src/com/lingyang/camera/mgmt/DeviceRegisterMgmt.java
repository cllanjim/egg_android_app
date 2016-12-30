package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.BindCameraEntity;
import com.lingyang.camera.entity.BindCameraEntity.BindEntity;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: DeviceRegisterMgmt
 * 描    述: 设备注册
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class DeviceRegisterMgmt extends BaseMgmt {
    private String mSn;
    private BaseCallBack<BindEntity> callBack;
    private Runnable mRequestRunnable = new Runnable() {
        @Override
        public void run() {
            CLog.v("regist request0");
            request();
        }
    };

    public void register(Context context, String sn, BaseCallBack<BindEntity> callBack) {
        mContext = context;
        this.mSn = sn;
        this.callBack = callBack;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        CLog.v("register result---" + result);
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            BindCameraEntity info = (BindCameraEntity) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                if (callBack != null) {
                    callBack.success(info.getData());
                }
            } else {
                if (callBack != null) {
                    callBack.error(info.getError());
                }
            }
        } else {
            if (callBack != null) {
                callBack.error(null);
            }
        }
    }

    @Override
    protected String getRequestUrl() {
        return String.format("%s/camera/register", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put(KEY_CSN, mSn);
        mPostParams.put(KEY_CNAME, "sadfasdfsd");
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(BindCameraEntity.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }

}
