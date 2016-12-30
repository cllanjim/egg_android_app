package com.lingyang.camera.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.base.utils.ThreadPoolManagerNormal;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalRecordWrapper;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalRecord;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.GetCameraSetResponse;
import com.lingyang.camera.entity.MobileInterconnectResponse;
import com.lingyang.camera.entity.RecordSection;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.exception.LogUtil;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.AttentionPublicCameraMgmt;
import com.lingyang.camera.mgmt.GetCameraSetMgmt;
import com.lingyang.camera.mgmt.GetRecordSegmentListMgmt;
import com.lingyang.camera.mgmt.IncreaseOrDecreasePlayerPeopleMgmt;
import com.lingyang.camera.mgmt.RecordMgmt;
import com.lingyang.camera.mgmt.UnAttentionPublicCameraMgmt;
import com.lingyang.camera.mgmt.UpdateCameraSetMgmt;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.ui.adapter.RecordDateListAdapter;
import com.lingyang.camera.ui.widget.MutiColorDrawable;
import com.lingyang.camera.ui.widget.MyHorizontalScrollView;
import com.lingyang.camera.util.DateTimeUtil;
import com.lingyang.camera.util.FileUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.player.IPlayer;
import com.lingyang.sdk.player.widget.LYPlayer;
import com.lingyang.sdk.player.widget.OnClosedListener;
import com.lingyang.sdk.player.widget.OnErrorListener;
import com.lingyang.sdk.player.widget.OnLocalRecordListener;
import com.lingyang.sdk.player.widget.OnPlayProgressListener;
import com.lingyang.sdk.player.widget.OnPreparedListener;
import com.lingyang.sdk.player.widget.OnSeekCompleteListener;
import com.lingyang.sdk.player.widget.OnSnapshotListener;
import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.lingyang.camera.entity.CameraResponse.MyCameras.Camera.TYPE_PUBLIC;

/**
 * 文件名：PlayerActivity
 * 描述：
 * 此类是看摄像机直播，录像，截屏，清晰度选择等功能
 * 注意： VideoPlayerView.setPlayerType(IPlayer.TPPE_LIVE); type区分直播和录像
 * 创建人：廖蕾
 * 时间：2015/9/21
 */
