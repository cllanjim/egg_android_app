package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.AttentionPublicCameraMgmt;
import com.lingyang.camera.mgmt.SearchCameraMgmt;
import com.lingyang.camera.mgmt.UnAttentionPublicCameraMgmt;
import com.lingyang.camera.ui.adapter.PublicCameraAdapter;
import com.lingyang.camera.ui.widget.CustomEditText;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.sdk.player.IPlayer;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.List;

public class SearchCameraActivity extends AppBaseActivity {

    private final int TYPE_ALL = 0;
    private final int TYPE_CAMERA = 1;
    private final int TYPE_MOBILE = 2;
    PublicCameraAdapter.CameraClickCallBackListener mCallBackListener = new PublicCameraAdapter.CameraClickCallBackListener() {

        @Override
        public void viewLive(Camera camera, String cover) {
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
                    ActivityUtil.startActivity(SearchCameraActivity.this, intent);
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
                intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP, camera.uname
                        .equals(LocalUserWrapper.getInstance().getLocalUser()
                                .getUid()) ? "" : camera.rtmp_addr);
//				intent.putExtra(Const.IntentKeyConst.KEY_LIVE_RTMP, camera.uid
//						.equals(LocalUserWrapper.getInstance().getLocalUser()
//								.getUid()) ? "" : "rtmp://182.254.148.97:1935/live/3000000098");
                ActivityUtil.startActivity(SearchCameraActivity.this, intent);
            }
        }

        @Override
        public void attention(final Camera camera) {
        }

        @Override
        public void unAttention(final Camera camera) {
        }
    };
    private CustomEditText mEtSearchCamera;
    private SearchCameraMgmt mSearchCameraMgmt;
    private PullToRefreshListView mCameraListView;
    private int mPage = 1;
    private List<Camera> mListCamera = new ArrayList<Camera>();
    private PublicCameraAdapter mPublicCameraAdapter;
    private AttentionPublicCameraMgmt mAttentionPublicCameraMgmt;
    private UnAttentionPublicCameraMgmt mUnAttentionPublicCameraMgmt;
    private TextView mTvNoResult;
    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
                mCameraListView.setVisibility(View.GONE);
                mTvNoResult.setVisibility(View.GONE);
                showInputMethod(true);
            }
        }
    };
    private BaseCallBack<List<Camera>> mSearchCallback
            = new BaseCallBack<List<Camera>>() {
        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            mCameraListView.onRefreshComplete();
            if (object != null) {
                showToast(object.error_msg);
            } else {
                showToast("搜索失败");
            }
        }

        @Override
        public void success(List<Camera> cameras) {

            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            mCameraListView.onRefreshComplete();
            if (cameras == null || cameras.size() == 0) {
                showList(mPage != 1);
            } else {
                showList(true);
                if (mPage == 1) {
                    mPublicCameraAdapter.Refresh(cameras);
                    mCameraListView.onRefreshComplete();
                } else {
                    mPublicCameraAdapter.AddList(cameras);
                }
            }
        }
    };
    private String mKeyWord;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_search:
                    mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
                    searchCamera();
                    break;
                case R.id.btn_back:
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            CLog.v("actionId " + actionId);
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
                searchCamera();
                return true;
            }
            return false;
        }
    };

    private void showList(boolean hasData) {
        mTvNoResult.setVisibility(hasData ? View.GONE : View.VISIBLE);
        mCameraListView.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_camera);
        mAttentionPublicCameraMgmt = (AttentionPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(AttentionPublicCameraMgmt.class);
        mUnAttentionPublicCameraMgmt = (UnAttentionPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(UnAttentionPublicCameraMgmt.class);
        mSearchCameraMgmt = (SearchCameraMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(SearchCameraMgmt.class);
        initView();
        showInputMethod(true);
    }

    private void initView() {
        ImageView ivSearch = (ImageView) findViewById(R.id.iv_search);
        mCameraListView = (PullToRefreshListView) findViewById(R.id.list_discovery_camera);
        mTvNoResult = (TextView) findViewById(R.id.tv_no_result);
        ImageView ivBack = (ImageView) findViewById(R.id.btn_back);
        mEtSearchCamera = (CustomEditText) findViewById(R.id.et_search_camera);
        mEtSearchCamera.setFocusable(true);
        mEtSearchCamera.setFocusableInTouchMode(true);
        mEtSearchCamera.requestFocus();
        ivSearch.setOnClickListener(mOnClickListener);
        ivBack.setOnClickListener(mOnClickListener);
        mEtSearchCamera.addTextChangedListener(mWatcher);
        mEtSearchCamera.setOnEditorActionListener(mOnEditorActionListener);
        mCameraListView.setScrollingWhileRefreshingEnabled(false);
        mCameraListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(SearchCameraActivity.this,
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                mPage = 1;
                searchCamera();
            }
        });

        mCameraListView
                .setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

                    @Override
                    public void onLastItemVisible() {
                        if (!mPublicCameraAdapter.isLoadFinished()) {
                            refreshListByPage(++mPage);
                        } else {
                            Toast.makeText(SearchCameraActivity.this, "已加载全部!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        mPublicCameraAdapter = new PublicCameraAdapter(SearchCameraActivity.this,
                mListCamera);
        mCameraListView.getRefreshableView()
                .setAdapter(mPublicCameraAdapter);
        mCameraListView.setOnScrollListener(new PauseOnScrollListener(
                ImageLoader.getInstance(), false, true));
        mPublicCameraAdapter
                .setCameraOnClickCallBackListener(mCallBackListener);
    }

    private void showInputMethod(final boolean b) {
        final InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (b) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                        CLog.v("showInputMethod---" + b);
                        manager.showSoftInput(mEtSearchCamera, InputMethodManager.SHOW_IMPLICIT);
                        CLog.v("InputMethod is active " +manager.isActive(mEtSearchCamera));
                    }
                }
            }, 800);
        } else {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                CLog.v("showInputMethod---" + b);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                CLog.v("InputMethod is active " +manager.isActive(mEtSearchCamera));
            }
        }

    }

    @Override
    public void onBackPressed() {
        showInputMethod(false);
        super.onBackPressed();
    }

    private void searchCamera() {
        if (!isSearchAvailable()) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            mCameraListView.onRefreshComplete();
            return;
        }
        showInputMethod(false);
        mPage = 1;
        mSearchCameraMgmt.searchCamera(
                getApplicationContext(), mPage,
                Const.PAGE_SIZE, TYPE_ALL, mKeyWord, mSearchCallback);
    }

    private void refreshListByPage(int page) {
        if (!isSearchAvailable()) {
            return;
        }
        mSearchCameraMgmt.searchCamera(
                SearchCameraActivity.this, page,
                Const.PAGE_SIZE, TYPE_ALL, mKeyWord, mSearchCallback);
    }

    private boolean isSearchAvailable() {
        mKeyWord = mEtSearchCamera.getText().toString();
        if (!NetWorkUtils.isNetworkAvailable(SearchCameraActivity.this)) {
            showToast(getString(R.string.app_network_error));
            return false;
        }
        if (TextUtils.isEmpty(mKeyWord)) {
            showToast(getString(R.string.input_keyword));
            return false;
        }
        return true;
    }
}
