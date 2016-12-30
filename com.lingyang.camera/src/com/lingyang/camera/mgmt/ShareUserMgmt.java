package com.lingyang.camera.mgmt;

import android.content.Context;
import android.os.Handler;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.http.HttpConnectManager;
import com.lingyang.base.utils.http.JsonParser;
import com.lingyang.base.utils.http.Request;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.ShareUserResponse;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.BaseMgmt;

import java.util.List;
import java.util.Map;

/**
 * 文件名: ShareUserMgmt
 * 描    述: 获取分享用户
 * 创建人: 刘 波
 * 创建时间: 2015/9
 */
public class ShareUserMgmt extends BaseMgmt {

    public BaseCallBack<List<ShareUserResponse.ShareUser>> mBaseCallBack;

    Handler mHandler;
    String mCid;

    public void getShareUserList(Context context, String cid, BaseCallBack<List<ShareUserResponse.ShareUser>> callback) {
        mContext = context;
        mBaseCallBack = callback;
        mCid = cid;
        mHandler = new Handler(context.getMainLooper());
        mHandler.post(mRequestRunnable);
    }

    @Override
    protected void response(String url, int state, Object result, int type, Request request, Map<String, String> headMap) {
        if (state == HttpConnectManager.STATE_SUC && result != null) {
            final ShareUserResponse info = (ShareUserResponse) result;
            if (info.getStatusCode() == Constants.Http.STATUS_CODE_SUCESS) {
                CLog.v("::::success:::" + info);
                if (info.data != null && mBaseCallBack != null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mBaseCallBack.success(info.data);
                        }
                    });

                }
            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mBaseCallBack.error(info.getError());
                    }
                });

            }
        } else {
            if (mBaseCallBack != null) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mBaseCallBack.error(null);
                    }
                });
            }
        }
    }

    @Override
    protected String getRequestUrl() {
        //%s/cameras/sharing/history?uid=%s&ip=%s&expire=%s&access_token=%s&cid=%s
        return String.format("%s/share/userlist", APP_REQUEST_URL);
    }

    @Override
    protected void request() {
        addValidUrlParams();
        mUrlParams.put("cid", mCid);
        request.setOnRequestListener(mRequestListener);
        request.setParser(new JsonParser(ShareUserResponse.class));
        HttpConnectManager.getInstance(mContext).doGet(request);
    }

}