public class PlayerActivity extends AppBaseActivity {
    static final int ADJUST_NUMBER = 10000;
    static final int TOTAL_SPAN = 86400;
    TextView mTitleTextView, mRecordTimeLineLeftSpan, mRecordTimeLineRightSpan,
            mLivingStatusTextView, mRecordSeekTextView, mRecordTimeTextView,
            mRecordCurrentDayTextView, mRecordTodayTextView, mLiveTodayTextView, mWatchedNumTextView, mResolutionTextView;
    Button mBackImageView, mSnapshotButton, mRecordSnapshotButton, mVoiceButton, motherSnapshotButton, mBackToLiveButton;
    ImageView mCoverImageView, mSectionImageView, mBackFromFullScreenImageView, mReplayImg,
            mPlayParamImageView;
    View mHeaderView;
    ToggleButton mLiveRecordButton, mRecordVolumeButton, mPlayerChangeButton,
            mSetFullScreenButton, mRecordDateToggleButton, mPublicLiveVolumeButton, mMineLiveVolumeButton,
            mPublicLiveRecordButton, mRecordRecordButton;
    CheckBox mAttentionButton;
    RelativeLayout mRecordLinearLayout, playParamRl, mLiveMineParamsLayout, mLiveParamsLayout;
    LinearLayout mCameraStatusLinearLayout;
    MyHorizontalScrollView mPlayerHorizontalScrollView, mTimeLineMyHorizontalScrollView;
    RelativeLayout mLiveRelativeLayout, mVisionSeekRelativeLayout, mOtherLiveRelativeLayout;
    CircularFillableLoaders mProgressBar;
    ImageView line;
    SeekBar mVisonSeekBar;
    LYPlayer mLYPlayer;
    FrameLayout mVideoViewFrameLayout, mPlayerContainerFrameLayout;
    Camera mCamera;
    LocalRecord mLocalMediaRecord;
    Date mRecordCurrentDate;
    Date mRecordPreDate;
    String mCid, mCname, mUserNickName, mImageCover, mRtmpAddr;
    boolean mIsRecord, mIsPublic, mIsShareLive, mHasPlayed, mHavingPlayRecord;
    long mRecordDuration, mBeginTime, mTotalPreparedTime, mCurrentTime, mCurrentSelFrom, mCurrentSelTo, mSeekTimeSpan, mTabCurrentTime = 0, temp;
    int mOnlineNums, mViewTimes, mPlayType, mResolutionType;
    boolean mIsSeeking = false;
    Animation mAnimation;
    IncreaseOrDecreasePlayerPeopleMgmt mPeopleMgmt;
    AttentionPublicCameraMgmt mAttentionPublicCameraMgmt;
    UnAttentionPublicCameraMgmt mUnAttentionPublicCameraMgmt;
    Runnable mUpdateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLiveRecordButton.isChecked() || mRecordRecordButton.isChecked()
                    || mPublicLiveRecordButton.isChecked()) {
                mRecordTimeTextView.setText(String.format("正在本地录像%s",
                        DateTimeUtil.timeStampToDateByFormat(mRecordDuration++, "mm:ss")));
            }
            mHandler.postDelayed(mUpdateTimeRunnable, 1000);
        }
    };
    Runnable mUpdateRealTimeRunnable = new Runnable() {
        @Override
        public void run() {
            long l = System.currentTimeMillis();
            String time = DateTimeUtil.timeStampToDateLong(l / 1000);
//            CLog.v("today--" + time);
            String[] times = time.split(" ");
            mRecordTodayTextView.setText(Html.fromHtml("<font color='#ffffff'>" + times[1] +
                    "</font> <br/> <font color='#ffffff'>" + times[0] + "</font>"));
            mLiveTodayTextView.setText(Html.fromHtml("<font color='#ffffff'>" + times[1] +
                    "</font> <br/> <font color='#ffffff'>" + times[0] + "</font>"));
            mHandler.postDelayed(mUpdateRealTimeRunnable, 1000);
        }
    };
    OnPlayProgressListener mOnPlayRecordProgressListener = new OnPlayProgressListener() {
        @Override
        public void onPlayProgress(int playProgress) {
            CLog.d("playProgress " + playProgress + " mCurrentSelFrom " + mCurrentSelFrom);
            temp = playProgress + mCurrentSelFrom;
            if (!mHasNetWork) {
                mLYPlayer.stop();
                mProgressBar.setVisibility(View.GONE);
                mLivingStatusTextView.setText("网络连接失败");
                mReplayImg.setVisibility(View.VISIBLE);
                return;
            }
            if (!mIsSeeking) {
                CLog.v("onPlayPositionChange:" + playProgress);
                mRecordSeekTextView.setText(DateTimeUtil
                        .timeStampToDateByFormat(playProgress + mCurrentSelFrom, "HH:mm:ss"));
                int scrollX = mSectionImageView.getWidth() * playProgress
                        / TOTAL_SPAN;
                mTimeLineMyHorizontalScrollView.scrollTo(scrollX, 0);
                if (playProgress == mCurrentSelTo) {
                    finish();
                }
            }
        }
    };
    PlayParamPopup playParamPopup;
    Runnable mShowProgressBarRunnable = new Runnable() {
        @Override
        public void run() {
            mProgressBar.setVisibility(View.VISIBLE);
            mLivingStatusTextView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mIsHide = false;
    Runnable mHideTopAndBottomRunnable = new Runnable() {
        @Override
        public void run() {
            mIsHide = true;
//            mLiveMineParamsLayout.setVisibility(View.GONE);
//            mLiveParamsLayout.setVisibility(View.GONE);
            mRecordLinearLayout.setVisibility(View.GONE);
            mHideTopAndBottomScaleAnimation = new ScaleAnimation(
                    1, 1, 1, 1.5f,
                    Animation.RELATIVE_TO_PARENT, 0.5f,
                    Animation.RELATIVE_TO_PARENT, 0.5f);
            mHideTopAndBottomScaleAnimation.setDuration(300);
            mHideTopAndBottomScaleAnimation.setInterpolator(new AccelerateInterpolator());
            mHideTopAndBottomScaleAnimation.setFillAfter(false);
            mHideTopAndBottomScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    CLog.d("hide onAnimationStart");
                    mLYPlayer.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    CLog.d("hide onAnimationEnd");
                    mLiveMineParamsLayout.setVisibility(View.GONE);
                    mLiveParamsLayout.setVisibility(View.GONE);
                    mLYPlayer.setClickable(true);
                    CLog.d("gone");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    CLog.d("hide onAnimationRepeat");
                }
            });
            mLiveMineParamsLayout.startAnimation(mHideTopAndBottomScaleAnimation);
            mLiveParamsLayout.startAnimation(mHideTopAndBottomScaleAnimation);
        }
    };
    View.OnTouchListener mOnTalkListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mHandler.removeCallbacks(mHideTopAndBottomRunnable);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                v.setBackgroundResource(R.drawable.speak_pressed);
                v.setBackgroundColor(getResources().getColor(R.color.orange));
                mMineLiveVolumeButton.setChecked(true);
                CLog.d("onTouch event startTalk---");
                mLYPlayer.startTalk();
                return false;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                v.setBackgroundResource(R.drawable.speak);
                v.setBackgroundColor(getResources().getColor(R.color.transparent));
                mMineLiveVolumeButton.setChecked(false);
                CLog.d("onTouch event stopTalk---");
                mLYPlayer.stopTalk();
                return true;
            }
            return false;
        }
    };
    private Button mCopyButton;
    private String mHls;
    private GetCameraSetMgmt mGetCameraSetMgmt;
    private UpdateCameraSetMgmt mUpdateCameraSetMgmt;
    private GetCameraSetResponse.CameraSet mCameraSet = new GetCameraSetResponse.CameraSet();
    /**
     * 获取设置回调
     */
    BaseCallBack<GetCameraSetResponse.CameraSet> mGetCallBack = new BaseCallBack<GetCameraSetResponse.CameraSet>() {

        @Override
        public void error(ResponseError object) {
            if (object != null) {
                CLog.e("getset---" + object.error_code + object.error_msg);
                showToast(object.error_code + object.error_msg);
            }
        }

        @Override
        public void success(final GetCameraSetResponse.CameraSet t) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (t != null) {
                        mCameraSet = t;
                        mResolutionTextView.setText(getText(t.getRate()));
                        mResolutionTextView.setEnabled(true);
                    }
                }
            });
        }
    };
    /**
     * 保存设置回调
     */
    BaseCallBack<BaseResponse> mUpdateCallBack = new BaseCallBack<BaseResponse>() {

        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            mCameraSet.rate = mResolutionType;
            if (object != null) {
                showToast(object.error_code + object.error_msg);
            }
        }

        @Override
        public void success(BaseResponse t) {
            showToast((String) getText(R.string.set_suc));
            mResolutionTextView.setText(getText(mCameraSet.getRate()));
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
        }
    };
    private boolean mIsDebug = true;
    private AlphaAnimation mHideCoverAlphaAnimation;
    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(final int time) {
            CLog.v("===playRecord onPrepared: " + time);
            savePlayerLog("playRecord onPrepared " + time);
            mHandler.removeCallbacks(mShowProgressBarRunnable);
            mLYPlayer.setOnPlayProgressListener(mOnPlayRecordProgressListener);
            mLYPlayer.getCurrentPosition();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    prepareRecordView(time);
                    mIsSeeking = false;
                }
            });
        }
    };
    private Runnable mUnAttentionRunnable = new Runnable() {

        @Override
        public void run() {
            mAttentionButton.setText(String.format("%d人正在关注", --mCamera.followed));
            mAttentionButton.setTextColor(getResources().getColor(R.color.white));
            mAttentionButton.setBackgroundColor(getResources().getColor(R.color.orange));
            Intent mIntent = new Intent(Const.Actions.ACTION_IS_ATTENTION_REFRESH);
            mIntent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                    Const.IntentKeyConst.REFRESH_FROM_PUBLIC);
            mIntent.putExtra(Const.IntentKeyConst.KEY_IS_ATTENTION, false);
            mIntent.putExtra(Const.IntentKeyConst.KEY_CAMERA, mCamera);
            mIntent.putExtra(Const.IntentKeyConst.KEY_CID, mCamera.cid);
            LocalBroadcastManager.getInstance(PlayerActivity.this).sendBroadcast(mIntent);
            line.setVisibility(View.GONE);
            mCamera.is_followed = false;
            showToast("已取消关注，" + mCamera.cname);
        }
    };
    private BaseCallBack<BaseResponse> mCallBack = new BaseCallBack<BaseResponse>() {
        @Override
        public void error(ResponseError object) {
            CLog.d("increase watch number error---");
        }

        @Override
        public void success(BaseResponse baseResponse) {
            CLog.d("increase watch number success---");
        }
    };
    private Runnable mAttentionRunnable = new Runnable() {

        @Override
        public void run() {
            mAttentionButton.setText(String.format("%d人已关注", ++mCamera.followed));
            mAttentionButton.setTextColor(getResources().getColor(R.color.text_white));
            mAttentionButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            Intent mIntent = new Intent(
                    Const.Actions.ACTION_IS_ATTENTION_REFRESH);
            mIntent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                    Const.IntentKeyConst.REFRESH_FROM_PUBLIC);
            mIntent.putExtra(Const.IntentKeyConst.KEY_IS_ATTENTION, true);
            mIntent.putExtra(Const.IntentKeyConst.KEY_CAMERA, mCamera);
            mIntent.putExtra(Const.IntentKeyConst.KEY_CID, mCamera.cid);
            LocalBroadcastManager.getInstance(PlayerActivity.this).
                    sendBroadcast(mIntent);
            line.setVisibility(View.VISIBLE);
            showToast("已关注，" + mCamera.cname);
            mCamera.is_followed = true;
        }
    };
    private List<RecordSection.Section> mSections;
    private OnSeekCompleteListener mOnSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekSuccess(int i) {
//            mIsSeeking = false;
            CLog.d("---seek success---" + i);
            mReplayImg.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mLivingStatusTextView.setText("正在加载中");
            mRecordRecordButton.setEnabled(false);
            mRecordSnapshotButton.setEnabled(false);
            mRecordVolumeButton.setEnabled(false);
        }

        @Override
        public void onSeekError(LYException e) {
            CLog.d("---seek LYException---" + e.getMessage());
            mProgressBar.setVisibility(View.GONE);
            if (!NetWorkUtils.isNetworkAvailable(getApplicationContext())) {
                mIsSeeking = false;
                mReplayImg.setVisibility(View.VISIBLE);
                mLivingStatusTextView.setText(getString(R.string.app_network_error));
            } else {
                mReplayImg.setVisibility(View.GONE);
                mLivingStatusTextView.setText("");

                //如果seek的地方没有录像，尝试播放下一时间段的录像
                boolean hasNextRecord = false;
                long currentSeekTimestamp = mSeekTimeSpan + mCurrentSelFrom;
                CLog.d("currentSeekTimestamp---" + currentSeekTimestamp);
                for (RecordSection.Section section : mSections) {
                    if (section.from > currentSeekTimestamp) {
                        mSeekTimeSpan = section.from - mCurrentSelFrom;
                        mHandler.post(mSeekRunnable);
                        hasNextRecord = true;
                        break;
                    }
                }
                if (!hasNextRecord) {
                    mIsSeeking = false;
                }
            }
        }
    };
    private GetRecordSegmentListMgmt.IRecordSegmentListCallBackListener mIRecordSegmentListCallBackListener;
    private ImageView mLine2;
    private ImageView mRecordExitImageView;
    private long mTouchUpStartTime;
    private RelativeLayout mResolutionRl;
    private long mTempBeginTime;
    Runnable mSeekRunnable = new Runnable() {
        @Override
        public void run() {
            CLog.d("seekTo---" + mSeekTimeSpan);
            if (mIsRecord) {
                mRecordRecordButton.setChecked(false);
            }
            mIsSeeking = true;
            CLog.d("mCurrentSelFrom---" + mCurrentSelFrom);
            CLog.v("mRecordPlayer.seek:" + mSeekTimeSpan);
            long cannotSeekTime = mTempBeginTime - mCurrentSelFrom;
            if (mIsRecordStart) {
                if (mTempBeginTime > mCurrentSelFrom && mSeekTimeSpan < cannotSeekTime) {
                    //重新绑定当天历史录像的问题
                    mLYPlayer.seekTo((int) cannotSeekTime);
                } else {
                    mLYPlayer.seekTo((int) mSeekTimeSpan);
                }
            }
        }
    };
    RecordDateListAdapter.OnSelDateCallback mOnSelDateCallback = new RecordDateListAdapter.OnSelDateCallback() {
        @Override
        public void onSelDate(String dateString, Date date, String preDateString, Date preDate) {
            CLog.v("onSelDate：onSelDate");
            if (DateTimeUtil.daysBetween(date, new Date()) == 0) {
                initRecordTime();
            } else {
                mRecordCurrentDate = date;
                mRecordPreDate = preDate;
                mRecordCurrentDayTextView.setText(dateString);
                mRecordCurrentDayTextView.setSelected(true);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mRecordCurrentDate);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                mCurrentSelFrom = mRecordCurrentDate.getTime() / 1000;
                mCurrentSelTo = calendar.getTime().getTime() / 1000;
                mSections = null;
            }
            playRecord();
        }
    };
    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.vpv_live:
