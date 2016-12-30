package com.lingyang.camera.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.extras.SoundPullEventListener;
import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.entity.CameraResponse;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.AttentionPublicCameraMgmt;
import com.lingyang.camera.mgmt.GetPublicCameraMgmt;
import com.lingyang.camera.mgmt.UnAttentionPublicCameraMgmt;
import com.lingyang.camera.ui.adapter.PublicCameraAdapter;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.sdk.player.IPlayer;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.List;

public class PublicCameraActivity extends AppBaseActivity {

    PullToRefreshListView mCameraListView;
    PublicCameraAdapter mGetPublicCameraAdapter;
    GetPublicCameraMgmt mGetHotPublicCameraMgmt;
    AttentionPublicCameraMgmt mAttentionPublicCameraMgmt;
    UnAttentionPublicCameraMgmt mUnAttentionPublicCameraMgmt;
    AttentionChangeListener mAttentionChangeListener;
    int page = 1;
    List<CameraResponse.MyCameras.Camera> mListCamera = new ArrayList<CameraResponse.MyCameras.Camera>();
    RefreshReceiver mReceiver;
    int mType;//0全部  1camera  2mobile
    private Intent mIntent;
    PublicCameraAdapter.CameraClickCallBackListener mCallBackListener = new PublicCameraAdapter.CameraClickCallBackListener() {

        @Override
        public void viewLive(CameraResponse.MyCameras.Camera camera, String cover) {
            //camera_type; 0 全部   1 camera    2 mobile
            if (camera.camera_type == 1) {
                if (camera.getIsOnline()) {
//				Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_MOBILE_LIVE_PLAYER);
                    Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_PLAYER);
                    intent.putExtra(Const.IntentKeyConst.KEY_CAMERA, camera);
                    intent.putExtra(Const.IntentKeyConst.KEY_CAMERA_COVER, cover);
                    intent.putExtra(Const.IntentKeyConst.KEY_LIVE_SHARE, false);
                    intent.putExtra(Const.IntentKeyConst.KEY_PLAYTYPE,
                            IPlayer.TYPE_QSUP);
                    intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP, camera.uname
                            .equals(LocalUserWrapper.getInstance().getLocalUser()
                                    .getUid()) ? "" : camera.rtmp_addr);
                    ActivityUtil.startActivity(PublicCameraActivity.this, intent);
                } else {
                    showToast("设备["
                            + camera.cname + "]不在线");
                }
            } else {
                Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_MOBILE_LIVE_PLAYER);
                intent.putExtra(Const.IntentKeyConst.KEY_CAMERA, camera);
                intent.putExtra(Const.IntentKeyConst.KEY_CAMERA_COVER, cover);
                intent.putExtra(Const.IntentKeyConst.KEY_LIVE_SHARE, false);
                intent.putExtra(Const.IntentKeyConst.KEY_PLAYTYPE,
                        IPlayer.TYPE_QSUP);
                intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP, camera.play_addr);
                ActivityUtil.startActivity(PublicCameraActivity.this, intent);
            }
        }

        @Override
        public void attention(final CameraResponse.MyCameras.Camera camera) {
            mAttentionPublicCameraMgmt.AttentionPublicCamera(PublicCameraActivity.this,
                    camera.cid, new BaseCallBack<Object>() {

                        @Override
                        public void error(ResponseError object) {
                            if (object != null) {
                                showToast(object.error_code
                                        + object.error_msg);
                            } else {
                                showToast("关注失败");
                            }
                        }

                        @Override
                        public void success(Object t) {

                            runOnUiThread(new Runnable() {

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
                                            PublicCameraActivity.this).sendBroadcast(
                                            mIntent);
                                    ((AppBaseActivity) PublicCameraActivity.this)
                                            .showToast("已关注，" + camera.cname);
                                    camera.is_followed = true;
                                    mGetPublicCameraAdapter.refresh();
                                }
                            });
                        }
                    });
        }

        @Override
        public void unAttention(final CameraResponse.MyCameras.Camera camera) {
            mUnAttentionPublicCameraMgmt.UnAttentionPublicCamara(PublicCameraActivity.this,
                    camera.cid, UnAttentionPublicCameraMgmt.MGMT_UNSHARED, new BaseCallBack<Object>() {

                        @Override
                        public void error(ResponseError object) {
                            mGetPublicCameraAdapter.refresh();
                            showToast("取消关注失败");
                        }

                        @Override
                        public void success(Object t) {
                            PublicCameraActivity.this.runOnUiThread(new Runnable() {

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
                                            PublicCameraActivity.this).sendBroadcast(
                                            mIntent);
                                    camera.is_followed = false;
                                    mGetPublicCameraAdapter.refresh();
                                    showToast("已取消关注，" + camera.cname);
                                }
                            });
                        }
                    });
        }
    };
    private CheckBox mCbGroup;
    private TextView mTvLoading;
    BaseCallBack<List<CameraResponse.MyCameras.Camera>> mGetCamerasCallback = new BaseCallBack<List<CameraResponse.MyCameras.Camera>>() {

        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            mTvLoading.setText(getString(R.string.click_to_refresh));
            if (object != null) {
                showToast(object.error_msg);
            }
            mCameraListView.onRefreshComplete();
        }

        @Override
        public void success(List<CameraResponse.MyCameras.Camera> t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            mTvLoading.setVisibility(View.GONE);
            CLog.v("success");
            if (page == 1) {
                mGetPublicCameraAdapter.Refresh(t);
                mCameraListView.onRefreshComplete();
            } else {
                mGetPublicCameraAdapter.AddList(t);
            }
        }
    };
    private Button mBtnTitle;
    /**
     * 分组查询点击监听
     */
    PopupHomeGroup.OnGroupListener mGroupListener = new PopupHomeGroup.OnGroupListener() {
        @Override
        public void callBack(int type) {
            groupRefresh(type);
        }
    };
    PopupWindow.OnDismissListener mOnDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            mBtnTitle.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, getResources().getDrawable(R.drawable.button_list_nor), null);
