package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.MobileInterconnectResponse.Mobile;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.MessageSendMgmt;
import com.lingyang.camera.util.DateTimeUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.av.SessionConfig;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.facetime.LYFaceTime;
import com.lingyang.sdk.player.IScreenRatio;
import com.lingyang.sdk.player.widget.LYPlayer;
import com.lingyang.sdk.view.LYGLCameraEncoderView;
import com.lingyang.sdk.view.SurfaceFrameShape;

/**
 * 文件名: MobileInterconnectActivity
 * 描    述: 手机互联,该类负责呼叫和被叫的音视频播放，及相关逻辑的处理
 * 创建人: 杜舒
 * 创建时间: 2015/11
 */
public class MobileInterconnectActivity extends AppBaseActivity {

    /**
     * 如果是正在通话中，有其他人打进电话时的回调
     */
    BaseCallBack<String> mBusyCallback = new BaseCallBack<String>() {
        @Override
        public void error(ResponseError object) {
            CLog.v("busyCallback error");
        }

        @Override
        public void success(String s) {
            CLog.v("busyCallback success");
        }
    };
    /**
     * 连接失败回调
     */
    BaseCallBack<String> mConnectFailCallback = new BaseCallBack<String>() {
        @Override
        public void error(ResponseError object) {
            CLog.v("mConnectFailCallback error");
        }

        @Override
        public void success(String s) {
            CLog.v("mConnectFailCallback success");
        }
    };
    /**
     * 挂断回调
     */
    BaseCallBack<String> mHangUpCallback = new BaseCallBack<String>() {
        @Override
        public void error(ResponseError object) {
            CLog.v("mHangUpCallback error");
        }

        @Override
        public void success(String s) {
            CLog.v("mHangUpCallback success " + System.currentTimeMillis());
        }
    };
    private LYPlayer mLYPlayer;
    private TextView mCallingHeader, mCallingTime, mCallingName, mCallingWait;
    private ToggleButton mTbVoice;
    CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.tb_voice:
                    if (mTbVoice.isChecked()) {
                        showToast(getString(R.string.have_open_microphone));
                        mLYFaceTime.startAudioRecording();
                    } else {
                        showToast(getString(R.string.have_close_microphone));
                        mLYFaceTime.stopAudioRecording();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private LinearLayout mLlActions;
    Runnable mHideSomeButtonsRunnable = new Runnable() {
        @Override
        public void run() {
            mLlActions.setVisibility(View.INVISIBLE);
        }
    };
    private long mMobileLiveTime = 0;
    Runnable mUpdateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mCallingTime.setText(String.format("%s",
                    DateTimeUtil.timeStampToDateByFormat(-3600 * 8 + mMobileLiveTime++,
                            "HH:mm:ss")));
            mHandler.postDelayed(mUpdateTimeRunnable, 1000);
        }
    };
    private String mFrom, mPhoneNumber;
    private MessageSendMgmt mMessageSendMgmt;
    private boolean mConnectSuccess;
    private int mRotate;
    private long mPreparedTime;
    private boolean mIsHardCodec = false;
    private RelativeLayout mRlParams;
    private long mOldTotalTxBytes;
    private long mOldTotalRxBytes;
    private PopupMobileInterconnectParam mPopupMobileInterconnectParam;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_mobile_interconnect:
                    if (mConnectSuccess) {
                        if (mLlActions.getVisibility() == View.VISIBLE) {
                            mLlActions.setVisibility(View.INVISIBLE);
                        } else {
                            mLlActions.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case R.id.ib_switch_fore_back_camera:
                    mLYFaceTime.switchCamera();
                    break;
                case R.id.ib_hang_up:
                    mHandler.post(mHangUpRunnable);
                    break;
                case R.id.rl_params:
                    showParamPopUp();
                    break;
                default:
                    break;
            }
        }
    };
    private LYFaceTime mLYFaceTime;
    private Runnable mHangUpRunnable = new Runnable() {
        @Override
        public void run() {
            Mobile mobile = new Mobile();
            mobile.message = Const.IntentKeyConst.KEY_CONNECT_HANG_UP;
            mobile.sid = mCallingSid;
            mobile.phoneNumber = LocalUserWrapper.getInstance().getLocalUser().getMobile();
            CLog.v("mPhoneNumber : " + mPhoneNumber);
            mMessageSendMgmt.sendMessage(getApplicationContext(), mobile.toString(),
                    mPhoneNumber, mHangUpCallback);
            mLYFaceTime.closePreview();
            finish();
        }
    };
    Runnable mCallTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mConnectSuccess) {
                mCallingWait.setText(R.string.connect_client_no_answer);
                mHandler.postDelayed(mHangUpRunnable, 2000);
            }
        }
    };
    private Runnable mBackgroundTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            showToast("进入后台超时，视频通话已中断！");
            mHandler.post(mHangUpRunnable);
        }
    };
    private LYGLCameraEncoderView mLyEncoderView;
    private String mP2pUrl;
    private CallBackListener<Integer> mCallbackListener = new CallBackListener<Integer>() {

        @Override
        public void onSuccess(Integer t) {
            CLog.d("onSuccess");
            changeToSuccessView();
            showToast("开始互联");
        }

        @Override
        public void onError(LYException exception) {
            CLog.d("onError");
            showToast("互联失败");
            finish();
        }
    };
    private ImageButton mIbSwitch;

    private Runnable mFinishRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };
    private Runnable mConnectFailRunnable = new Runnable() {
        @Override
        public void run() {
            Mobile mobile = new Mobile();
            mobile.message = Const.IntentKeyConst.KEY_CONNECT_FAIL;
            mobile.phoneNumber = LocalUserWrapper.getInstance().getLocalUser().getMobile();
            CLog.v("mPhoneNumber : " + mPhoneNumber);
            mMessageSendMgmt.sendMessage(getApplicationContext(), mobile.toString(),
                    mPhoneNumber, mConnectFailCallback);
        }
    };
    private String mCallingSid;
    private boolean mIsFinishing;

    /**
     * 展示视频播放相关信息
     */
    private void showParamPopUp() {
        CLog.v("showParamPopUp");
        mPopupMobileInterconnectParam.showAtLocation(mLYPlayer, Gravity.BOTTOM, 0, 150);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mobile_interconnect);
        //添加标记，分别是锁屏状态下显示，解锁，保持屏幕长亮，打开屏幕
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC
                , AudioManager.ADJUST_RAISE
                , AudioManager.FLAG_PLAY_SOUND);
        mMessageSendMgmt = (MessageSendMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(MessageSendMgmt.class);
        mOldTotalTxBytes = TrafficStats.getUidTxBytes(Utils.getAppUid());
        mOldTotalRxBytes = TrafficStats.getUidRxBytes(Utils.getAppUid());
        initView();
        initMobileConnect();
        createPhoneListener();
    }
    public void createPhoneListener() {
        TelephonyManager telephony = (TelephonyManager)getSystemService(
                Context.TELEPHONY_SERVICE);
        telephony.listen(new OnePhoneStateListener(),
                PhoneStateListener.LISTEN_CALL_STATE);
    }
    /**
     * 电话状态监听.
     *
     */
    class OnePhoneStateListener extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            CLog.d("[Listener]电话号码:"+incomingNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_RINGING:
                    CLog.d("[Listener]等待接电话:"+incomingNumber);
                    showToast("有来电，视频通话已挂断！");
                    finish();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    CLog.d( "[Listener]电话挂断:"+incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    CLog.d( "[Listener]通话中:"+incomingNumber);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    private void initView() {
        mFrom = getIntent().getStringExtra(Const.IntentKeyConst.KEY_FROM_WHERE);
        mPhoneNumber = getIntent().getStringExtra(Const.IntentKeyConst.KEY_PHONE_NUMBER);
        mP2pUrl = getIntent().getStringExtra(Const.IntentKeyConst.KEY_P2P_URL);
        CLog.v("mFrom " + mFrom + "mPhoneNumber: " + mPhoneNumber);
        String nickname = getIntent().getStringExtra(Const.IntentKeyConst.KEY_NICKNAME);

        mCallingHeader = (TextView) findViewById(R.id.tv_calling_header);
        mCallingTime = (TextView) findViewById(R.id.tv_calling_time);
        mCallingName = (TextView) findViewById(R.id.tv_calling_name);
        mCallingWait = (TextView) findViewById(R.id.tv_calling_wait);
        mLlActions = (LinearLayout) findViewById(R.id.ll_actions);
        mLYPlayer = (LYPlayer) findViewById(R.id.ly_mobile_interconnect_player);
        if (mPopupMobileInterconnectParam == null) {
            mPopupMobileInterconnectParam = new PopupMobileInterconnectParam(this, mLYPlayer, mOldTotalTxBytes, mOldTotalRxBytes);
            mPopupMobileInterconnectParam.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.mobile_msg_bg));
        }
        mLyEncoderView = (LYGLCameraEncoderView) findViewById(R.id.ly_encoder_view);
        LinearLayout mLlMobileInterconnect = (LinearLayout) findViewById(R.id.ll_mobile_interconnect);
        mIbSwitch = (ImageButton) findViewById(R.id.ib_switch_fore_back_camera);
        ImageButton mIbHangup = (ImageButton) findViewById(R.id.ib_hang_up);
        mTbVoice = (ToggleButton) findViewById(R.id.tb_voice);
        mIbSwitch.setVisibility(View.GONE);
        mTbVoice.setVisibility(View.GONE);
        mRlParams = (RelativeLayout) findViewById(R.id.rl_params);
        mRlParams.setClickable(false);
        mRlParams.setVisibility(View.INVISIBLE);

        if (mFrom.equals(Const.Actions.ACTION_ACTIVITY_CONTACTS)) {
            mCallingName.setVisibility(View.VISIBLE);
            mCallingName.setText(nickname);
            mCallingWait.setVisibility(View.VISIBLE);
        }

        mTbVoice.setChecked(true);
        mLlMobileInterconnect.setOnClickListener(mOnClickListener);
        mIbSwitch.setOnClickListener(mOnClickListener);
        mIbHangup.setOnClickListener(mOnClickListener);
        mRlParams.setOnClickListener(mOnClickListener);
        mTbVoice.setOnCheckedChangeListener(listener);
        mTbVoice.setClickable(false);
        int screenWidth = Utils.getScreenWidth(getApplicationContext());
        ViewGroup.LayoutParams mCallingNameLp = mCallingName.getLayoutParams();
        mCallingNameLp.width = screenWidth / 3;
        mCallingName.setLayoutParams(mCallingNameLp);
    }

    private void initMobileConnect() {
        SessionConfig sessionConfig = new SessionConfig.Builder()
                .withVideoBitrate(512000)
                .withAudioSampleRateInHz(16000)//音频采样率
                .withVideoResolution(640, 480)
                .withDesireadCamera(Camera.CameraInfo.CAMERA_FACING_FRONT)
                .withCameraDisplayOrientation(90)
                .withAudioChannels(1)//通道 1单通道 2双通道
                .useHardAudioEncode(false)
                .useHardVideoEncode(true)
                .useAudio(true)
                .useVideo(true)
                .build();
        //没有必须配置项，可直接使用默认值
        mLYFaceTime = new LYFaceTime(this, sessionConfig);

        //设置本地预览
        mLYFaceTime.setLocalPreview(mLyEncoderView);
        mLyEncoderView.setShape(SurfaceFrameShape.RECTANGLE);

//        mLYPlayer.setFitScreen(true);
        mLYPlayer.setScreenRatio(IScreenRatio.TYPE_PLAYER_RATIO_PROP_FULL);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLYPlayer.getLayoutParams();
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        layoutParams.width = heightPixels * 3 / 4;
        layoutParams.height = heightPixels;
        int screenWidth = Utils.getScreenWidth(getApplicationContext());
        //裁剪左右
        layoutParams.setMargins(-(heightPixels * 3 / 4 - screenWidth) / 2, 0,
                -(heightPixels * 3 / 4 - screenWidth) / 2, 0);
        mLYPlayer.setLayoutParams(layoutParams);


        mHandler.postDelayed(mCallTimeOutRunnable, 30000);
        if (mFrom.equals(Const.Actions.ACTION_ACTIVITY_CONTACTS)) {

            //设置远程播放器
            mLYFaceTime.setRemoteView(null, mLYPlayer);
            Mobile mobile = new Mobile();
            mobile.message = Const.IntentKeyConst.KEY_VIDEO_CALL;
            LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
            mobile.phoneNumber = localUser.getMobile();
            mobile.nickname = localUser.getNickName();
            mobile.p2pUrl = localUser.getPhoneConnectAddr();
            mCallingSid = System.currentTimeMillis()+"";
            mobile.sid = mCallingSid;
            mobile.uid = localUser.getUid();
            CLog.d(mobile.toString());
            mLYFaceTime.setCallBackListener(mCallbackListener);
            mMessageSendMgmt.sendMessage(getApplicationContext(), mobile.toString(), mPhoneNumber, new BaseCallBack<String>() {
                @Override
                public void error(ResponseError object) {
                    CLog.v("error");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mFrom.equals(Const.IntentKeyConst.KEY_FROM_OTHER)) {
                                mCallingWait.setText(R.string.connect_fail);
                            } else {
                                mCallingWait.setText(R.string.calling_error);
                            }
                        }
                    });
                }

                @Override
                public void success(String s) {
                    CLog.v("success " + System.currentTimeMillis());
                }
            });
        } else {
            mCallingSid = getIntent().getStringExtra(Const.IntentKeyConst.KEY_SESSION_ID);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCallingWait.setVisibility(View.VISIBLE);
                    mCallingWait.setText(R.string.connect_client_ing);
                    mCallingHeader.setText(R.string.connect_client_ing);
                }
            });
            CLog.d("mP2pUrl---" + mP2pUrl);
            mLYFaceTime.setRemoteView(null, mLYPlayer);
            mLYFaceTime.openRemote(mP2pUrl, new CallBackListener<Integer>() {

                @Override
                public void onSuccess(Integer t) {
                    CLog.d("onSuccess");
                    changeToSuccessView();
                    // 连接成功
                    showToast("连接成功");
                }

                @Override
                public void onError(final LYException exception) {
                    CLog.d("onError");
                    // 连接失败
                    CLog.e(exception.getMessage());
                    showToast(getString(R.string.connect_fail));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCallingWait.setVisibility(View.VISIBLE);
                            mCallingWait.setText(R.string.connect_fail);
                            mHandler.post(mConnectFailRunnable);
                            mHandler.postDelayed(mFinishRunnable,500);
                        }
                    });
                }
            });


        }
    }

    /**
     * 手机互联初始化
     */
    private void changeToSuccessView() {
        mHandler.postDelayed(mHideSomeButtonsRunnable, 3000);
        mHandler.post(mUpdateTimeRunnable);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRlParams.setClickable(true);
                mRlParams.setVisibility(View.VISIBLE);
                mCallingHeader.setText(R.string.in_the_call);
                mCallingName.setVisibility(View.INVISIBLE);
                mCallingWait.setVisibility(View.INVISIBLE);
                mCallingTime.setVisibility(View.VISIBLE);
                mIbSwitch.setVisibility(View.VISIBLE);
                mTbVoice.setVisibility(View.VISIBLE);
            }
        });
        mConnectSuccess = true;
        mTbVoice.setClickable(true);
        mHandler.removeCallbacks(mCallTimeOutRunnable);
    }


    @Override
    protected void onDestroy() {
        CLog.v("onDestroy");
        mIsFinishing = true;
        if (mLYFaceTime != null) {
            CLog.d("closeRemote---");
            mLYFaceTime.closeRemote(null);
            //mIsStop=true mRecordingFence.wait
            //PlatformAPI.getInstance().disconnect(fd);
            //mMediaPlayerAPI.CloseAudioPlayer(mMediaHandler);
            //mMediaPlayerAPI.CloseVideoPlayer(mMediaHandler);
            mLYFaceTime.release();
            //LYStreamEncoderAPI.getInstance().uninitVideoEncoder();
            //mCamEncoder.release();//释放Camera
            //LYStreamEncoderAPI.getInstance().uninitAudioEncoder();
        }
        if (mFrom.equals(Const.Actions.ACTION_ACTIVITY_CONTACTS)) {
            mLYFaceTime.setCallBackListener(null);
            mCallbackListener=null;
        }
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CLog.v("onPause");
        if (mConnectSuccess) {
            mLYFaceTime.stopVideoRecording();
////            mLYFaceTime.stopAudioRecording();
        }
        mLYFaceTime.onHostActivityPaused();
        mHandler.postDelayed(mBackgroundTimeOutRunnable, 30000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLYFaceTime.onHostActivityResumed();
        if (mConnectSuccess) {
            mLYFaceTime.startVideoRecording();
//            mLYFaceTime.startAudioRecording();
        }
        mHandler.removeCallbacks(mBackgroundTimeOutRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        CLog.v("onStop");
    }

    @Override
    protected void processNetworkChange(boolean isConnect) {
        super.processNetworkChange(isConnect);
        if (!isConnect) {
            mHandler.removeCallbacks(mUpdateTimeRunnable);
            mCallingTime.setVisibility(View.INVISIBLE);
            mCallingHeader.setText(R.string.calling_end);
            mLlActions.setVisibility(View.VISIBLE);
            //todo 网络异常处理
            finish();
        }
    }

    @Override
    protected void processMessage(final Mobile mobileMessage) {
        super.processMessage(mobileMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CLog.v("mobileMessage.message " + mobileMessage.message);
                if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_SEND_BUSY)) {
                    //如果是正在通话中，有其他人打进电话
                    CLog.v("第三人来电 " + mobileMessage.phoneNumber);
                    Mobile anotherCall = new Mobile();
                    anotherCall.message = Const.IntentKeyConst.KEY_LINE_IS_BUSY;
                    anotherCall.phoneNumber = mobileMessage.phoneNumber;
                    mMessageSendMgmt.sendMessage(getApplicationContext(), anotherCall.toString(),
                            anotherCall.phoneNumber, mBusyCallback);
                } else {
                    if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_NO_ANSWER)) {
                        //对方无应答
                        showToast(getString(R.string.no_answer_call_later));
                        finish();
                    } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECT_REFUSE)) {
                        //对方拒绝接听
                        showToast(getString(R.string.refuse_answer));
                        finish();
                    } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECT_FAIL)) {
                        mHandler.removeCallbacks(mCallTimeOutRunnable);
                        mCallingWait.setText(getString(R.string.connect_client_fail));
                        showToast(getString(R.string.connect_client_fail));
                        mHandler.post(mFinishRunnable);
                    } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECT_HANG_UP)
                            && mobileMessage.sid.equals(mCallingSid)) {
                        //对方主动挂断
                        showToast(getString(R.string.has_been_hung_up));
                        finish();
                    } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_LINE_IS_BUSY)) {
                        //拨打电话得知对方正在通话中，自动挂断
                        showToast(getString(R.string.line_is_busy));
                        finish();
                    } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECTION_ACCEPTED)) {
                        showToast("对方连接成功");
                    } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECTION_CLOSED)) {
                        showToast("通话已挂断");
                        //主叫方，断开连接，停止采集发送数据
//                        if (!mIsFinishing) {
//                            finish();
//                        }
                    }
                }
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mLlActions.getVisibility() == View.INVISIBLE) {
                mLlActions.setVisibility(View.VISIBLE);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