//                   mLiveMineParamsLayout.setVisibility(View.GONE);分享给我，私有
//                    mLiveParamsLayout.setVisibility(View.GONE);公众直播
//                    mRecordLinearLayout.setVisibility(View.VISIBLE);历史录像界面
                    mHandler.removeCallbacks(mHideTopAndBottomRunnable);
                    if (!isMineCamera()) {
//                        mLiveParamsLayout.setVisibility(
//                                mLiveParamsLayout.getVisibility() == View.GONE
//                                        ? View.VISIBLE : View.GONE);
                        if (mIsHide) {
                            showTopAndBottomAnimation(mLiveParamsLayout);
                        } else {
                            mHandler.post(mHideTopAndBottomRunnable);
                        }
                    } else {
                        if (mPlayType == IPlayer.TYPE_RECORD) {
                            mRecordLinearLayout.setVisibility(
                                    mRecordLinearLayout.getVisibility() == View.GONE
                                            ? View.VISIBLE : View.GONE);
                        } else {
                            if (mIsHide) {
                                showTopAndBottomAnimation(mLiveMineParamsLayout);
                            } else {
                                mHandler.post(mHideTopAndBottomRunnable);
                            }
//                            mLiveMineParamsLayout.setVisibility(
//                                    mLiveMineParamsLayout.getVisibility() == View.GONE
//                                            ? View.VISIBLE : View.GONE);
                        }
                    }
                    break;
                case R.id.btn_live_snapshot:
                case R.id.btn_record_snapshot:
                case R.id.btn_other_live_snapshot:
                    snapShot(true, false, true);
                    break;
                case R.id.rl_play_param:
                case R.id.iv_play_param:
                    if (mHasPlayed) {
                        showParamPopUp();
                    }
                    break;
                case R.id.img_replay:
                    if (mHavingPlayRecord) {
                        playRecord();
                    } else {
                        closeLive();
                        mProgressBar.setVisibility(View.VISIBLE);
                        playLive();
                    }
                    break;
                case R.id.iv_record_exit:
                case R.id.iv_player_backfrom_fullscreen:
                    onBackPressed();
                    break;
                case R.id.btn_backlive:
                    if (mCamera.state == Camera.DEVICE_STATUS_PREPARED
                            || mCamera.state == Camera.DEVICE_STATUS_LIVING) {
                        mLiveMineParamsLayout.setVisibility(View.VISIBLE);
                        mWatchedNumTextView.setVisibility(View.VISIBLE);
                        mHandler.removeCallbacks(mHideTopAndBottomRunnable);
                        closeRecordPlay();
                        resetPlayer();
                        playLive();
                    } else {
                        showToast("设备" + mCamera.getStatus(mCamera.state));
                        return;
                    }
                    break;
                case R.id.tb_change_player:
                    mHandler.removeCallbacks(mHideTopAndBottomRunnable);
                    closeLive();
                    resetPlayer();
                    playRecord();
                    break;
                case R.id.tb_attention:
                    if (mAttentionButton.isChecked()) {
                        setAttention();
                    } else {
                        setUnAttention();
                    }
                    break;
                case R.id.tv_resolution:
                    mHandler.removeCallbacks(mHideTopAndBottomRunnable);
                    //选择清晰度
                    PopupResolution popupResolution = new PopupResolution(PlayerActivity.this);
                    popupResolution.setWidth(mResolutionTextView.getWidth());
                    popupResolution.setOnResolutionListener(new PopupResolution.OnResolutionListener() {
                        @Override
                        public void callBack(int type) {
                            if (type != mCameraSet.rate) {
                                mResolutionType = mCameraSet.rate;
                                mCameraSet.rate = type;
                                updateCameraSet();
                            }
                        }
                    });
                    popupResolution.selectedItem(mCameraSet.rate - 1);
                    popupResolution.showAsDropDown(mResolutionTextView);
//                    popupResolution.showAsDropDown(
//                            mHeaderView,
//                            Utils.getScreenWidth(PlayerActivity.this) / 2,
//                            Utils.dip2px(PlayerActivity.this, 118));
                    break;
                case R.id.tv_record_currentday:
                    mRecordDateToggleButton.setChecked(!mRecordDateToggleButton.isChecked());
                    if (mRecordCurrentDayTextView.isSelected())
                        return;
                    mRecordCurrentDayTextView.setSelected(true);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mRecordCurrentDate);
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    mCurrentSelFrom = mRecordCurrentDate.getTime() / 1000;
                    mCurrentSelTo = calendar.getTime().getTime() / 1000;
                    playRecord();
                    break;
                case R.id.btn_copy_play_address:
                    if (!TextUtils.isEmpty(mHls)) {
                        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData.newPlainText("text", mHls);
                        cmb.setPrimaryClip(ClipData.newPlainText("text", mHls));
                        showToast(getString(R.string.hls_addr_copy_success));
                    } else {
                        showToast(getString(R.string.hls_addr_get_failed));
                    }
                    break;
                default:
                    break;
            }
        }
    };
    OnCheckedChangeListener mOnCheckChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.tb_live_record:
                case R.id.tb_other_live_record:
//                     从录像中存本地录像
                case R.id.tb_record_record:
                    if (isChecked) {
                        startLocalRecord();
                    } else {
                        stopLocalRecord();
                    }
                    break;
                case R.id.tb_record_seldate:
                    if (isChecked) {
                        mHandler.removeCallbacks(mHideTopAndBottomRunnable);
                        RecordDateSelPopup recordDateSelPopup = new RecordDateSelPopup(getApplicationContext(), mRecordCurrentDate);
                        recordDateSelPopup.showAsDropDown(mRecordDateToggleButton, 0, 0);
                        recordDateSelPopup.setOnResultCallback(mOnSelDateCallback);
                        recordDateSelPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                mRecordDateToggleButton.setChecked(false);
                            }
                        });
                    }
                    break;
                case R.id.tb_live_public_volume:
                case R.id.tb_live_mine_volume:
                case R.id.tb_record_vol:
                    if (isChecked) {
                        mLYPlayer.mute();
                    } else {
                        mLYPlayer.unmute();
                    }
