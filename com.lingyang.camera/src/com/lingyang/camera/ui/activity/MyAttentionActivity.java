package com.lingyang.camera.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.AttentionCameraMgmt;
import com.lingyang.camera.mgmt.CancelMyAttentionMgmt;
import com.lingyang.camera.mgmt.MyAttentionMgmt;
import com.lingyang.camera.ui.adapter.MyAttentionAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：MyAttentionActivity
 * 描述：此类主要显示我关注的摄像机列表，以及取消关注功能
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class MyAttentionActivity extends AppBaseActivity {

    TextView mTitleTv;
    ImageView mBackIv, mNullImg;
    PullToRefreshListView mPullToRefreshListView;
    MyAttentionAdapter mMyAttentionAdapter;
    MyAttentionMgmt mMyAttentionMgmt;
    List<Camera> mCameraList = new ArrayList<Camera>();
    BaseCallBack<List<Camera>> mCallBack = new BaseCallBack<List<Camera>>() {
        @Override
        public void error(ResponseError object) {
            if (object != null) {
                showToast(object.error_code + object.error_msg);
            }
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
        }

        @Override
        public void success(List<Camera> t) {
            if (t != null) {
                if (mCameraList != null) {
                    mCameraList.clear();
                }
                mCameraList.addAll(t);
                mMyAttentionAdapter.setData(mCameraList);
                if (mCameraList.size() == 0) {
                    mNullImg.setVisibility(View.VISIBLE);
                } else {
                    mNullImg.setVisibility(View.GONE);
                }
            }
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
        }


    };
    OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    private AttentionCameraMgmt mAttentionCameraMgmt;
    private CancelMyAttentionMgmt mCancelMgmt;
    private String mCId;
    private BaseCallBack<List<Camera>> mGetCamerasCallback = new BaseCallBack<List<Camera>>() {
        @Override
        public void error(ResponseError object) {
            if (object != null) {
                showToast(object.error_code + object.error_msg);
            }
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
        }

        @Override
        public void success(List<Camera> cameras) {
            if (cameras != null) {
                if (mCameraList != null) {
                    mCameraList.clear();
                }
                assert mCameraList != null;
                for (Camera camera : cameras) {
                    if (camera.is_followed) {
                        mCameraList.add(camera);
                    }
                }
                mMyAttentionAdapter.setData(mCameraList);
                if (mCameraList.size() == 0) {
                    mNullImg.setVisibility(View.VISIBLE);
                } else {
                    mNullImg.setVisibility(View.GONE);
                }
            }
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
        }
    };
    BaseCallBack<ResponseError> mDeleteCallBack = new BaseCallBack<ResponseError>() {
        @Override
        public void success(ResponseError t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            showToast((String) getText(R.string.was_cancel));
            initData();
            Intent it = new Intent(Const.Actions.ACTION_IS_ATTENTION_REFRESH);
            it.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                    Const.IntentKeyConst.REFRESH_FROM_MYATTENTION);
            it.putExtra(Const.IntentKeyConst.KEY_CID, mCId);
            it.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                    Const.IntentKeyConst.REFRESH_FROM_MYATTENTION);
            LocalBroadcastManager.getInstance(MyAttentionActivity.this).sendBroadcast(it);
        }

        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object != null) {
                showToast(object.error_code + object.error_msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_attention);
        mAttentionCameraMgmt = (AttentionCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(AttentionCameraMgmt.class);
        mMyAttentionMgmt = (MyAttentionMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(MyAttentionMgmt.class);
        mCancelMgmt = (CancelMyAttentionMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(CancelMyAttentionMgmt.class);
        initView();
        initData();
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.refreshlv);
        mTitleTv = (TextView) findViewById(R.id.tv_header_title);
        mBackIv = (ImageView) findViewById(R.id.iv_heder_back);
        mNullImg = (ImageView) findViewById(R.id.imgnull);

        mTitleTv.setText(getText(R.string.mine_myattention));
        mBackIv.setOnClickListener(mListener);
        mBackIv.setVisibility(View.VISIBLE);
    }

    private void initData() {
        mMyAttentionAdapter = new MyAttentionAdapter(this, mHandler,
                new MyAttentionAdapter.CallBackUpdate() {
                    @Override
                    public void update(Camera mCamera) {
                        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
                        mCancelMgmt.cancelMyAttention(MyAttentionActivity.this,
                                mCamera, mDeleteCallBack);
                        mCId = mCamera.cid;
                    }
                });
        mPullToRefreshListView.setAdapter(mMyAttentionAdapter);
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
//        mMyAttentionMgmt.getMyAttention(this, mCallBack);
        mAttentionCameraMgmt.getMyCamera(this, mGetCamerasCallback);
    }
}
