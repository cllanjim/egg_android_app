package com.lingyang.camera.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.extras.SoundPullEventListener;
import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.AttentionPublicCameraMgmt;
import com.lingyang.camera.mgmt.GetPublicCameraMgmt;
import com.lingyang.camera.mgmt.UnAttentionPublicCameraMgmt;
import com.lingyang.camera.ui.activity.AppBaseActivity;
import com.lingyang.camera.ui.adapter.PublicCameraAdapter;
import com.lingyang.camera.ui.adapter.PublicCameraAdapter.CameraClickCallBackListener;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.sdk.player.IPlayer;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.List;

public class PublicCameraFragment extends Fragment {

    PullToRefreshListView mCameraListView;
    PublicCameraAdapter mGetPublicCameraAdapter;
    GetPublicCameraMgmt mGetHotPublicCameraMgmt;
    AttentionPublicCameraMgmt mAttentionPublicCameraMgmt;
    UnAttentionPublicCameraMgmt mUnAttentionPublicCameraMgmt;
    AttentionChangeListener mAttentionChangeListener;
    int page = 1;
    List<Camera> mListCamera = new ArrayList<Camera>();
    Handler mHandler;
    RefreshReceiver mReceiver;
    int mType;//0全部  1camera  2mobile
    BaseCallBack<List<Camera>> mGetCamerasCallback = new BaseCallBack<List<Camera>>() {

        @Override
        public void error(ResponseError object) {
            if (object != null) {
                ((AppBaseActivity) getActivity()).showToast(object.error_msg);
            }
            mCameraListView.onRefreshComplete();
        }

        @Override
        public void success(List<Camera> t) {
            CLog.v("success");
            if (page == 1) {
                mGetPublicCameraAdapter.Refresh(t);
                mCameraListView.onRefreshComplete();
            } else {
                mGetPublicCameraAdapter.AddList(t);
            }
        }
    };
    private Intent mIntent;
    CameraClickCallBackListener mCallBackListener = new CameraClickCallBackListener() {

        @Override
        public void viewLive(Camera camera, String cover) {
//            camera_type; 0 全部   1 camera    2 mobile
            if (camera.camera_type == 1) {
                if (camera.getIsOnline()) {
//				Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_MOBILE_LIVE_PLAYER);
                    Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_PLAYER);
                    intent.putExtra(Const.IntentKeyConst.KEY_CAMERA, camera);
                    intent.putExtra(Const.IntentKeyConst.KEY_CAMERA_COVER, cover);
                    intent.putExtra(Const.IntentKeyConst.KEY_LIVE_SHARE, false);
                    intent.putExtra(Const.IntentKeyConst.KEY_PLAYTYPE,
                            IPlayer.TYPE_QSTP);
                    intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP, camera.uname
                            .equals(LocalUserWrapper.getInstance().getLocalUser()
                                    .getUid()) ? "" : camera.rtmp_addr);
                    ActivityUtil.startActivity(getActivity(), intent);
                } else {
                    ((AppBaseActivity) getActivity()).showToast("设备["
                            + camera.cname + "]不在线");
                }
            } else {
                Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_MOBILE_LIVE_PLAYER);
                intent.putExtra(Const.IntentKeyConst.KEY_CAMERA, camera);
                intent.putExtra(Const.IntentKeyConst.KEY_CAMERA_COVER, cover);
                intent.putExtra(Const.IntentKeyConst.KEY_LIVE_SHARE, false);
                intent.putExtra(Const.IntentKeyConst.KEY_PLAYTYPE,
                        IPlayer.TYPE_QSTP);
                intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP, camera.uname
                        .equals(LocalUserWrapper.getInstance().getLocalUser()
                                .getUid()) ? "" : camera.rtmp_addr);