//                    if (isChecked) {
//                        startLocalRecord();
//                    } else {
//                        stopLocalRecord();
//                    }
                    break;
                case R.id.btn_set_fullscreen:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    break;
                default:
                    break;
            }
        }
    };
    private boolean mIsRecordStart;
    private ScaleAnimation mShowScaleAnimation;
    private ScaleAnimation mHideTopAndBottomScaleAnimation;

    private void resetPlayer() {
        mIsRecordStart = false;
        if (mHideTopAndBottomScaleAnimation != null) {
            mHideTopAndBottomScaleAnimation.cancel();
        }
        if (mShowScaleAnimation != null) {
            mShowScaleAnimation.cancel();
        }
        CLog.d("====reset");
        mLYPlayer.reset();
    }

    private void showTopAndBottomAnimation(final ViewGroup viewGroup) {
        mIsHide = false;
        viewGroup.setVisibility(View.VISIBLE);
        mShowScaleAnimation = new ScaleAnimation(
                1, 1, 1.5f, 1f,
                Animation.RELATIVE_TO_PARENT, 0.5f,
                Animation.RELATIVE_TO_PARENT, 0.5f);
        mShowScaleAnimation.setDuration(300);
        mShowScaleAnimation.setInterpolator(new AccelerateInterpolator());
        mShowScaleAnimation.setFillAfter(false);
        mShowScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                CLog.d("onAnimationStart");
                mLYPlayer.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                CLog.d("onAnimationEnd");
                mLYPlayer.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                CLog.d("onAnimationRepeat");
            }
        });
        viewGroup.startAnimation(mShowScaleAnimation);
    }

    private void hideTopAndBottom() {
        mHandler.postDelayed(mHideTopAndBottomRunnable, 5000);
    }

    private void initRecordSegmentListCallBackListener() {
        if (mIRecordSegmentListCallBackListener == null) {
            mIRecordSegmentListCallBackListener
                    = new GetRecordSegmentListMgmt.IRecordSegmentListCallBackListener() {
                @Override
                public void onResult(RecordSection result) {
                    if (!mHasNetWork || result == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);
                                mLivingStatusTextView.setText("网络连接失败");
                                mReplayImg.setVisibility(View.VISIBLE);
                            }
                        });
                        return;
                    }
                    if (mPlayType!= IPlayer.TYPE_RECORD) {
                        return;
                    }
                    CLog.v("result:" + result.toGson());
                    savePlayerLog("result:" + result.toGson());
                    mSections = RecordMgmt
                            .getSectionListByTimeStamp(result,
                                    mCurrentSelFrom, mCurrentSelTo);
                    if (mSections == null || mSections.size() == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);
                                mLivingStatusTextView.setText("");
                                showToast("录像列表为空！");
                            }
                        });
                    } else {
                        mHavingPlayRecord = true;
                        initRecordProgressBar(mSections);
                        long playTime = 0;
                        long shouldBackTime;
                        if (mTabCurrentTime != 0) {
                            shouldBackTime = mTabCurrentTime;
                            mTabCurrentTime = 0;
                            CLog.d("----1");
                        } else {
                            long nowTimeStamp = System.currentTimeMillis() / 1000;
                            long dayBetweenToday = (nowTimeStamp - mCurrentSelFrom) / TOTAL_SPAN;
                            CLog.d("dayBetweenToday---" + dayBetweenToday);
                            //如果是当天，从当前时间的前一个小时开始播放
                            if (dayBetweenToday == 0) {
                                shouldBackTime = (mCurrentSelTo - 3600)
                                        > mCurrentSelFrom ? (mCurrentSelTo - 3600) : mCurrentSelFrom;
                            } else {
                                shouldBackTime = nowTimeStamp - dayBetweenToday * TOTAL_SPAN - 3600;
                            }
                            CLog.d("mCurrentSelFrom2----" + mCurrentSelFrom);
                        }
                        String play_addr = result.play_addr;
                        CLog.d("result.play_addr---" + play_addr);
//                        String substring = play_addr.substring(0, play_addr.lastIndexOf("="));
//                        CLog.d("substring---" + substring);
//                        String recordUrl = String.format("%s=%s&cid=%s", substring, playTime, result.cid);

                        //&begin=1467820800&end=1467854911&play=1467820800
                        //=======================================================================
                        String tokenString = play_addr.substring(0, play_addr.indexOf("begin=") + "begin=".length());
                        CLog.d("tokenString---" + tokenString);
                        String tempBeginTimeString = play_addr.substring(play_addr.indexOf("begin=") + "begin=".length(), play_addr.indexOf("&end"));
                        String endTime = play_addr.substring(play_addr.indexOf("end=") + "end=".length(), play_addr.indexOf("&play"));
                        CLog.d("endTime---" + endTime);
                        try {
                            //重新绑定的当天 返回的开始时间begin
                            mTempBeginTime = Long.valueOf(tempBeginTimeString);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return;
                        }
                        CLog.d("mTempBeginTime --- " + mTempBeginTime);
                        CLog.d("mCurrentSelFrom --- " + mCurrentSelFrom);
                        CLog.d("shouldBackTime --- " + shouldBackTime);
                        if (mTempBeginTime > mCurrentSelFrom && shouldBackTime < mTempBeginTime) {
                            //如果返回地址中的begin大于当天的零点，说明是重新绑定的
                            //如果应该倒流的时间
                            playTime = mTempBeginTime;
                        } else {
                            playTime = shouldBackTime;
                        }
                        CLog.v("playertime--" + playTime + "---currenttime---" + mTabCurrentTime);
                        String recordUrl = String.format("%s%s&end=%s&play=%s&cid=%s",
                                tokenString, mCurrentSelFrom, endTime, playTime, result.cid);
                        //=======================================================================
                        CLog.d("---" + recordUrl);
                        mLYPlayer.setDataSource(recordUrl);
//                        topvdn://topvdn.public.cn?protocolType=3&token=1003525_3356491776_1466576986_3ccf39046bbb095a99813d7209c44077&begin=1466438400&end=1466490584&play=1466486984&cid=1003525
                        mLYPlayer.start();
                        CLog.d("===start play record");
                        mIsRecordStart = true;
                        final long finalPlayTime = playTime;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.VISIBLE);
                                mLivingStatusTextView.setVisibility(View.VISIBLE);
                                mLivingStatusTextView.setText(String.format("时光正在倒流至\r\n%s",
                                        DateTimeUtil.timeStampToDateByFormat(
                                                finalPlayTime, "yyyy-MM-dd HH:mm:ss")));
                            }
                        });
                    }
                }
            };
        }
    }

    private void initRecordProgressBar(List<RecordSection.Section> sections) {
        List<MutiColorDrawable.MutiColorEntity> list = new ArrayList<MutiColorDrawable.MutiColorEntity>();
        list.add(new MutiColorDrawable.MutiColorEntity(0, Color
                .parseColor("#cccccc")));
        for (RecordSection.Section sectionData : sections) {
            if (sectionData == null)
                return;
            float fromPercent = (100 * (float) (sectionData.from - mCurrentSelFrom) / TOTAL_SPAN);
            float endPercent = (100 * (float) (sectionData.to - mCurrentSelFrom) / TOTAL_SPAN);
            list.add(new MutiColorDrawable.MutiColorEntity(fromPercent, Color
                    .parseColor("#ff6c00")));
            list.add(new MutiColorDrawable.MutiColorEntity(endPercent, Color
                    .parseColor("#cccccc")));
            CLog.v("sectionData.from:" + sectionData.from + " -sectionData.to:"
                    + sectionData.to + " -fromPercent:" + fromPercent
                    + " -endPercent:" + endPercent);
        }
        if (list.size() > 0) {
            final MutiColorDrawable colorDrawable = new MutiColorDrawable(
                    list.toArray(new MutiColorDrawable.MutiColorEntity[list
                            .size()]));
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSectionImageView.setImageDrawable(colorDrawable);
                }
            });
        }

    }

    private void updateCameraSet() {
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        mUpdateCameraSetMgmt.UpdateCameraSet(getApplicationContext(), mCamera.cid, mCameraSet,
                    mUpdateCallBack);
    }

    /**
     * 关注该摄像机
     */
    public void setAttention() {
        mAttentionPublicCameraMgmt.AttentionPublicCamera(getApplicationContext(),
                mCamera.cid, new BaseCallBack<Object>() {

                    @Override
                    public void error(ResponseError object) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAttentionButton.setChecked(!mAttentionButton.isChecked());
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
                        runOnUiThread(mAttentionRunnable);
                    }
                });
    }

    /**
     * 取消关注该摄像机
     */
    public void setUnAttention() {
        mUnAttentionPublicCameraMgmt.UnAttentionPublicCamara(getApplicationContext(),
                mCamera.cid, UnAttentionPublicCameraMgmt.MGMT_UNATTENTION, new BaseCallBack<Object>() {

                    @Override
                    public void error(ResponseError object) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAttentionButton.setChecked(!mAttentionButton.isChecked());
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
                        runOnUiThread(mUnAttentionRunnable);
                    }
                });
    }

    private void changeToLivePlayer() {
        CLog.v("changeToLivePlayer");
        mPlayType = IPlayer.TYPE_QSTP;
//        closeRecordPlay();
        if (mCamera.type == Camera.TYPE_PUBLIC
                && !isMineCamera()) {
            mWatchedNumTextView.setVisibility(View.VISIBLE);
        }
        mCoverImageView.setVisibility(View.VISIBLE);
        mReplayImg.setVisibility(View.GONE);
        mPlayParamImageView.setVisibility(View.VISIBLE);
        mRecordLinearLayout.setVisibility(View.GONE);
        mRecordTimeTextView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(78);
        mLivingStatusTextView.setVisibility(View.VISIBLE);
        mReplayImg.setVisibility(View.GONE);
        reInitView();
    }

    private void changeToRecordPlayer() {
        CLog.v("changeToRecordPlayer");
        mPlayType = IPlayer.TYPE_RECORD;
//        closeRecordPlay();
//        closeLive();
        mCoverImageView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(78);
        mReplayImg.setVisibility(View.GONE);
        mLiveParamsLayout.setVisibility(View.GONE);
        mLiveMineParamsLayout.setVisibility(View.GONE);
        mLiveRelativeLayout.setVisibility(View.GONE);
        mOtherLiveRelativeLayout.setVisibility(View.GONE);
        mWatchedNumTextView.setVisibility(View.GONE);
        mRecordLinearLayout.setVisibility(View.VISIBLE);
        mSectionImageView.setImageDrawable(new ColorDrawable(getResources().getColor(R.color.bg_gray)));
        mPlayParamImageView.setVisibility(View.GONE);
        mRecordTimeTextView.setVisibility(View.INVISIBLE);
        mLivingStatusTextView.setVisibility(View.VISIBLE);
        initProgressbarPos();
        reInitView();
    }

    /**
     * 开始录像
     */
    private void startLocalRecord() {
        CLog.v("startLocalRecord()");
        final long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        mRecordTimeTextView.setVisibility(View.INVISIBLE);
        mLiveRecordButton.setEnabled(false);
//        mRecordTimeTextView.setText(String.format("正在本地录像%s",
//                DateTimeUtil.timeStampToDateByFormat(0l,
//                        "mm:ss")));
        final String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                .format(date) + "_" + System.currentTimeMillis() % 1000;
        final String snapPath = FileUtil.getInstance().getSnapshotFile()
                .getAbsolutePath();
        final String recordPath = FileUtil.getInstance().getRecordFile()
                .getAbsolutePath()
                + "/" + name + ".mp4";
        mLYPlayer.snapshot(snapPath, name, new OnSnapshotListener() {
            @Override
            public void onSnapshotSuccess(final String saveFullPath) {
                CLog.v("onSnapshotSuccess savaFullPath-" + saveFullPath);
                ThreadPoolManagerQuick.execute(new Runnable() {
                    @Override
                    public void run() {
                        mLocalMediaRecord = new LocalRecord(name, timestamp, 0,
                                LocalRecord.TYPE_MEDIA_VIDEO, recordPath,
                                saveFullPath, timestamp, mCid, LocalUserWrapper
                                .getInstance().getLocalUser().getUid());
                        mBeginTime = System.currentTimeMillis();

                        mLYPlayer.startLocalRecord(recordPath);
                        mLYPlayer.setLocalRecordListener(new OnLocalRecordListener() {
                            @Override
                            public void onRecordStart() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mIsRecord = true;
                                        mRecordTimeTextView.setVisibility(View.VISIBLE);
                                        CLog.v("recordtime--start");
                                        mLiveRecordButton.setEnabled(true);
                                        startRecordAnimation();
                                        mHandler.removeCallbacks(mUpdateTimeRunnable);
                                        mHandler.post(mUpdateTimeRunnable);
                                    }
                                });
                            }

                            @Override
                            public void onRecordSizeChange(long l, long l1) {

                            }

                            @Override
                            public void onRecordError(LYException e) {
                                showToast("视频录制异常");
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        mIsRecord = false;
                                        mRecordTimeTextView.setVisibility(View.INVISIBLE);
                                        mLiveRecordButton.setChecked(false);
                                        mRecordRecordButton.setChecked(false);
                                        mPublicLiveRecordButton.setChecked(false);
                                    }
                                });
                            }

                            @Override
                            public void onRecordStop() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        showToast("视频录制停止");
                                        mIsRecord = false;
                                        mRecordTimeTextView.setVisibility(View.INVISIBLE);
                                        mLiveRecordButton.setChecked(false);
                                        mRecordRecordButton.setChecked(false);
                                        mPublicLiveRecordButton.setChecked(false);
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onSnapshotFail(LYException e) {
                showToast("视频录制失败");
                runOnUiThread(new Runnable() {
                    public void run() {
                        mIsRecord = false;
                        mRecordTimeTextView.setVisibility(View.INVISIBLE);
                        mLiveRecordButton.setChecked(false);
                        mRecordRecordButton.setChecked(false);
                        mPublicLiveRecordButton.setChecked(false);
                    }
                });
            }
        });

    }

    private void startRecordAnimation() {
        mAnimation = new AlphaAnimation(1, 0.3f);
        mAnimation.setDuration(1500);
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mRecordTimeTextView.startAnimation(mAnimation);
    }

    /**
     * 展示视频播放相关信息
     */
    private void showParamPopUp() {
        if (playParamPopup == null) {
            playParamPopup = new PlayParamPopup(this, mLYPlayer,
                    mIsPublic, "iermu", mCamera.address, mOnlineNums + 1, mViewTimes + 1,
                    (int) mTotalPreparedTime, mRtmpAddr);
        }
        playParamPopup.setAnimationStyle(R.style.popup_param_anim_style);
        if (!playParamPopup.mIsLandScape) {
            playParamPopup.setWidth(Utils.getScreenWidth(mCameraApplication)
                    - 2 * mPlayParamImageView.getLeft());
            playParamPopup.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.bg_param_popdown));
            playParamPopup.showAsDropDown(mPlayParamImageView);
        } else {
            playParamPopup.setBackgroundDrawable(new ColorDrawable(
                    getResources().getColor(R.color.half_transparent)));
            playParamPopup.showAtLocation(mPlayerContainerFrameLayout,
                    Gravity.CENTER, 0, 0);
        }
    }

    private void initRecordTime() {
        Calendar calendar = Calendar.getInstance();
        final Date date = new Date(calendar.get(Calendar.YEAR) - 1900, calendar
                .get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        calendar.setTime(date);
        mRecordCurrentDate = date;
        mCurrentSelFrom = date.getTime() / 1000;
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        mCurrentSelTo = (new Date()).getTime() / 1000;
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        mRecordPreDate = calendar.getTime();
        mRecordCurrentDayTextView.setText(R.string.date_today);
    }

    void play() {
//        initRecordTime();
        CLog.v("mPlayType: " + mPlayType);
        if (mPlayType == IPlayer.TYPE_RECORD) {
            if (mTabCurrentTime == 0) {
                initRecordTime();
            }
            playRecord();
        } else {
            initRecordTime();
            playLive();
        }
    }

    /**
     * 播放录像
     */
    private void playRecord() {
        changeToRecordPlayer();
        mHavingPlayRecord = true;
        mLYPlayer.setOnClosedListener(new OnClosedListener() {
            @Override
            public void onClosed() {
                CLog.v("onClosed");
            }
        });
        mLYPlayer.setOnPreparedListener(mOnPreparedListener);
        mLYPlayer.setOnClosedListener(new OnClosedListener() {
            @Override
            public void onClosed() {
                CLog.v("onClosed");
            }
        });
        mLYPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(final int what, final String extra) {
                CLog.v("===playRecord onError");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        savePlayerLog("playRecord onError");
                        mIsSeeking = false;
                        mReplayImg.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        mLivingStatusTextView.setText("网络连接失败");
                        showToast(what + ":" + extra);
                    }
                });
                return false;
            }
        });
        mLYPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mCurrentTime = System.currentTimeMillis();
        mIsSeeking = true;
        CLog.d("===start play record");
        initRecordSegmentListCallBackListener();
        savePlayerLog(String.format("%s %s %s %s ", "getRecordSegmentList", mCid, mCurrentSelFrom,
                mCurrentSelTo));
        LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
        GetRecordSegmentListMgmt.getRecordSegmentList(
                mCurrentSelFrom,
                mCurrentSelTo,
                localUser.getUid(),
                mCid,
                localUser.getExpire() + "",
                localUser.getAccessToken(),
                mIRecordSegmentListCallBackListener
        );
    }

    private void initProgressbarPos() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long playTime;
                if (mTabCurrentTime == 0) {
                    long nowTimeStamp = System.currentTimeMillis() / 1000;
                    CLog.d("nowTimeStamp ---  " + nowTimeStamp);
                    CLog.d("mCurrentSelFrom ---  " + mCurrentSelFrom);
                    long dayBetweenToday = (nowTimeStamp - mCurrentSelFrom) / TOTAL_SPAN;
                    CLog.d("dayBetweenToday01---" + dayBetweenToday);
                    //如果是当天
                    if (dayBetweenToday == 0) {
                        playTime = (mCurrentSelTo - 3600)
                                > mCurrentSelFrom ? (mCurrentSelTo - 3600) : mCurrentSelFrom;
                    } else {
                        playTime = nowTimeStamp - dayBetweenToday * TOTAL_SPAN - 3600;
                    }
                    CLog.d("initProgressbarPos---01");
                } else {
                    playTime = mTabCurrentTime;
                    CLog.d("initProgressbarPos---02");
                }
                int scrollX = (int) ((mSectionImageView.getWidth() *
                        (playTime - mCurrentSelFrom)) / TOTAL_SPAN);
                mRecordSeekTextView.setText(
                        DateTimeUtil.timeStampToDateByFormat(playTime, "HH:mm:ss"));
                CLog.v("playertime2--" + playTime + "---currenttime---" + mTabCurrentTime);
                mTimeLineMyHorizontalScrollView.scrollTo(scrollX, 0);
            }
        }, 50);
    }

    /**
     * 摄像机直播
     */
    void playLive() {
        savePlayerLog(String.format("mLivePlayer.play %s\n%s", mCid, mRtmpAddr));
        CLog.d("---" + mCamera.play_addr);
        changeToLivePlayer();
        mIsPublic = mCamera.type == TYPE_PUBLIC;
        //mCamera.play_addr
//        mLYPlayer.setDataSource("rtmp://rtmp2.public.topvdn.cn/live/3000000092_3356491776_1471082162_ca58def790b322ebca96af37c5290734");
        mLYPlayer.setDataSource(mCamera.play_addr);

        mLYPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(int time) {
                CLog.v("===playLive onPrepared: " + time);
                savePlayerLog("onLiving");
                mHandler.removeCallbacks(mShowProgressBarRunnable);
                mHasPlayed = true;
                if (mCamera != null && mCamera.type == Camera.TYPE_PRIVATE) {
                    snapShot(false, true, false);
                }
                prepareLiveView(time);
                mHandler.postDelayed(mHideTopAndBottomRunnable, 5000);
                if (mCamera != null
                        && mCamera.type == Camera.TYPE_PUBLIC) {
                    mPeopleMgmt.doExe(getApplicationContext(), true, mCamera.cid, mCallBack);
                }
            }
        });
        mLYPlayer.setOnClosedListener(new OnClosedListener() {
            @Override
            public void onClosed() {
                CLog.v("onClosed");
            }
        });
        mLYPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(int errorCode, String msg) {
                savePlayerLog(String.format("onError %s %s", errorCode, msg));
                CLog.v("===playLive onError  " + errorCode
                        + "  errorCode msg: " + msg);
                if (mLiveRecordButton.isChecked()) {
                    showToast("录像已保存至我的文件");
                }

                if (!mHavingPlayRecord) {
                    mReplayImg.setVisibility(View.VISIBLE);
                }
                mHandler.removeCallbacks(mShowProgressBarRunnable);
                mReplayImg.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mLivingStatusTextView.setVisibility(View.VISIBLE);
                stopLocalRecord();
                mLiveRecordButton.setEnabled(false);
                mLiveRecordButton.setChecked(false);
                mRecordRecordButton.setChecked(false);
                mPublicLiveRecordButton.setChecked(false);
                if (msg != null && !msg.equals("")) {
                    mLivingStatusTextView.setText(msg);
                } else {
                    mLivingStatusTextView.setText(R.string.livecamera_state_error);
                }
                return false;
            }
        });
        mLYPlayer.start();
        CLog.d("===start play live");
    }

    private void reInitView() {
        loadLiveVideoCover();
        if (mHideCoverAlphaAnimation != null) {
            mHideCoverAlphaAnimation.cancel();
        }
        mLivingStatusTextView.setText(R.string.livecamera_state_connecting);
        mSnapshotButton.setEnabled(false);
        mRecordTimeTextView.setText("");
        mRecordTimeTextView.setVisibility(View.INVISIBLE);
        mResolutionTextView.setOnClickListener(null);
        mPublicLiveVolumeButton.setEnabled(false);
        mLiveRecordButton.setEnabled(false);
        mVoiceButton.setEnabled(false);
        mPublicLiveVolumeButton.setChecked(false);
        mRecordVolumeButton.setChecked(false);
        mMineLiveVolumeButton.setChecked(false);
        mMineLiveVolumeButton.setEnabled(false);
        mRecordVolumeButton.setEnabled(false);
        mPublicLiveVolumeButton.setEnabled(false);
        mRecordSnapshotButton.setEnabled(false);
        mRecordRecordButton.setEnabled(false);
    }

    private void prepareLiveView(long time) {
        CLog.v("onLiving button_time_record" + time);
        mTotalPreparedTime = time;
        hideLoadCover();
        mReplayImg.setVisibility(View.GONE);
        mResolutionTextView.setOnClickListener(mOnClickListener);
        mPublicLiveVolumeButton.setEnabled(true);
        mLiveRecordButton.setEnabled(true);
        mSnapshotButton.setEnabled(true);
        mVoiceButton.setEnabled(true);
        mMineLiveVolumeButton.setEnabled(true);
        mSetFullScreenButton.setVisibility(View.VISIBLE);
        mLivingStatusTextView.setText("");
        CLog.v("onLiving END");
    }

    private void prepareRecordView(int time) {
        CLog.v("prepareRecordView:" + time);
        mTimeLineMyHorizontalScrollView.setHorizontalScrollBarEnabled(true);
        mRecordDateToggleButton.setEnabled(true);
        mCoverImageView.setVisibility(View.GONE);
        mRecordSnapshotButton.setEnabled(true);
        mRecordRecordButton.setEnabled(true);
        mRecordVolumeButton.setEnabled(true);
        mReplayImg.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mLivingStatusTextView.setText("");
        hideLoadCover();
    }

    private void hideLoadCover() {
        mHideCoverAlphaAnimation = new AlphaAnimation(1f, 0f);
        mHideCoverAlphaAnimation.setDuration(500);
        mHideCoverAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                CLog.d("onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                CLog.d("onAnimationEnd");
                mProgressBar.setVisibility(View.INVISIBLE);
                mCoverImageView.setVisibility(View.GONE);
                mLivingStatusTextView.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                CLog.d("onAnimationRepeat");
            }
        });
        if (mCoverImageView.getVisibility() == View.VISIBLE) {
            mCoverImageView.startAnimation(mHideCoverAlphaAnimation);
        }
        mProgressBar.startAnimation(mHideCoverAlphaAnimation);
        mLivingStatusTextView.startAnimation(mHideCoverAlphaAnimation);
    }

    private void snapShot(final boolean showToast, final boolean isPrivateCover, final boolean isSnapShotByClick) {
        motherSnapshotButton.setClickable(false);
        final long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        CLog.v("isSnapShotByClick " + isSnapShotByClick);
        String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                .format(date) + "_" + System.currentTimeMillis() % 1000;
        if (isPrivateCover && !isSnapShotByClick) {
            name = name + mCid;
            deleteOldCover(mCid);
        }
        final String snapPath = FileUtil.getInstance().getSnapshotFile()
                .getAbsolutePath();
        final String finalName = name;
        if (isPrivateCover || isSnapShotByClick) {
            mLYPlayer.snapshot(snapPath, name, new OnSnapshotListener() {
                @Override
                public void onSnapshotSuccess(final String saveFullPath) {
                    motherSnapshotButton.setClickable(true);
                    if (showToast) {
                        showToast("截图已保存至我的文件");
                    }
                    ThreadPoolManagerQuick.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isPrivateCover) {
                                CLog.v("save private cover success" + saveFullPath);
                                MyPreference myPreference = MyPreference.getInstance();
                                myPreference.setPreferenceKey(mCamera.cid);
                                myPreference.setValue(PlayerActivity.this, MyPreference.SNAPSHOT_OF_COVER, saveFullPath);
                            }
                            if (isSnapShotByClick) {
                                boolean flg = LocalRecordWrapper.getInstance().addLocalMedia(
                                        new LocalRecord(finalName, timestamp, 0, LocalRecord.TYPE_MEDIA_PHOTO,
                                                saveFullPath, saveFullPath, timestamp, mCid,
                                                LocalUserWrapper.getInstance().getLocalUser().getUid()));
                                CLog.v("addLocalMedia -flg:" + flg);
                            }
                        }
                    });
                }

                @Override
                public void onSnapshotFail(LYException errorCode) {
                    motherSnapshotButton.setClickable(true);
                    if (showToast) {
                        showToast("截图失败!" + errorCode);
                    }
                    if (isPrivateCover) {
                        CLog.v("save private cover fail");
                    }
                }
            });
        }

    }

    private void deleteOldCover(final String cid) {
        ThreadPoolManagerQuick.execute(new Runnable() {
            @Override
            public void run() {
                File dir = FileUtil.getInstance().getSnapshotFile();
                if (dir == null || !dir.exists()) {
                    return;
                }
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (file != null && file.isFile()) {
                        if (file.getName().contains(cid)) {
                            file.delete();
                        }
                    }
                }
            }
        });
    }

    private void loadLiveVideoCover() {
        CLog.v("loadLiveVideoCover mImageCover:" + mImageCover);
        Utils.displayCaptureView(mCoverImageView, mImageCover);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPlayerHorizontalScrollView.smoothScrollTo((mLYPlayer.getWidth()
                        - getResources().getDisplayMetrics().widthPixels) / 2, 0);
                mVisonSeekBar.setProgress(50);
            }
        }, 50);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        CLog.v("newConfig:" + newConfig + " orientation："
                + this.getResources().getConfiguration().orientation);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mVisionSeekRelativeLayout.setVisibility(View.INVISIBLE);
            mBackFromFullScreenImageView.setVisibility(View.VISIBLE);
            FrameLayout.LayoutParams horLayoutParams = (FrameLayout.LayoutParams) mPlayerContainerFrameLayout
                    .getLayoutParams();
            horLayoutParams.width = getResources().getDisplayMetrics().widthPixels;
            mPlayerContainerFrameLayout.setLayoutParams(horLayoutParams);

            FrameLayout.LayoutParams videoLayoutParams = (FrameLayout.LayoutParams) mLYPlayer
                    .getLayoutParams();
            videoLayoutParams.width = getResources().getDisplayMetrics().widthPixels;
            mLYPlayer.setLayoutParams(videoLayoutParams);
            LayoutParams layoutParams = (LayoutParams) mVideoViewFrameLayout
                    .getLayoutParams();
            layoutParams.width = getResources().getDisplayMetrics().widthPixels;
            mHeaderView.setVisibility(View.GONE);
            layoutParams.height = getResources().getDisplayMetrics().heightPixels;
            mVideoViewFrameLayout.setLayoutParams(layoutParams);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            initVideoView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.post(mUpdateRealTimeRunnable);
    }

    /*
     * 截图
     *
     * @param showToast      是否提示
     * @param isPrivateCover 是否保存私有摄像机封面
     * */

    private void initVideoView() {
        mVisionSeekRelativeLayout.setVisibility(View.VISIBLE);
        mBackFromFullScreenImageView.setVisibility(View.INVISIBLE);
        LayoutParams layoutParams = (LayoutParams) mVideoViewFrameLayout
                .getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        layoutParams.height = getResources().getDisplayMetrics().widthPixels * 3 / 4;
        mHeaderView.setVisibility(View.VISIBLE);
        mVideoViewFrameLayout.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams horLayoutParams = (FrameLayout.LayoutParams) mPlayerContainerFrameLayout
                .getLayoutParams();
        horLayoutParams.width = getResources().getDisplayMetrics().widthPixels * 4 / 3;
        mPlayerContainerFrameLayout.setLayoutParams(horLayoutParams);
        FrameLayout.LayoutParams videoLayoutParams = (FrameLayout.LayoutParams) mLYPlayer
                .getLayoutParams();
        videoLayoutParams.width = getResources().getDisplayMetrics().widthPixels * 4 / 3;
        mLYPlayer.setLayoutParams(videoLayoutParams);
        mPlayerContainerFrameLayout.setLayoutParams(videoLayoutParams);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        CLog.v("onTouchEvent" + event.getAction());
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            mTouchUpStartTime = System.currentTimeMillis();
//        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            long touchDownTime = System.currentTimeMillis();
//            if (touchDownTime - mTouchUpStartTime > 3000) {
//                CLog.v("hide");
//                mHandler.post(mHideTopAndBottomRunnable);
//            } else {
//                CLog.v("remove hide ");
//                mHandler.removeCallbacks(mHideTopAndBottomRunnable);
//            }
//        }
        return super.onTouchEvent(event);
    }

    public void onClick_Back(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
//        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
//            return;
//        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CLog.v("onCreate");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_player);
        mPeopleMgmt = (IncreaseOrDecreasePlayerPeopleMgmt) MgmtClassFactory.getInstance().
                getMgmtClass(IncreaseOrDecreasePlayerPeopleMgmt.class);
        mAttentionPublicCameraMgmt = (AttentionPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(AttentionPublicCameraMgmt.class);
        mUnAttentionPublicCameraMgmt = (UnAttentionPublicCameraMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(UnAttentionPublicCameraMgmt.class);
        mGetCameraSetMgmt = (GetCameraSetMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(GetCameraSetMgmt.class);
        mUpdateCameraSetMgmt = (UpdateCameraSetMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(UpdateCameraSetMgmt.class);
        initView();
        initLandscapeView();
        savePlayerLog("--------------player--log--start-------------------");
    }

    private void initView() {
        CLog.v("initView");
        mCamera = (Camera) getIntent().getSerializableExtra(Const.IntentKeyConst.KEY_CAMERA);
        mCid = mCamera.cid;
        mCname = mCamera.cname;
        mOnlineNums = mCamera.online_nums;
        mViewTimes = mCamera.total_watched_nums;
        mHls = mCamera.hls;
        mPlayType = getIntent().getIntExtra(Const.IntentKeyConst.KEY_PLAYTYPE, IPlayer.TYPE_QSTP);
        mUserNickName = mCamera.nickname;
        mIsShareLive = getIntent().getBooleanExtra(Const.IntentKeyConst.KEY_LIVE_SHARE, false);
        mImageCover = getIntent().getStringExtra(Const.IntentKeyConst.KEY_CAMERA_COVER);
        mRtmpAddr = getIntent().getStringExtra(Const.IntentKeyConst.KEY_LIVE_RTMP);
        mAttentionButton = (CheckBox) findViewById(R.id.tb_attention);
        mRecordTodayTextView = (TextView) findViewById(R.id.tv_record_today);
        mLiveTodayTextView = (TextView) findViewById(R.id.tv_live_today);
        mResolutionTextView = (TextView) findViewById(R.id.tv_resolution);
        mResolutionRl = (RelativeLayout) findViewById(R.id.rl_resolution);
        mWatchedNumTextView = (TextView) findViewById(R.id.tv_live_watched_num);
        mCopyButton = (Button) findViewById(R.id.btn_copy_play_address);
        mWatchedNumTextView.setText(String.format("%s%s%s", "看过", mCamera.total_watched_nums + 1, "次"));
        line = (ImageView) findViewById(R.id.iv_line);

        if (mCamera.is_followed) {
            mAttentionButton.setChecked(true);
            mAttentionButton.setTextColor(getResources().getColor(R.color.white));
            mAttentionButton.setText(String.format("%s人已关注", mCamera.followed));
            mAttentionButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            line.setVisibility(View.VISIBLE);
        } else {
            mAttentionButton.setChecked(false);
            mAttentionButton.setTextColor(getResources().getColor(R.color.white));
            mAttentionButton.setText(String.format("%s人正在关注", mCamera.followed));
            mAttentionButton.setBackgroundColor(getResources().getColor(R.color.orange));
            line.setVisibility(View.GONE);
        }

        mReplayImg = (ImageView) findViewById(R.id.img_replay);
        mBackToLiveButton = (Button) findViewById(R.id.btn_backlive);
        mTitleTextView = (TextView) findViewById(R.id.tv_header_title);
        TextView titleTitleView = (TextView) findViewById(R.id.tv_live_title);
        TextView titleTitleView2 = (TextView) findViewById(R.id.tv_live_title2);
        titleTitleView.setText(mCname);
        titleTitleView2.setText(mCname);
        mLiveRelativeLayout = (RelativeLayout) findViewById(R.id.rl_layout_live);
        mOtherLiveRelativeLayout = (RelativeLayout) findViewById(R.id.rl_layout_other_live);
        mVisionSeekRelativeLayout = (RelativeLayout) findViewById(R.id.rl_vision_seek);
        mRecordLinearLayout = (RelativeLayout) findViewById(R.id.layout_record);
        mRecordTimeTextView = (TextView) findViewById(R.id.tv_live_recordtime);
        mPlayParamImageView = (ImageView) findViewById(R.id.iv_play_param);
        playParamRl = (RelativeLayout) findViewById(R.id.rl_play_param);
        mLiveParamsLayout = (RelativeLayout) findViewById(R.id.layout_live_params);
        mLiveMineParamsLayout = (RelativeLayout) findViewById(R.id.layout_live_mine_params);
        mRecordDateToggleButton = (ToggleButton) findViewById(R.id.tb_record_seldate);
        mRecordTimeLineLeftSpan = (TextView) findViewById(R.id.tv_record_timeline_leftspan);
        mRecordTimeLineRightSpan = (TextView) findViewById(R.id.tv_record_timeline_rightspan);
        mVisonSeekBar = (SeekBar) findViewById(R.id.seekbarview);
        mLivingStatusTextView = (TextView) findViewById(R.id.tv_camera_status);
        mRecordSeekTextView = (TextView) findViewById(R.id.tv_record_currentseek);
        mSectionImageView = (ImageView) findViewById(R.id.iv_section);
        mLiveRecordButton = (ToggleButton) findViewById(R.id.tb_live_record);
        mPublicLiveRecordButton = (ToggleButton) findViewById(R.id.tb_other_live_record);
        mRecordRecordButton = (ToggleButton) findViewById(R.id.tb_record_record);
        mVoiceButton = (Button) findViewById(R.id.tb_live_media_record);
        mRecordVolumeButton = (ToggleButton) findViewById(R.id.tb_record_vol);
        mPlayerChangeButton = (ToggleButton) findViewById(R.id.tb_change_player);
        mPlayerContainerFrameLayout = (FrameLayout) findViewById(R.id.fl_player_container);
        mSetFullScreenButton = (ToggleButton) findViewById(R.id.btn_set_fullscreen);
        mSnapshotButton = (Button) findViewById(R.id.btn_live_snapshot);
        mRecordSnapshotButton = (Button) findViewById(R.id.btn_record_snapshot);
        motherSnapshotButton = (Button) findViewById(R.id.btn_other_live_snapshot);
        mLYPlayer = (LYPlayer) findViewById(R.id.vpv_live);
        mRecordCurrentDayTextView = (TextView) findViewById(R.id.tv_record_currentday);

        mPublicLiveVolumeButton = (ToggleButton) findViewById(R.id.tb_live_public_volume);
        mMineLiveVolumeButton = (ToggleButton) findViewById(R.id.tb_live_mine_volume);

        mCameraStatusLinearLayout = (LinearLayout) findViewById(R.id.ll_camera_status);
        mProgressBar = (CircularFillableLoaders) findViewById(R.id.pb_bar);
        mCoverImageView = (ImageView) findViewById(R.id.iv_video_cover);
        mTimeLineMyHorizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.hv_timeline);
        mPlayerHorizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.hv_player_container);
        mVideoViewFrameLayout = (FrameLayout) findViewById(R.id.videoplayer_layout);
        mBackImageView = (Button) findViewById(R.id.iv_heder_back);
        mBackFromFullScreenImageView = (ImageView) findViewById(R.id.iv_player_backfrom_fullscreen);
        mRecordExitImageView = (ImageView) findViewById(R.id.iv_record_exit);
        mHeaderView = findViewById(R.id.headerview);
        mLine2 = (ImageView) findViewById(R.id.line2);


        // 判断播放类型，设置布局是否可见
        if (mPlayType == IPlayer.TYPE_RECORD) {
            mLiveMineParamsLayout.setVisibility(View.GONE);
            mLiveParamsLayout.setVisibility(View.GONE);
            mRecordLinearLayout.setVisibility(View.VISIBLE);
            mOtherLiveRelativeLayout.setVisibility(View.GONE);
            mLiveRelativeLayout.setVisibility(View.GONE);
            mWatchedNumTextView.setVisibility(View.GONE);
        } else {
            if (!isMineCamera()) {
                mLiveParamsLayout.setVisibility(View.VISIBLE);
                mLiveMineParamsLayout.setVisibility(View.GONE);
                mRecordLinearLayout.setVisibility(View.GONE);
                mOtherLiveRelativeLayout.setVisibility(View.GONE);
                mLiveRelativeLayout.setVisibility(View.GONE);
                mWatchedNumTextView.setVisibility(View.VISIBLE);
            } else {
                mLiveParamsLayout.setVisibility(View.GONE);
                mLiveMineParamsLayout.setVisibility(View.VISIBLE);
                mRecordLinearLayout.setVisibility(View.GONE);
                mOtherLiveRelativeLayout.setVisibility(View.GONE);
                mLiveRelativeLayout.setVisibility(View.GONE);
                mWatchedNumTextView.setVisibility(View.GONE);
            }
        }
        mResolutionTextView.setEnabled(false);
        if (mCamera.uname.equals(LocalUserWrapper.getInstance().getLocalUser().getUid())) {
            getSet();
        } else {
//            mResolutionTextView.setText(R.string.set_definition_fluent);
            mResolutionRl.setVisibility(View.GONE);
            mLine2.setVisibility(View.GONE);
        }
        mLYPlayer.setOnClickListener(mOnClickListener);
        mCopyButton.setOnClickListener(mOnClickListener);
        mRecordCurrentDayTextView.setSelected(true);
        mRecordCurrentDayTextView.setOnClickListener(mOnClickListener);
        mReplayImg.setOnClickListener(mOnClickListener);
        mRecordTimeTextView.setVisibility(View.INVISIBLE);
        mVideoViewFrameLayout.setKeepScreenOn(true);
        mBackFromFullScreenImageView.setVisibility(View.GONE);
        mPlayParamImageView.setOnClickListener(mOnClickListener);
        playParamRl.setOnClickListener(mOnClickListener);
        mBackFromFullScreenImageView.setOnClickListener(mOnClickListener);
        mRecordExitImageView.setOnClickListener(mOnClickListener);
        mBackImageView.setVisibility(View.VISIBLE);
        mTitleTextView.setText(mCname);
        mRecordCurrentDayTextView.setOnClickListener(mOnClickListener);
        mRecordSnapshotButton.setOnClickListener(mOnClickListener);
        mBackToLiveButton.setOnClickListener(mOnClickListener);
        motherSnapshotButton.setOnClickListener(mOnClickListener);
        mSnapshotButton.setOnClickListener(mOnClickListener);
        mAttentionButton.setOnClickListener(mOnClickListener);
        mPublicLiveVolumeButton.setOnCheckedChangeListener(mOnCheckChangeListener);
        mMineLiveVolumeButton.setOnCheckedChangeListener(mOnCheckChangeListener);
        mLiveRecordButton.setOnCheckedChangeListener(mOnCheckChangeListener);
        mPublicLiveRecordButton.setOnCheckedChangeListener(mOnCheckChangeListener);
        mRecordRecordButton.setOnCheckedChangeListener(mOnCheckChangeListener);
        mVoiceButton.setOnTouchListener(mOnTalkListener);
        mPlayerChangeButton.setOnClickListener(mOnClickListener);
        mRecordVolumeButton.setOnCheckedChangeListener(mOnCheckChangeListener);
        mSetFullScreenButton.setOnCheckedChangeListener(mOnCheckChangeListener);
        mRecordDateToggleButton.setOnCheckedChangeListener(mOnCheckChangeListener);
        mRecordTimeLineLeftSpan.setWidth(getResources().getDisplayMetrics().widthPixels / 2);
        mRecordTimeLineRightSpan.setWidth(getResources().getDisplayMetrics().widthPixels / 2);
        initVideoView();
        mPlayerHorizontalScrollView.setSmoothScrollingEnabled(true);
        mTimeLineMyHorizontalScrollView.setSmoothScrollingEnabled(true);
        mPlayerHorizontalScrollView.setOnScrollByUserListener(new MyHorizontalScrollView.OnScrollByUserListener() {
            @Override
            public void onScroll(int scrollX, boolean isOnTouch) {
                mVisonSeekBar.setProgress(100 * scrollX / (getResources().getDisplayMetrics().widthPixels / 3));
            }
        });
        mTimeLineMyHorizontalScrollView.setOnScrollByUserListener(new MyHorizontalScrollView.OnScrollByUserListener() {
            @Override
            public void onScroll(int scrollX, boolean isOnTouch) {
                mHandler.removeCallbacks(mHideTopAndBottomRunnable);
                mIsSeeking = true;
                int recordSeekPercent = ADJUST_NUMBER * scrollX / mSectionImageView.getWidth();
                mSeekTimeSpan = (long) (TOTAL_SPAN * recordSeekPercent / ADJUST_NUMBER);
                mRecordSeekTextView.setText(DateTimeUtil.timeStampToDateByFormat(mSeekTimeSpan + mCurrentSelFrom, "HH:mm:ss"));
                temp = mSeekTimeSpan + mCurrentSelFrom;
                CLog.v("recordSeekPercent :" + recordSeekPercent +
                        " -mRecordSeekTextView:" + mRecordSeekTextView.getText() + " -isOnTouch:" + isOnTouch);
                if (!isOnTouch) {
                    mHandler.removeCallbacks(mSeekRunnable);
                    mHandler.postDelayed(mSeekRunnable, 200);
                }
            }
        });
    }

    private void initLandscapeView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mVisionSeekRelativeLayout.setVisibility(View.INVISIBLE);
        mBackFromFullScreenImageView.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams horLayoutParams = (FrameLayout.LayoutParams) mPlayerContainerFrameLayout
                .getLayoutParams();
        horLayoutParams.width = getResources().getDisplayMetrics().widthPixels;
        mPlayerContainerFrameLayout.setLayoutParams(horLayoutParams);

        FrameLayout.LayoutParams videoLayoutParams = (FrameLayout.LayoutParams) mLYPlayer
                .getLayoutParams();
        videoLayoutParams.width = getResources().getDisplayMetrics().widthPixels;
        mLYPlayer.setLayoutParams(videoLayoutParams);
        LayoutParams layoutParams = (LayoutParams) mVideoViewFrameLayout
                .getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        mHeaderView.setVisibility(View.GONE);
        layoutParams.height = getResources().getDisplayMetrics().heightPixels;
        mVideoViewFrameLayout.setLayoutParams(layoutParams);
    }

    private void savePlayerLog(final String str) {
        if (mIsDebug) {
            ThreadPoolManagerNormal.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogUtil.getInstance().savePlayerLog2File(
                                String.format("%s %s%s",
                                        DateTimeUtil.formatTimeToMS(System.currentTimeMillis()),
                                        str,
                                        "\n"));
                    } catch (Exception e) {
                        CLog.e(e.getMessage());
                    }
                }
            });
        }
    }

    private boolean isMineCamera() {
        return mCamera.uname.equals(LocalUserWrapper.getInstance().getLocalUser().getUid())
                || mIsShareLive;
    }

    public void getSet() {
        try {
            mGetCameraSetMgmt.getCameraSet(getApplicationContext(), mCamera.cid, mGetCallBack);
        } catch (Exception e) {
            CLog.e(e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        savePlayerLog("--------------player--log--end-------------------\n\n");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mLYPlayer.pauseToBackground();
        closePlay();
        super.onPause();
    }

    @Override
    protected void onResume() {
        CLog.v("onResume");
        mLYPlayer.resumeFromBackground();
        mTabCurrentTime = temp;
//        reInitView();
        play();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mHasPlayed) {
//            mPeopleMgmt.doExe(PlayerActivity.this, false, mCid, mCallBack);
            mHasPlayed = false;
        }
        mHandler.removeCallbacks(mUpdateRealTimeRunnable);
//        if (mLYPlayer.isPlaying()) {
//            CLog.d("===stop");
//            mLYPlayer.stop();
//        }
    }

    @Override
    protected void finishSharedPlayer(String cid) {
        if (mCamera.cid.equals(cid)) {
            showToast(String.format("%s已取消分享 %s 摄像机!\n即将自动退出!",
                    mCamera.nickname, mCamera.cname));
            if (mCamera.getCameraOwner()== Camera.CameraOwner.CAMERA_SHARA_TO_ME) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 2000);
            }
        }
    }

    @Override
    protected void processNetworkChange(boolean isConnect) {
        super.processNetworkChange(isConnect);
        if (!isConnect) {
            if (mIsRecord) {
                stopLocalRecord();
                mLiveRecordButton.setChecked(false);
                mRecordRecordButton.setChecked(false);
                mPublicLiveRecordButton.setChecked(false);
            }
        }
    }

    @Override
    protected void processMessage(MobileInterconnectResponse.Mobile mobileMessage) {
        super.processMessage(mobileMessage);
        if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECTION_CLOSED)) {
            //私有连接中断
            CLog.d("play private stop---------------------" + mobileMessage.message);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mProgressBar.setVisibility(View.GONE);
//                    mReplayImg.setVisibility(View.VISIBLE);
//                    mLivingStatusTextView.setText("私有连接中断");
//                }
//            });
        } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CLOSE_PLAY_AND_LIVE)) {
            finish();
        }
    }

    private void stopLocalRecord() {
        mHandler.removeCallbacks(mUpdateTimeRunnable);
        mRecordTimeTextView.setVisibility(View.GONE);
        stopRecordAnimation();
        if (mIsRecord) {
            mLYPlayer.stopLocalRecord();
            if (mLocalMediaRecord != null) {
                mLocalMediaRecord.setDuration((int) ((System.currentTimeMillis() - mBeginTime) / 1000));
                boolean flg = LocalRecordWrapper.getInstance().addLocalMedia(mLocalMediaRecord);
                if (flg) {
                    showToast(getString(R.string.record_saved_to_my_file));
                }
            }
        }
        mRecordDuration = 0;
        mIsRecord = false;
        mLocalMediaRecord = null;
    }

    private void stopRecordAnimation() {
        CLog.v("recordtime--stop");
        if (mAnimation != null) {
            mRecordTimeTextView.clearAnimation();
            mAnimation.reset();
        }

    }

    void closePlay() {
        mVideoViewFrameLayout.setKeepScreenOn(false);
        if (mPlayType == IPlayer.TYPE_RECORD) {
            savePlayerLog("mLYPlayer.stop();");
            closeRecordPlay();
        } else {
            savePlayerLog("mLYPlayer.stop();");
            closeLive();
        }

    }

    void closeRecordPlay() {
        if (mLYPlayer != null) {
            mRecordRecordButton.setChecked(false);
//            if (mHavingPlayRecord) {
//                if (mLYPlayer.isPlaying()) {
                    CLog.d("===stop");
                    mLYPlayer.stop();
//                }
                mHavingPlayRecord = false;
//            }
            mIRecordSegmentListCallBackListener = null;
        }
    }

    void closeLive() {
        if (mLYPlayer != null) {
            mLiveRecordButton.setChecked(false);
            mPublicLiveRecordButton.setChecked(false);
//            if (mLYPlayer.isPlaying()) {
                CLog.d("===stop");
                mLYPlayer.stop();
//            }
        }
    }
}
