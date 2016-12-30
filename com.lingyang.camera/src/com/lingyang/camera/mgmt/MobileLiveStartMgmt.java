package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.entity.MobileLiveResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: MobileLiveStartMgmt
 * 描    述: 手机直播开始
 * 创建人:杜舒
 * 创建时间: 2015/9
 */
public class MobileLiveStartMgmt extends BaseMgmt {
    public BaseCallBack<String> mBaseCallBack;
    Handler mHandler = new Handler();
    private String mType;
    private String mAddress;
    private String mMobileLiveTitle;

    public void getPushUrl(Context context, String camera_type,
                           String address, String title, BaseCallBack<String> callback) {
        mContext = context;
        mBaseCallBack = callback;
        mType = camera_type;
        mAddress = address;
        mMobileLiveTitle = title;
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected String getRequestUrl() {
        return String.format("%s/broadcast/begin", APP_REQUEST_URL);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            MobileLiveResponse info = (MobileLiveResponse) result;
            if (mBaseCallBack != null) {
                String push_addr = info.getData().push_addr;
                if (push_addr != null && !push_addr.equals("")) {
                    mBaseCallBack.success(push_addr);
                } else {
                    mBaseCallBack.error(null);
                }
            }
        } else {
            if (mBaseCallBack != null) {
                mBaseCallBack.error(null);
            }
        }
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put("address", mAddress);
        mPostParams.put("broadcast_name", mMobileLiveTitle);
        mPostParams.put("camera_type", mType);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(MobileLiveResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }
}
