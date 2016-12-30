package com.lingyang.camera.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalRecordWrapper;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalRecord;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.MobileInterconnectResponse;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.AttentionPublicCameraMgmt;
import com.lingyang.camera.mgmt.IncreaseOrDecreasePlayerPeopleMgmt;
import com.lingyang.camera.mgmt.UnAttentionPublicCameraMgmt;
import com.lingyang.camera.util.FileUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.player.IPlayer;
import com.lingyang.sdk.player.IScreenRatio;
import com.lingyang.sdk.player.widget.LYPlayer;
import com.lingyang.sdk.player.widget.OnClosedListener;
import com.lingyang.sdk.player.widget.OnErrorListener;
import com.lingyang.sdk.player.widget.OnPreparedListener;
import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 文件名：MobilePlayerActivity
 * 描述：此类主要是观看手机直播界面，监听播放状态
 * 创建人：廖蕾
 * 时间：2015/11
 */
public class MobilePlayerActivity extends AppBaseActivity {

    private LYPlayer mLYPlayer;
    private IPlayer mPlayer;
    private String mRtmpAddr;
    private TextView mProgressText, mLiveOverText;
    private CircularFillableLoaders mProgressBar;
    private LinearLayout mProgressLayout;
    private Button mSnapShotBtn;
    private long mPreparedTime;
    private boolean mHasPlayed;
    private Camera mCamera;
    private IncreaseOrDecreasePlayerPeopleMgmt mPeopleMgmt;
    BaseCallBack<BaseResponse> mCallBack = new BaseCallBack<BaseResponse>() {
        @Override
        public void error(ResponseError object) {
        }

        @Override
        public void success(BaseResponse t) {
        }
    };

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_snapshot:
                    snapShot();
                    break;
                case R.id.iv_back:
                    onBackPressed();
                    break;
                case R.id.iv_msg:
                    //弹出参数信息
                    PopupMobilePlayParam playParamPopup;
                    playParamPopup = new PopupMobilePlayParam(MobilePlayerActivity.this, mLYPlayer,
                            mCamera, mPreparedTime + "", mRtmpAddr);
                    playParamPopup.setAnimationStyle(R.style.popup_mobile_live_anim_style);
                    playParamPopup.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.mobile_msg_bg));
                    playParamPopup.showAtLocation(mLYPlayer, Gravity.BOTTOM, 0, 150);
                    break;
                case R.id.layout_player:
