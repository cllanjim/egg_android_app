package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.BindCameraEntity;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: IncreaseOrDecreasePlayerPeopleMgmt
 * 描    述: 增加或减少在线人数
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class IncreaseOrDecreasePlayerPeopleMgmt extends BaseMgmt {

    public BaseCallBack<BaseResponse> mBaseCallBack;
    Handler mHandler;
    private String mCID;
    private boolean mIsIncrease;

    public void doExe(Context context, boolean isIncrease, String cid, BaseCallBack<BaseResponse> callback) {
        mContext = context;
        mCID = cid;
        mIsIncrease = isIncrease;
        mBaseCallBack = callback;
        if (mHandler == null) {
            mHandler = new Handler(context.getMainLooper());
        }
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            BaseResponse info = (BaseResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                if (mBaseCallBack != null) {
                    mBaseCallBack.success(null);
                }
            } else {
                mBaseCallBack.error(info.getError());
            }
        } else {
            if (mBaseCallBack != null) {
                mBaseCallBack.error(null);
            }
        }
    }

    @Override
    protected String getRequestUrl() {
        if (mIsIncrease) {
            return String.format("%s/camera/increase/total_watched_nums", APP_REQUEST_URL);
        }
        return String.format("%s/camera/decrease/total_watched_nums", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put(KEY_CID, mCID);
        Request request = new Request(getRequestUrl());
        request.setHttpHead(mHeadParams);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(BindCameraEntity.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }

}
