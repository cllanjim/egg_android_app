package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.base.utils.http.UploadRequest;
import com.lingyang.base.utils.http.UploadUtil;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.LoginToken;
import com.lingyang.camera.entity.LoginToken.UserToken;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件名: ChangeHeadMgmt
 * 描    述: 更改头像
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class ChangeHeadMgmt extends BaseMgmt {

    private File mFile;
    private BaseCallBack mBaseCallBack;

    public void changeHead(Context context, File file, BaseCallBack<UserToken> callBack) {
        mContext = context;
        this.mFile = file;
        this.mBaseCallBack = callBack;
        new Handler(context.getMainLooper()).post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        CLog.v("changehead---" + result);
        LoginToken info = (LoginToken) result;
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            if (info.status_code == Constants.Http.STATUS_CODE_SUCESS) {


                if (mBaseCallBack != null) {
                    mBaseCallBack.success(info.getData());
                }
            } else {
                if (mBaseCallBack != null) {
                    mBaseCallBack.error(info.error);
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
        return String.format("%s/users/update_face", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidPostParams();
        UploadRequest request = new UploadRequest();
        request.setHttpHead(mHeadParams);
        request.setOnRequestListener(mRequestListener);
        request.setPostData(mPostParams);
        request.setUrl(getRequestUrl());
        request.setTimeout(60000);
        request.setParser(new JsonParser(LoginToken.class));
        request.setUploadFile(mFile.getAbsolutePath());
        request.getUploadFileList().get(0).setName("faceimage");
        HashMap<String, String> headmap = new HashMap<String, String>();
        headmap.put("Content-Type", "image/jpeg");
        request.getUploadFileList().get(0).setHeadMap(headmap);
        UploadUtil.startUploadFile(mContext, request);
    }
}
