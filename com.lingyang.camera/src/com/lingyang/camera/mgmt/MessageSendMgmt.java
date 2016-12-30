package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.entity.MobileInterconnectResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.Map;

/**
 * 文件名: MobileInterconnectMgmt
 * 描    述: 视频通话发送消息
 * 创建人:杜舒
 * 创建时间: 2015/9
 */
public class MessageSendMgmt extends BaseMgmt {
    public BaseCallBack<String> mBaseCallBack;
    Handler mHandler = new Handler();
    private String mMassage;
    private String mPhoneNumber;

    public void sendMessage(Context context, String message, String phoneNumber, BaseCallBack<String> callback) {
        mContext = context;
        mBaseCallBack = callback;
        mMassage = message;
        mPhoneNumber = phoneNumber;
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            MobileInterconnectResponse info = (MobileInterconnectResponse) result;
            MobileInterconnectResponse.Mobile data = info.getData();
            if (mBaseCallBack != null && data != null) {
                mBaseCallBack.success("success");
            } else {
                if (mBaseCallBack != null) {
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
    protected String getRequestUrl() {
        return String.format("%s/broadcast/call", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put("called_pn", mPhoneNumber);
        mPostParams.put("call_msg", mMassage);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(MobileInterconnectResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }
}
