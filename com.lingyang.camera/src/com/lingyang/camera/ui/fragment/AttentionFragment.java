package com.lingyang.camera.ui.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.extras.SoundPullEventListener;
import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.CameraState;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.AttentionCameraMgmt;
import com.lingyang.camera.mgmt.UnAttentionPublicCameraMgmt;
import com.lingyang.camera.mgmt.UnShareCameraToOthersMgmt;
import com.lingyang.camera.ui.activity.AppBaseActivity;
import com.lingyang.camera.ui.activity.ShareToPopup;
import com.lingyang.camera.ui.adapter.AttentionAdapter;
import com.lingyang.camera.ui.adapter.AttentionAdapter.CameraClickCallBackListener;
import com.lingyang.camera.ui.widget.LYProgressDialog;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.player.IPlayer;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.List;

/**
 * 文件名：AttentionFragment
 * 描述：
 * 此Fragment主要显示我自己的和我关注的以及分享给我的摄像机列表
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class AttentionFragment extends Fragment {

    public static final int RESULT_CODE_SETTING_PUBLIC = 1;
    public static final int RESULT_CODE_SETTING_UNBIND = 2;
    public static final int RESULT_CODE_SETTING = 3;
    public PullToRefreshListView mCameraListView;


    AppBaseActivity mAppBaseActivity;
    private TextView mLoadingTv;
    private AttentionAdapter mAttentionAdapter;
    /**
     * 获取摄像头列表回调
     */
    BaseCallBack<List<Camera>> mGetCamerasCallback = new BaseCallBack<List<Camera>>() {

        @Override
        public void error(final ResponseError object) {
            if (isAdded()) {
                mLoadingTv.setText(getString(R.string.click_to_refresh));
            }
            if (object != null && getActivity() != null) {
                ((AppBaseActivity) getActivity()).showToast(object.error_msg);
            }
            mCameraListView.onRefreshComplete();

        }

        @Override
        public void success(final List<Camera> t) {
            CLog.v("success");
            mLoadingTv.setVisibility(View.GONE);
            mCameraListView.setVisibility(View.VISIBLE);
            mAttentionAdapter.refresh(t);
            mCameraListView.onRefreshComplete();
        }
    };
    private AttentionCameraMgmt mAttentionCameraMgmt;
    private UnAttentionPublicCameraMgmt mUnAttentionPublicCameraMgmt;
    private Camera mCamera;
    private int mPosition, mType;
    private Handler mHandler;
    Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            mAttentionAdapter.refreshCameraStatus();
            mHandler.postDelayed(mRefreshRunnable, 1000);
        }
    };
    private RefreshReceiver mReceiver;
    private BroadcastReceiver mDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CameraState.PayLoad payLoad = (CameraState.PayLoad)
                    intent.getSerializableExtra(Const.IntentKeyConst.KEY_MSG_REFRESH_STATE);
            CLog.d("---- mDeviceReceiver " + payLoad);
            mAttentionAdapter.refreshCameraState(payLoad);
//            Gson gson = new Gson();