//            mCbGroup.setChecked(false);
        }
    };
    private PopupHomeGroup mPopupGroup;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_search:
                    ActivityUtil.startActivity(PublicCameraActivity.this, Const.Actions.ACTION_ACTIVITY_SEARCH);
                    break;
                case R.id.rl_public_header:
                    backToTop();
                    break;
                case R.id.btn_header_title:
                    showGroupPopup();
                    break;
                case R.id.btn_main_title_group:
                    showGroupPopup();
                    break;
                case R.id.tv_loading_public:
                    mTvLoading.setText("正在加载中...");
                    refreshList();
                    break;
                default:
                    break;
            }
        }
    };

    private void backToTop() {
        if (mCameraListView != null) {
            ListView publicListView = mCameraListView.getRefreshableView();
            if (publicListView != null) {
                if (!(publicListView).isStackFromBottom()) {
                    publicListView.smoothScrollToPosition(1);
                }
                publicListView.setStackFromBottom(false);
            }
        }
    }

    private void showGroupPopup() {
        // 点击分组查询
        if (!mPopupGroup.isShowing()) {
            mBtnTitle.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, getResources().getDrawable(R.drawable.button_list_pressed), null);
            mPopupGroup.showAsDropDown(mBtnTitle);
        } else {
            mBtnTitle.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, getResources().getDrawable(R.drawable.button_list_nor), null);
            mPopupGroup.dismiss();
        }
    }

    public void onClick_Back(View view) {
        onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_camera);
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
        LocalBroadcastManager.getInstance(PublicCameraActivity.this).registerReceiver(
                mReceiver, itf);

        mIntent = new Intent(Const.Actions.ACTION_IS_ATTENTION_REFRESH);
        mIntent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                Const.IntentKeyConst.REFRESH_FROM_PUBLIC);
        int type = getIntent().getIntExtra(Const.IntentKeyConst.KEY_LIVE_TYPE, PopupMenu.MENU_CAMERA_LIVE);
        initView();
