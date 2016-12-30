package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.entity.MobileLiveResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: MobileLiveStopMgmt
 * 描    述: 手机直播结束
 * 创建人:杜舒
 * 创建时间: 2015/9
 */
public class MobileLiveStopMgmt extends BaseMgmt {
    public BaseCallBack<String> mBaseCallBack;
    Handler mHandler = new Handler();
    private String mAddress;

    public void stopMobileLive(Context context, String address, BaseCallBack<String> callback) {
        mContext = context;
        mBaseCallBack = callback;
        mAddress = address;
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected String getRequestUrl() {
        return String.format("%s/broadcast/stop", APP_REQUEST_URL);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            MobileLiveResponse info = (MobileLiveResponse) result;
            MobileLiveResponse.MobileLive data = info.getData();
            if (mBaseCallBack != null) {
                if (data != null) {
                    CLog.v("::::success:::" + data.total_watched_nums);
                    mBaseCallBack.success(data.total_watched_nums + "");
                }else {
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
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(MobileLiveResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }
}