//                    if (mSnapShotBtn.getVisibility() == View.VISIBLE) {
//                        mSnapShotBtn.setVisibility(View.GONE);
//                    } else {
//                        mSnapShotBtn.setVisibility(View.VISIBLE);
//                    }
                    break;
                case R.id.tb_mobile_attention:
                    if (mCbAttention.isChecked()) {
                        setAttention();
                    } else {
                        setUnAttention();
                    }
                    break;
                case R.id.btn_copy_hls_address:
                    String hls = mCamera.hls;
                    if (!TextUtils.isEmpty(hls)) {
                        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData.newPlainText("text", hls);
                        cmb.setPrimaryClip(ClipData.newPlainText("text", hls));
                        showToast(getString(R.string.hls_addr_copy_success));
                    } else {
                        showToast(getString(R.string.hls_addr_get_failed));
                    }
                    break;
                default:break;
            }
        }
    };
    private AttentionPublicCameraMgmt mAttentionPublicCameraMgmt;
    private UnAttentionPublicCameraMgmt mUnAttentionPublicCameraMgmt;
    private ImageView mMobileCoverIv;
    private AlphaAnimation mAlphaAnimation;
    private ImageView mMsgIv;
    private Button mBtnCopyHls;

    private void setUnAttention() {
        mUnAttentionPublicCameraMgmt.UnAttentionPublicCamara(MobilePlayerActivity.this,
                mCamera.cid, UnAttentionPublicCameraMgmt.MGMT_UNATTENTION, new BaseCallBack<Object>() {

                    @Override
                    public void error(ResponseError object) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCbAttention.setChecked(!mCbAttention.isChecked());
                            }
                        });
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
                                mCbAttention.setTextColor(getResources().getColor(R.color.white));
                                mCbAttention.setBackgroundColor(getResources().getColor(R.color.orange));
                                Intent mIntent = new Intent(Const.Actions.ACTION_IS_ATTENTION_REFRESH);
                                mIntent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                                        Const.IntentKeyConst.REFRESH_FROM_PUBLIC);
                                mIntent.putExtra(Const.IntentKeyConst.KEY_IS_ATTENTION, false);
                                mIntent.putExtra(Const.IntentKeyConst.KEY_CAMERA, mCamera);
                                mIntent.putExtra(Const.IntentKeyConst.KEY_CID, mCamera.cid);
                                LocalBroadcastManager.getInstance(MobilePlayerActivity.this).sendBroadcast(mIntent);
                                mCamera.is_followed = false;
                                showToast("已取消关注，" + mCamera.cname);
                            }
                        });
                    }
                });
    }

    private void setAttention() {
        mAttentionPublicCameraMgmt.AttentionPublicCamera(this,
                mCamera.cid, new BaseCallBack<Object>() {

                    @Override
                    public void error(ResponseError object) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCbAttention.setChecked(!mCbAttention.isChecked());
                            }
                        });
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
                                mCbAttention.setTextColor(getResources().getColor(R.color.text_white));
                                mCbAttention.setBackgroundColor(getResources().getColor(R.color.transparent));
                                Intent mIntent = new Intent(
                                        Const.Actions.ACTION_IS_ATTENTION_REFRESH);
                                mIntent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                                        Const.IntentKeyConst.REFRESH_FROM_PUBLIC);
                                mIntent.putExtra(Const.IntentKeyConst.KEY_IS_ATTENTION, true);
                                mIntent.putExtra(Const.IntentKeyConst.KEY_CAMERA, mCamera);
                                mIntent.putExtra(Const.IntentKeyConst.KEY_CID, mCamera.cid);
                                LocalBroadcastManager.getInstance(MobilePlayerActivity.this).
                                        sendBroadcast(mIntent);
                                showToast("已关注，" + mCamera.cname);
                                mCamera.is_followed = true;
                            }
                        });
                    }
                });
    }

    private RelativeLayout mRlAbove;
    private CheckBox mCbAttention;

    @Override
    public void onBackPressed() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mobile_player);
        mCamera = (Camera) getIntent().getSerializableExtra(Const.IntentKeyConst.KEY_CAMERA);
        mAttentionPublicCameraMgmt = (AttentionPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(AttentionPublicCameraMgmt.class);
        mUnAttentionPublicCameraMgmt = (UnAttentionPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(UnAttentionPublicCameraMgmt.class);
        mPeopleMgmt = (IncreaseOrDecreasePlayerPeopleMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(IncreaseOrDecreasePlayerPeopleMgmt.class);
        initView();
        loadMobileCover();
        play();
    }

    private void loadMobileCover() {
        if (mCamera!=null) {
            DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(true).cacheOnDisk(true)
                    .displayer(new RoundedBitmapDisplayer(0,RoundedBitmapDisplayer.CORNER_NONE))
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
            ImageLoader.getInstance().displayImage(mCamera.cover_url, mMobileCoverIv,
                    displayImageOptions, null);
        }
    }

    @Override
    protected void onPause() {
        mLYPlayer.pauseToBackground();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mLYPlayer.resumeFromBackground();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mLYPlayer.isPlaying()) {
            mLYPlayer.stop();
        }
        if (mAlphaAnimation!=null) {
            mAlphaAnimation.cancel();
        }
        super.onDestroy();
    }
    private void hideLoadCover() {
        mAlphaAnimation = new AlphaAnimation(1f, 0f);
        mAlphaAnimation.setDuration(500);
        mAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                CLog.d("onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                CLog.d("onAnimationEnd");
                mProgressBar.setVisibility(View.INVISIBLE);
                mMobileCoverIv.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                CLog.d("onAnimationRepeat");
            }
        });
        mMobileCoverIv.startAnimation(mAlphaAnimation);
        mProgressBar.startAnimation(mAlphaAnimation);
    }

    @Override
    protected void processMessage(MobileInterconnectResponse.Mobile mobileMessage) {
        super.processMessage(mobileMessage);
        if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CLOSE_PLAY_AND_LIVE)) {
            CLog.d("mobileMessage.message---"+mobileMessage.message);
            finish();
        }
    }
    /**
     * 播放rtmp视频流，设置监听
     */
    private void play() {
        mLYPlayer.setDataSource(mCamera.play_addr);
        mLYPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(int time) {
                hideLoadCover();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressLayout.setVisibility(View.GONE);
                        mMsgIv.setEnabled(true);
                        mBtnCopyHls.setEnabled(true);
                    }
                });
                mPreparedTime = time;
                mHasPlayed = true;
                mPeopleMgmt.doExe(MobilePlayerActivity.this, true, mCamera.cid, mCallBack);
                CLog.v("mobile-state--直播中");
            }
        });
        mLYPlayer.setOnClosedListener(new OnClosedListener() {
            @Override
            public void onClosed() {
                CLog.d("onClosed");

            }
        });
        mLYPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(int i, final String msg) {
                CLog.v("mobile-state--直播已结束");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mHasPlayed) {
                            mRlAbove.setBackgroundColor(getResources().getColor(R.color.half_transparent));
                            mLiveOverText.setVisibility(View.VISIBLE);
                            mLiveOverText.setText("直播已结束");
                        } else {
                            if (TextUtils.isEmpty(msg)) {
                                mProgressText.setText(getText(R.string.state_error));
                            } else {
                                if (!mHasNetWork) {
                                    mProgressText.setText(msg);
                                } else {
                                    mProgressBar.setVisibility(View.GONE);
                                    mProgressText.setText("直播已结束");
                                }
                            }
                        }
                    }
                });
                return false;
            }
        });
        mLYPlayer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mHasPlayed) {
            mPeopleMgmt.doExe(MobilePlayerActivity.this, false, mCamera.cid, mCallBack);
            mHasPlayed = false;
        }
    }

    private void initView() {
        mRtmpAddr = getIntent().getStringExtra(Const.IntentKeyConst.KEY_LIVE_RTMP);

        ImageView backIv = (ImageView) findViewById(R.id.iv_back);
        mMsgIv = (ImageView) findViewById(R.id.iv_msg);
        mMobileCoverIv = (ImageView) findViewById(R.id.iv_mobile_cover);
        mSnapShotBtn = (Button) findViewById(R.id.btn_snapshot);
        mCbAttention = (CheckBox) findViewById(R.id.tb_mobile_attention);
        mCbAttention.setOnClickListener(mOnClickListener);
        mLYPlayer = (LYPlayer) findViewById(R.id.layout_player);
        mRlAbove = (RelativeLayout) findViewById(R.id.rl_above);

        mProgressLayout = (LinearLayout) findViewById(R.id.layout_progressbar);
        mProgressBar = (CircularFillableLoaders) findViewById(R.id.progressbar);
        mProgressText = (TextView) findViewById(R.id.tv_progressbar);
        mLiveOverText = (TextView) findViewById(R.id.tv_liveover);
        mBtnCopyHls = (Button) findViewById(R.id.btn_copy_hls_address);
        mLiveOverText.setVisibility(View.GONE);
        mSnapShotBtn.setVisibility(View.GONE);
        backIv.setOnClickListener(mOnClickListener);
        mMsgIv.setOnClickListener(mOnClickListener);
        mSnapShotBtn.setOnClickListener(mOnClickListener);
        mLYPlayer.setOnClickListener(mOnClickListener);
        mBtnCopyHls.setOnClickListener(mOnClickListener);
        mMsgIv.setEnabled(false);
        mBtnCopyHls.setEnabled(false);

        mLYPlayer.setScreenRatio(IScreenRatio.TYPE_PLAYER_RATIO_PROP_BEST);
//        mLYPlayer.setFitScreen(true);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLYPlayer.getLayoutParams();
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        layoutParams.width = heightPixels * 3 / 4;
        layoutParams.height = heightPixels;
        int screenWidth = Utils.getScreenWidth(this);
        layoutParams.setMargins(-(heightPixels * 3 / 4 - screenWidth) / 2,
                0, -(heightPixels * 3 / 4 - screenWidth) / 2, 0);//裁剪左右
        mLYPlayer.setLayoutParams(layoutParams);
    }

    /**
     * 截图并保存
     */
    private void snapShot() {
        final long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        final String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                .format(date) + "_" + System.currentTimeMillis() % 1000;
        final String snapPath = FileUtil.getInstance().getSnapshotFile()
                .getAbsolutePath();
        mPlayer.snapshot(snapPath, name, new IPlayer.OnSnapshotListener() {
            @Override
            public void onSnapshotSuccess(final String savaFullPath) {
                showToast("截图已保存!");
                ThreadPoolManagerQuick.execute(new Runnable() {
                    @Override
                    public void run() {
                        boolean flg = LocalRecordWrapper.getInstance().addLocalMedia(
                                new LocalRecord(name, timestamp, 0, LocalRecord.TYPE_MEDIA_PHOTO,
                                        savaFullPath, savaFullPath, timestamp, mCamera.cid,
                                        LocalUserWrapper.getInstance().getLocalUser().getUid()));
                        CLog.v("addLocalMedia-mobile -flg:" + flg);
                    }
                });
            }

            @Override
            public void onSnapshotFail(int errorCode) {
                showToast("截图失败!" + errorCode);
            }
        });
    }
}
