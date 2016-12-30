package com.lingyang.camera.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.CancelPublicCameraMgmt;
import com.lingyang.camera.mgmt.GetMyCameraListMgmt;
import com.lingyang.camera.mgmt.SetCameraToPublicMgmt;
import com.lingyang.camera.ui.activity.MainActivity;
import com.lingyang.camera.ui.adapter.LiveAdapter;
import com.lingyang.camera.ui.adapter.LiveAdapter.CameraClickCallBackListener;
import com.lingyang.camera.ui.adapter.LiveAdapter.UpdateUiListener;
import com.lingyang.camera.util.ActivityUtil;

import java.util.List;

public class LiveFragment extends Fragment {
    private View v;
    private Context mContext;
    private GetMyCameraListMgmt mGetMgmt;
    private SetCameraToPublicMgmt mPublicMgmt;
    private CancelPublicCameraMgmt mCancelMgmt;
    private RefreshReceiver mReceiver;
    private Intent mIntent;
    private LiveAdapter adapter;
    private PullToRefreshListView mListView;
    CameraClickCallBackListener mCameraClickCallBackListener = new CameraClickCallBackListener() {

        @Override
        public void viewLive(Camera camera, String url) {
//            AppBaseActivity activity = (AppBaseActivity) getActivity();
//            if (camera.type == Camera.TYPE_PRIVATE) {
//                if (CloudOpenAPI.getInstance().getDeviceStatus(camera.cid) != ICloudOpenAPI.DEVICE_STATUS_PREPARED) {
//                    CLog.v("CloudOpenAPI.getInstance().getDeviceStatus():camera.cid"
//                            + camera.cid
//                            + ":"
//                            + CloudOpenAPI.getInstance().getDeviceStatus(
//                            camera.cid));
//                    activity.showToast("设备未就绪");
//                    return;
//                }
//            } else {
//                if (!camera.is_online) {
//                    activity.showToast("设备[" + camera.cname + "]不在线");
//                    return;
//                }
//            }
//            Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_PLAYER);
//            intent.putExtra(Const.IntentKeyConst.KEY_CAMERA, camera);
//            intent.putExtra(Const.IntentKeyConst.KEY_PLAYTYPE, IPlayer.TPPE_LIVE);
//            intent.putExtra(Const.IntentKeyConst.KEY_CAMERA_COVER, url);
//            intent.putExtra(Const.IntentKeyConst.KEY_LIVE_SHARE, false);
//            intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP, "");
//            ActivityUtil.startActivity(activity, intent);
        }
    };

    OnClickListener mUseMobileListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ActivityUtil.startActivity(mContext,
                    Const.Actions.ACTION_ACTIVITY_USE_MOBILE_LIVE);
        }
    };
    OnClickListener mAddListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ActivityUtil.startActivity(mContext,
                    Const.Actions.ACTION_ACTIVITY_FIRST_OF_ADD_DEVICE);
        }
    };

    BaseCallBack<List<Camera>> mCallBack = new BaseCallBack<List<Camera>>() {
        @Override
        public void error(ResponseError object) {
            mListView.onRefreshComplete();
            if (object != null) {
                ((MainActivity) getActivity()).showToast(object.error_code
                        + object.error_msg);
            }
        }

        @Override
        public void success(List<Camera> t) {
            mListView.onRefreshComplete();
            if (t != null) {
                adapter.setData(t);
            }
        }
    };

    BaseCallBack<BaseResponse> mPublicCallBack = new BaseCallBack<BaseResponse>() {

        @Override
        public void success(BaseResponse t) {
            ((MainActivity) getActivity())
                    .showToast((String) getText(R.string.live_set_suc));
            getCameraList();

            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(mIntent);

        }

        @Override
        public void error(ResponseError object) {
            if (object != null) {
                ((MainActivity) getActivity()).showToast(object.error_code
                        + object.error_msg);
            } else {
                ((MainActivity) getActivity())
                        .showToast((String) getText(R.string.live_set_fail));
            }
        }
    };
    UpdateUiListener mUpdateListener = new UpdateUiListener() {

        @Override
        public void update(Camera c, boolean isToPublic) {
            mIntent.putExtra(Const.IntentKeyConst.KEY_CID, c.cid);
            mIntent.putExtra(Const.IntentKeyConst.KEY_CAMERA, c);
            if (isToPublic) {
                mIntent.putExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE, Camera.TYPE_PUBLIC);
                mPublicMgmt.setPublic(mContext, c.cid, mPublicCallBack);
            } else {
                mIntent.putExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE, Camera.TYPE_PRIVATE);
                mCancelMgmt.cancelPublicCamera(mContext, c.cid, mPublicCallBack);
            }
        }
    };

    PullToRefreshBase.OnRefreshListener mOnRefreshListener = new PullToRefreshBase.OnRefreshListener() {
        @Override
        public void onRefresh(PullToRefreshBase pullToRefreshBase) {
            getCameraList();
        }
    };

    public LiveFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mReceiver = new RefreshReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Const.Actions.ACTION_IS_PUBLIC_REFRESH);
        mFilter.addAction(Const.Actions.ACTION_IS_BIND_REFRESH);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver,
                mFilter);
        mIntent = new Intent(Const.Actions.ACTION_IS_PUBLIC_REFRESH);
        mIntent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE, Const.IntentKeyConst.REFRESH_FROM_LIVE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_live, container, false);

        mGetMgmt = (GetMyCameraListMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(GetMyCameraListMgmt.class);
        mPublicMgmt = (SetCameraToPublicMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(SetCameraToPublicMgmt.class);
        mCancelMgmt = (CancelPublicCameraMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(CancelPublicCameraMgmt.class);
        initView();
        getCameraList();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
                mReceiver);
        super.onDestroy();
    }

    private void initView() {

        RelativeLayout addLayout = (RelativeLayout) v.findViewById(R.id.layout_add);
        mListView = (PullToRefreshListView) v.findViewById(R.id.listview);
        TextView titleText = (TextView) v.findViewById(R.id.tv_header_title);
        RelativeLayout rlUseMobile = (RelativeLayout) v.findViewById(R.id.layout_use_mobile);

        mListView.setOnRefreshListener(mOnRefreshListener);
        rlUseMobile.setOnClickListener(mUseMobileListener);
        addLayout.setOnClickListener(mAddListener);
        titleText.setText(getText(R.string.live_title));
        adapter = new LiveAdapter(mContext);
        adapter.setUpdateListener(mUpdateListener);
        adapter.setCameraOnClickCallBackListener(mCameraClickCallBackListener);
        mListView.setAdapter(adapter);
    }

    private void getCameraList() {
        mGetMgmt.getCameraList(mContext, mCallBack);
    }

    class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getCameraList();
        }

    }
}