//        groupRefresh(type);
        refreshList();
    }

    private void initView() {
        mCameraListView = (PullToRefreshListView) findViewById(R.id.list_discovery_camera);
        mBtnTitle = (Button) findViewById(R.id.btn_header_title);
        mTvLoading = (TextView) findViewById(R.id.tv_loading_public);
        mCbGroup = (CheckBox) findViewById(R.id.btn_main_title_group);
        RelativeLayout rlHeader = (RelativeLayout) findViewById(R.id.rl_public_header);
        mBtnTitle.setText(getString(R.string.all));
        mBtnTitle.setOnClickListener(mOnClickListener);
        mCbGroup.setOnClickListener(mOnClickListener);
        rlHeader.setOnClickListener(mOnClickListener);
        mTvLoading.setOnClickListener(mOnClickListener);
        ImageView ivSearch = (ImageView) findViewById(R.id.iv_search);
        ivSearch.setOnClickListener(mOnClickListener);

        mPopupGroup = new PopupHomeGroup(this);
        mPopupGroup.setOnGroupListener(mGroupListener);
        mPopupGroup.setOnDismissListener(mOnDismissListener);
        SoundPullEventListener<ListView> soundListener = new SoundPullEventListener<ListView>(
                PublicCameraActivity.this);
        soundListener.addSoundEvent(PullToRefreshBase.State.REFRESHING, R.raw.diaoluo_xiao);
        // soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
        // soundListener.addSoundEvent(State.REFRESHING,
        // R.raw.refreshing_sound);
        mCameraListView.setOnPullEventListener(soundListener);
        mCameraListView.setScrollingWhileRefreshingEnabled(false);
        // mCameraListView.setMode(Mode.BOTH);
        mCameraListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(PublicCameraActivity.this,
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                page = 1;
                refreshList();
            }
        });

        mCameraListView
                .setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

                    @Override
                    public void onLastItemVisible() {
                        if (!mGetPublicCameraAdapter.isLoadFinished()) {
                            refreshListByPage(++page);
                        } else {
                            Toast.makeText(PublicCameraActivity.this, "已加载全部!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        mGetPublicCameraAdapter = new PublicCameraAdapter(getApplicationContext(),
                mListCamera);
        mCameraListView.getRefreshableView()
                .setAdapter(mGetPublicCameraAdapter);
        mCameraListView.setOnScrollListener(new PauseOnScrollListener(
                ImageLoader.getInstance(), false, true));
        mGetPublicCameraAdapter
                .setCameraOnClickCallBackListener(mCallBackListener);
    }

    public void refreshList() {
        CLog.v("refreshList ");
        if (mGetHotPublicCameraMgmt != null) {
            page = 1;
            mGetHotPublicCameraMgmt.getPublicCamera(getApplicationContext(), page,
                    Const.PAGE_SIZE, mType, mGetCamerasCallback);
        }

    }

    private void refreshListByPage(int page) {
        CLog.v("refreshListByPage page" + page);
        mGetHotPublicCameraMgmt.getPublicCamera(getApplicationContext(), page,
                Const.PAGE_SIZE, mType, mGetCamerasCallback);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(PublicCameraActivity.this).unregisterReceiver(
                mReceiver);
        if (mGetPublicCameraAdapter!=null) {
            mGetPublicCameraAdapter.stopLoad();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        CLog.v("onPause");
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    public void groupRefresh(int type) {
        switch (type) {
            case PopupHomeGroup.GROUP_ALL:
                mBtnTitle.setText(getString(R.string.all));
                break;
            case PopupHomeGroup.GROUP_CAMERA:
                mBtnTitle.setText(getString(R.string.camera_player));
                break;
            case PopupHomeGroup.GROUP_MOBILE:
                mBtnTitle.setText(getString(R.string.mobile_player));
                break;
            default:
                break;
        }
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        mType = type;
        refreshList();
    }

    public void refreshFromSet(boolean isUnBind, CameraResponse.MyCameras.Camera c) {
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
            CameraResponse.MyCameras.Camera c;
            switch (where) {
                case Const.IntentKeyConst.REFRESH_FROM_LIVE:
                    c = (CameraResponse.MyCameras.Camera) intent.getSerializableExtra(Const.IntentKeyConst.KEY_CAMERA);
                    int type = intent.getIntExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE, 0);
                    if (type == CameraResponse.MyCameras.Camera.TYPE_PRIVATE) {
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