//            CLog.v("mDeviceReceiver:" + gson.toJson(message));
//             /*PopConfig指令	指令说明	是否配置公众	是否配置录像	是否配置rtmp服务
//    0	待命状态，设备刚启动的初始状态	否	否	否
//    2	配置公众摄像头	是	否	是
//    3	配置摄像头录像	否	是	是
//    4	配置公众摄像头录像	是	是	是*/
//            CLog.v("mDeviceReceiver updateCameraConfig");
//            boolean isPublic;
//            if (message.Message.equals("2") || message.Message.equals("4")) {
//                isPublic = true;
//            } else {
//                isPublic = false;
//            }
//            mAttentionAdapter.updateCameraConfig(message.SrcID, false, isPublic);
        }
    };
    private UnShareCameraToOthersMgmt mUnShareCameraToOthersMgmt;
    CameraClickCallBackListener mCameraOnClickCallBackListener = new CameraClickCallBackListener() {

        /**
         * 点击分享
         * @param cid 设备唯一id
         * @param cname 摄像机名称
         * @param shared 是否分享
         */
        @Override
        public void share(final String cid, String cname, final int shared) {
            if (mAttentionAdapter.getIsConfiging(cid)) {
                mAppBaseActivity.showToast("设备正在更改配置中,请稍后");
                return;
            }
            Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_SHARE);
            intent.putExtra(Const.IntentKeyConst.KEY_CID, cid);
            intent.putExtra(Const.IntentKeyConst.KEY_CNAME, cname);
            ActivityUtil.startActivity(getActivity(), intent);
            ShareToPopup sharePopup = new ShareToPopup(getActivity(), cid, cname, shared);
            sharePopup.showAtLocation(
                    getActivity().findViewById(R.id.rootview), Gravity.CENTER,
                    0, 0);
            sharePopup.setOnResultCallback(new ShareResultCallback() {
                @Override
                public void onShare(int count) {
                    if (count != shared) {
                        mAttentionAdapter.refresh(cid, count);
                    }
                }
            });
        }

        /**
         * 跳转到设置
         * @param camera
         * @param pos
         */
        @Override
        public void setting(Camera camera, int pos) {
            if (mAttentionAdapter.getIsConfiging(camera.cid)) {
                mAppBaseActivity.showToast("设备正在更改配置中,请稍后");
                return;
            }
            Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_SET);
            intent.putExtra(Const.IntentKeyConst.KEY_CID, camera.cid);
            intent.putExtra(Const.IntentKeyConst.KEY_CNAME, camera.cname);
            intent.putExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE, camera.type);
            mPosition = pos;
            mCamera = camera;
            mType = mCamera.type;
            startActivityForResult(intent, RESULT_CODE_SETTING);
        }

        /**
         * 跳转进入播放
         * @param camera
         * @param imageUrl
         */
        @Override
        public void viewLive(Camera camera, String imageUrl) {
            if (camera.getCameraOwner()== Camera.CameraOwner.CAMERA_MINE
                    ||camera.getCameraOwner()== Camera.CameraOwner.CAMERA_SHARA_TO_ME) {
                if (camera.state == Camera.DEVICE_STATUS_PREPARED
                        || camera.state == Camera.DEVICE_STATUS_LIVING) {
                    gotoLivePlayer(camera, imageUrl);
                } else {
                    mAppBaseActivity.showToast("设备" + camera.getStatus(camera.state));
                }
            }else {
                if (camera.is_online) {
                    gotoLivePlayer(camera, imageUrl);
                } else {
                    mAppBaseActivity.showToast("设备" + camera.getStatus(camera.state));
                }
            }

        }

        /**
         * 跳转进入录像播放
         * @param camera
         * @param imageUrl
         */
        @Override
        public void viewRecord(final Camera camera, final String imageUrl) {
//            Intent intent1 = new Intent();
//            intent1.setClass(getActivity(), RecordEventActivity.class);
//            ActivityUtil.startActivity(getActivity(),intent1);
            if (LYService.getInstance().isOnline()) {
                gotoRecordPlayer(camera, imageUrl);
            } else {
                final LYProgressDialog lyProgressDialog = new LYProgressDialog(getActivity());
                ((AppBaseActivity) getActivity()).showToast("云平台离线，重新登录中...");
                if (!lyProgressDialog.isShowing() && !isDetached())
                    lyProgressDialog.show();
                LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
                if (localUser != null) {
                    LYService.getInstance().startCloudService(
                            localUser.getUserToken(),
                            localUser.getInitString(),
                            new CallBackListener<Long>() {
                                @Override
                                public void onSuccess(Long aLong) {
                                    if (lyProgressDialog != null && lyProgressDialog.isShowing()) {
                                        lyProgressDialog.dismiss();
                                    }
                                    gotoRecordPlayer(camera, imageUrl);
                                }

                                @Override
                                public void onError(LYException e) {
                                    if (lyProgressDialog != null && lyProgressDialog.isShowing()) {
                                        lyProgressDialog.dismiss();
                                    }
                                    AppBaseActivity activity = (AppBaseActivity) getActivity();
                                    if (activity != null) {
                                        activity.showToast(getString(R.string.cloud_login_fail));
                                    }
                                }
                            });
                }
            }
        }

        /**
         * 取消关注
         * @param camera
         */
        @Override
        public void unAttention(final Camera camera, final String unAttentionType) {
            String string = "";
            if (unAttentionType.equals(UnAttentionPublicCameraMgmt.MGMT_UNATTENTION)) {
                string = "是否取消关注";
            } else {
                string = "是否不再查看分享给我的";
            }
            new AlertDialog.Builder(getActivity())
                    .setTitle("提示")
                    .setMessage(string + camera.cname + " 摄像机?")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (unAttentionType.equals(UnAttentionPublicCameraMgmt.MGMT_UNATTENTION)) {
                        mUnAttentionPublicCameraMgmt.UnAttentionPublicCamara(getActivity(),
                                camera.cid, unAttentionType, new BaseCallBack<Object>() {
                                    @Override
                                    public void error(ResponseError object) {
                                        if (object != null) {
                                            mAppBaseActivity.showToast(object.error_code + object.error_msg);
                                        } else {
                                            mAppBaseActivity.showToast(
                                                    "取消关注:" + camera.cname + "失败");
                                        }
                                    }

                                    @Override
                                    public void success(Object t) {
                                        mAppBaseActivity.showToast(
                                                "已取消关注:" + camera.cname);
                                        sendRefreshBroadcast(camera);
                                    }
                                });
                    } else {
                        mUnShareCameraToOthersMgmt.unShareCamaraToOther(getActivity(), camera.cid,
                                LocalUserWrapper.getInstance().getLocalUser().getNickName(), new BaseCallBack() {
                                    @Override
                                    public void error(ResponseError object) {
                                        if (object == null) {
                                            mAppBaseActivity.showToast(mAppBaseActivity.getString(R.string.share_cancel_fail));
                                        } else {
                                            mAppBaseActivity.showToast(object.error_code + "_" + object.error_msg);
                                        }
                                    }

                                    @Override
                                    public void success(Object o) {
                                        mAppBaseActivity.showToast(String.format(
                                                "已删除 %s 分享给我的 %s 摄像机",
                                                camera.nickname,
                                                camera.cname));
                                        sendRefreshBroadcast(camera);
                                    }
                                });
                    }
                }
            }).create().show();
        }
    };

    private void gotoLivePlayer(Camera camera, String imageUrl) {
        Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_PLAYER);
        intent.putExtra(Const.IntentKeyConst.KEY_CAMERA, camera);
        intent.putExtra(Const.IntentKeyConst.KEY_PLAYTYPE, IPlayer.TYPE_QSTP);
        intent.putExtra(Const.IntentKeyConst.KEY_CAMERA_COVER, imageUrl);
        intent.putExtra(Const.IntentKeyConst.KEY_LIVE_SHARE,
                camera.getCameraOwner() == Camera.CameraOwner.CAMERA_SHARA_TO_ME);
        intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP,
                camera.play_addr);
        ActivityUtil.startActivity(mAppBaseActivity, intent);
    }

    View.OnClickListener mRefreshOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLoadingTv.setText("正在加载中...");
            refreshList();
        }
    };

    private void gotoRecordPlayer(Camera camera, String imageUrl) {
        Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_PLAYER);
        intent.putExtra(Const.IntentKeyConst.KEY_CAMERA, camera);
        intent.putExtra(Const.IntentKeyConst.KEY_PLAYTYPE,
                IPlayer.TYPE_RECORD);
        intent.putExtra(Const.IntentKeyConst.KEY_CAMERA_COVER, imageUrl);
        intent.putExtra(Const.IntentKeyConst.KEY_LIVE_SHARE,
                camera.getCameraOwner() == Camera.CameraOwner.CAMERA_SHARA_TO_ME);
        intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP,
                camera.type == Camera.TYPE_PUBLIC ? camera.rtmp_addr : "");
        ActivityUtil.startActivity(getActivity(), intent);
    }

    private void sendRefreshBroadcast(final Camera camera) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAttentionAdapter.removeCamera(camera);
                Intent it = new Intent(
                        Const.Actions.ACTION_IS_ATTENTION_REFRESH);
                it.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                        Const.IntentKeyConst.REFRESH_FROM_ATTENTION);
                it.putExtra(Const.IntentKeyConst.KEY_CID, camera.cid);
                LocalBroadcastManager.getInstance(
                        getActivity()).sendBroadcast(it);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CLog.v("onActivityResult-resultCode" + resultCode);
        if (resultCode == RESULT_CODE_SETTING_PUBLIC) {
            refreshList();
        } else if (resultCode == RESULT_CODE_SETTING_UNBIND) {
            mAttentionAdapter.removeCamera(mPosition);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mAttentionCameraMgmt = (AttentionCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(AttentionCameraMgmt.class);
        mUnAttentionPublicCameraMgmt = (UnAttentionPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(UnAttentionPublicCameraMgmt.class);
        mUnShareCameraToOthersMgmt = (UnShareCameraToOthersMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(UnShareCameraToOthersMgmt.class);

        mReceiver = new RefreshReceiver();
        IntentFilter itf = new IntentFilter();
        itf.addAction(Const.Actions.ACTION_IS_BIND_REFRESH);
        itf.addAction(Const.Actions.ACTION_IS_ATTENTION_REFRESH);
        itf.addAction(Const.Actions.ACTION_IS_PUBLIC_REFRESH);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mReceiver, itf);
        mAppBaseActivity = ((AppBaseActivity) getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.Actions.ACTION_MSG_REFRESH_STATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDeviceReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_attention, null);
        Button gotoPubButton = (Button) view.findViewById(R.id.tv_pub_camera_operate);
        gotoPubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CLog.v("");
                ((HomeFragment) getParentFragment()).gotoPublic();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLoadingTv = (TextView) view.findViewById(R.id.tv_loading);
        mCameraListView = (PullToRefreshListView) view
                .findViewById(R.id.list_attention_camera);
        mCameraListView.setVisibility(View.INVISIBLE);
        LinearLayout emptyLinearLayout = (LinearLayout) view
                .findViewById(R.id.ll_attention_empty);
        emptyLinearLayout.setVisibility(View.GONE);
        mCameraListView.setVisibility(View.GONE);
        mLoadingTv.setVisibility(View.VISIBLE);
        mLoadingTv.setOnClickListener(mRefreshOnClickListener);
        SoundPullEventListener<ListView> soundListener = new SoundPullEventListener<ListView>(
                getActivity());
        soundListener.addSoundEvent(State.REFRESHING, R.raw.diaoluo_xiao);
        mCameraListView.setOnPullEventListener(soundListener);
        mCameraListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                refreshList();
            }
        });
        mAttentionAdapter = new AttentionAdapter(getActivity(),
                (HomeFragment) getParentFragment());
        mCameraListView.getRefreshableView().setAdapter(mAttentionAdapter);
        mCameraListView.setOnScrollListener(new PauseOnScrollListener(
                ImageLoader.getInstance(), false, true));
        refreshList();
    }

    @Override
    public void onStart() {
        super.onStart();
//        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        CLog.v("onDestroy");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDeviceReceiver);
        super.onDestroy();
    }

    public void refreshList() {
        mAttentionAdapter
                .setCameraOnClickCallBackListener(mCameraOnClickCallBackListener);
        mAttentionCameraMgmt.getMyCamera(getActivity(), mGetCamerasCallback);
    }

    private void refreshCameraIsOnline() {
        mHandler.postDelayed(mRefreshRunnable, 1000);
    }

    public void refreshConfig() {
        CLog.v("refreshConfig");
        if (mCamera != null) {
            mAttentionAdapter.updateCameraConfig(mCamera.cid, true, null);
        }
    }

    public interface ShareResultCallback {
        void onShare(int count);
    }

    /**
     * 广播监听本地数据刷新
     */
    public class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int where = intent.getIntExtra(Const.IntentKeyConst.KEY_FROM_WHERE, 0);
            String cid;
            String nickname = intent.getStringExtra(Const.IntentKeyConst.KEY_NICKNAME);
            String cname = intent.getStringExtra(Const.IntentKeyConst.KEY_CNAME);
            switch (where) {
                case Const.IntentKeyConst.REFRESH_FROM_SET:
                    cid = intent.getStringExtra(Const.IntentKeyConst.KEY_CID);
                    if (intent.getBooleanExtra(
                            Const.IntentKeyConst.KEY_SET_IS_UNBIND, false)) {
                        mAttentionAdapter.localRemove(cid);
                        ((HomeFragment) getParentFragment()).getAdapter().
                                refreshPublicFragment(1, true, null, false, 0);
                    } else {
                        mAttentionAdapter.localUpdate(cid,
                                intent.getStringExtra(Const.IntentKeyConst.KEY_CNAME),
                                intent.getIntExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE,
                                        mType));
                        mCamera.cname = intent.getStringExtra(Const.IntentKeyConst.KEY_CNAME);
                        mCamera.type = intent.getIntExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE,
                                mType);
                        ((HomeFragment) getParentFragment()).getAdapter().
                                refreshPublicFragment(1, false, mCamera, false, 0);
                    }
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_LIVE:
                    cid = intent.getStringExtra(Const.IntentKeyConst.KEY_CID);
                    mAttentionAdapter.localUpdate(cid, null,
                            intent.getIntExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE,
                                    mType));
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_MYATTENTION:
                    cid = intent.getStringExtra(Const.IntentKeyConst.KEY_CID);
                    mAttentionAdapter.localRemove(cid);
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_UNSHARE:
                    cid = intent.getStringExtra(Const.IntentKeyConst.KEY_CID);
                    ((AppBaseActivity) getActivity()).showToast(
                            String.format("%s 取消分享了 %s 摄像机", nickname, cname));
                    mAttentionAdapter.deleteShareCamera(cid);
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_SHARE:
                    cid = intent.getStringExtra(Const.IntentKeyConst.KEY_CID);
                    ((AppBaseActivity) getActivity()).showToast(
                            String.format("%s 给你分享了 %s 摄像机", nickname, cname));
                    refreshList();
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_PUBLIC:
                    boolean flag = intent.getBooleanExtra(
                            Const.IntentKeyConst.KEY_IS_ATTENTION, false);
                    if (flag) {
                        mAttentionAdapter.localAdd((Camera) intent.getSerializableExtra(Const.IntentKeyConst.KEY_CAMERA));
                    } else {
                        cid = intent.getStringExtra(Const.IntentKeyConst.KEY_CID);
                        mAttentionAdapter.localRemove(cid);
                    }
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_BIND:
                    refreshList();
                    break;
                default:
                    break;
            }
        }
    }
}