//				intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP, camera.uid
//						.equals(LocalUserWrapper.getInstance().getLocalUser()
//								.getUid()) ? "" : "rtmp://182.254.148.97:1935/live/3000000098");
                ActivityUtil.startActivity(getActivity(), intent);
            }
        }

        @Override
        public void attention(final Camera camera) {
            mAttentionPublicCameraMgmt.AttentionPublicCamera(getActivity(),
                    camera.cid, new BaseCallBack<Object>() {

                        @Override
                        public void error(ResponseError object) {
                            if (object != null) {
                                ((AppBaseActivity) getActivity())
                                        .showToast(object.error_code
                                                + object.error_msg);
                            } else {
                                ((AppBaseActivity) getActivity())
                                        .showToast("关注失败");
                            }
                        }

                        @Override
                        public void success(Object t) {

                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (mAttentionChangeListener != null) {
                                        mAttentionChangeListener.onChange();
                                    }
                                    mIntent.putExtra(
                                            Const.IntentKeyConst.KEY_IS_ATTENTION,
                                            true);
                                    mIntent.putExtra(
                                            Const.IntentKeyConst.KEY_CAMERA,
                                            camera);
                                    LocalBroadcastManager.getInstance(
                                            getActivity()).sendBroadcast(
                                            mIntent);
                                    ((AppBaseActivity) getActivity())
                                            .showToast("已关注，" + camera.cname);
                                    camera.is_followed = true;
                                    mGetPublicCameraAdapter.refresh();
                                }
                            });
                        }
                    });
        }

        @Override
        public void unAttention(final Camera camera) {
            mUnAttentionPublicCameraMgmt.UnAttentionPublicCamara(getActivity(),
                    camera.cid,UnAttentionPublicCameraMgmt.MGMT_UNSHARED, new BaseCallBack<Object>() {

                        @Override
                        public void error(ResponseError object) {
                            mGetPublicCameraAdapter.refresh();
                            ((AppBaseActivity) getActivity())
                                    .showToast("取消关注失败");
                        }

                        @Override
                        public void success(Object t) {
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (mAttentionChangeListener != null) {
                                        mAttentionChangeListener.onChange();
                                    }
                                    mIntent.putExtra(
                                            Const.IntentKeyConst.KEY_IS_ATTENTION,
                                            false);
                                    mIntent.putExtra(
                                            Const.IntentKeyConst.KEY_CID,
                                            camera.cid);
                                    LocalBroadcastManager.getInstance(
                                            getActivity()).sendBroadcast(
                                            mIntent);
                                    camera.is_followed = false;
                                    mGetPublicCameraAdapter.refresh();
                                    ((AppBaseActivity) getActivity())
                                            .showToast("已取消关注，" + camera.cname);
                                }
                            });
                        }
                    });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGetHotPublicCameraMgmt = (GetPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(GetPublicCameraMgmt.class);
        mAttentionPublicCameraMgmt = (AttentionPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(AttentionPublicCameraMgmt.class);
        mUnAttentionPublicCameraMgmt = (UnAttentionPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(UnAttentionPublicCameraMgmt.class);
        mReceiver = new RefreshReceiver();
        IntentFilter itf = new IntentFilter();
        itf.addAction(Const.Actions.ACTION_IS_BIND_REFRESH);
        itf.addAction(Const.Actions.ACTION_IS_ATTENTION_REFRESH);
        itf.addAction(Const.Actions.ACTION_IS_PUBLIC_REFRESH);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mReceiver, itf);

        mIntent = new Intent(Const.Actions.ACTION_IS_ATTENTION_REFRESH);
        mIntent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                Const.IntentKeyConst.REFRESH_FROM_PUBLIC);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_publiccamera, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHandler = new Handler();
        mCameraListView = (PullToRefreshListView) view
                .findViewById(R.id.list_discovery_camera);
        SoundPullEventListener<ListView> soundListener = new SoundPullEventListener<ListView>(
                getActivity());
        soundListener.addSoundEvent(State.REFRESHING, R.raw.diaoluo_xiao);
        // soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
        // soundListener.addSoundEvent(State.REFRESHING,
        // R.raw.refreshing_sound);
        mCameraListView.setOnPullEventListener(soundListener);
        mCameraListView.setScrollingWhileRefreshingEnabled(false);
        // mCameraListView.setMode(Mode.BOTH);
        mCameraListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                page = 1;
                refreshList();
            }
        });

        mCameraListView
                .setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

                    @Override
                    public void onLastItemVisible() {
                        if (!mGetPublicCameraAdapter.isLoadFinished()) {
                            refreshListByPage(++page);
                        } else {
                            Toast.makeText(getActivity(), "已加载全部!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        mGetPublicCameraAdapter = new PublicCameraAdapter(getActivity(),
                mListCamera);
        mCameraListView.getRefreshableView()
                .setAdapter(mGetPublicCameraAdapter);
        mCameraListView.setOnScrollListener(new PauseOnScrollListener(
                ImageLoader.getInstance(), false, true));
        mGetPublicCameraAdapter
                .setCameraOnClickCallBackListener(mCallBackListener);
        refreshList();
    }

    private void refreshListByPage(int page) {
        CLog.v("refreshListByPage page" + page);
        mGetHotPublicCameraMgmt.getPublicCamera(getActivity(), page,
                Const.PAGE_SIZE, mType, mGetCamerasCallback);
    }

    @Override
    public void onResume() {
        CLog.v("onResume");
        mHandler.removeCallbacksAndMessages(null);
        super.onResume();
    }

    @Override
    public void onPause() {
        CLog.v("onPause");
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                mReceiver);
        super.onDestroy();
    }

    public void groupRefresh(int type) {
        mType = type;
        refreshList();
    }

    public void refreshList() {
        CLog.v("refreshList ");
        if (mGetHotPublicCameraMgmt != null) {
            page = 1;
            mGetHotPublicCameraMgmt.getPublicCamera(getActivity(), page,
                    Const.PAGE_SIZE, mType, mGetCamerasCallback);
        }

    }

    public void refreshFromSet(boolean isUnBind, Camera c) {
        if (isUnBind) {
            mGetPublicCameraAdapter.localRemove(c.cid);
        } else {
            mGetPublicCameraAdapter.localUpdate(c);
        }
    }

    public void setAttentionChangeListener(
            AttentionChangeListener attentionChangeListener) {
        mAttentionChangeListener = attentionChangeListener;
    }

    public interface AttentionChangeListener {

        void onChange();

    }

    public class RefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int where = intent.getIntExtra(Const.IntentKeyConst.KEY_FROM_WHERE, 0);
            String cid;
            Camera c;
            switch (where) {
                case Const.IntentKeyConst.REFRESH_FROM_LIVE:
                    c = (Camera) intent.getSerializableExtra(Const.IntentKeyConst.KEY_CAMERA);
                    int type = intent.getIntExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE, 0);
                    if (type == Camera.TYPE_PRIVATE) {
                        mGetPublicCameraAdapter.localRemove(c.cid);
                    } else {
                        mGetPublicCameraAdapter.localAdd(c);
                    }
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_ATTENTION:
                case Const.IntentKeyConst.REFRESH_FROM_MYATTENTION:
                    cid = intent.getStringExtra(Const.IntentKeyConst.KEY_CID);
                    mGetPublicCameraAdapter.localCancelAttention(cid, false);
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_PUBLIC:
                    cid = intent.getStringExtra(Const.IntentKeyConst.KEY_CID);
                    boolean isAttention = intent.getBooleanExtra(Const.IntentKeyConst.KEY_IS_ATTENTION, false);
                    mGetPublicCameraAdapter.localCancelAttention(cid, isAttention);
                    break;
                case Const.IntentKeyConst.REFRESH_FROM_BIND:
                    refreshList();
                    break;
                default:
                    break;
            }
//			mGetHotPublicCameraMgmt.getPublicCamera(getActivity(), page,
//					Const.PAGE_SIZE, mGetCamerasCallback);
        }

    }

}
