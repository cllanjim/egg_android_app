package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.CallContactResponse;
import com.lingyang.camera.entity.CallContactResponse.CallContact;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.List;
import java.util.Map;

/**
 * 文件名: SearchCallContactMgmt
 * 描    述:视频通话 搜索用户
 * 创建人:廖雷
 * 创建时间: 2015/11
 */
public class SearchCallContactMgmt extends BaseMgmt {

    private BaseCallBack<List<CallContact>> callBack;
    private String input_value;

    public void SearchCallContact(Context context, BaseCallBack<List<CallContact>> callback, String input_value) {

        mContext = context;
        this.callBack = callback;
        this.input_value = input_value;
        Handler handler = new Handler(context.getMainLooper());
        handler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            CallContactResponse info = (CallContactResponse) result;
            if (info.status_code == Constants.Http.STATUS_CODE_SUCESS) {
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
        return String.format("%s/broadcast/find", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        mPostParams.put("input_value", input_value);
        Request request = new Request(getRequestUrl());
        request.setHttpHead(mHeadParams);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(CallContactResponse.class));
        HttpConnectManager.getInstance(mContext).doPost(request, mPostParams);
    }



}
