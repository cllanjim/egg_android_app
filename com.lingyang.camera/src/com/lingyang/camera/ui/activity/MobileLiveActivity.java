package com.lingyang.camera.ui.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.MobileInterconnectResponse;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.MobileLiveStartMgmt;
import com.lingyang.camera.mgmt.MobileLiveStopMgmt;
import com.lingyang.camera.ui.adapter.PrepareMobileLiveParamAdapter;
import com.lingyang.camera.util.DateTimeUtil;
import com.lingyang.camera.util.HardCoding;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.av.SessionConfig;
import com.lingyang.sdk.broadcast.BroadcastListener;
import com.lingyang.sdk.broadcast.LYLiveBroadcast;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.view.LYGLCameraEncoderView;

import java.util.ArrayList;

public class MobileLiveActivity extends AppBaseActivity {
    private final String BIT_RATE_LOW = "128kbps";
    private final String BIT_RATE_MIDDLE = "256kbps";
    private final String BIT_RATE_HIGH = "512kbps";
    private final String BIT_RATE_HIGHER = "1024kbps";
    long mMobileLiveTime = 0;
    BaseCallBack<String> mStopMobileLiveCallback = new BaseCallBack<String>() {
        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
        }

        @Override
        public void success(final String s) {
            CLog.v("watchnumbers " + s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvAttractNumber.setText(String.format(getString(R.string.attract_num), s));
                }
            });
        }
    };
    Runnable mUpdateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mTvMobileLiveTime.setText(String.format("%s",
                    DateTimeUtil.timeStampToDateByFormat(-3600 * 8 + mMobileLiveTime++,
                            "HH:mm:ss")));
            mHandler.postDelayed(mUpdateTimeRunnable, 1000);
        }
    };
    private ToggleButton mResolution, mTbUseFlashLight, mRate, mTbCodeType;
    PopupWindow.OnDismissListener mOnDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            mResolution.setChecked(false);
            mRate.setChecked(false);
            mTbCodeType.setChecked(false);
        }
    };
    private CheckBox mVolume, mLocation;
    private Button mBtnScreenShot;
    private TextView mTvResolutionValue, mTvRateValue, mTvCodeTypeValue, mStartMobileLive,
            mTvLocation, mTvAttractNumber, mTvMobileLiveHeader, mTvMobileLiveTime;
    private EditText mEtMobileLiveTitle;
    private RelativeLayout mRlMobileLive, mRlMobileLiveEnd;
    private LinearLayout mPrepareMobileLiveParam, mLlPrepareMobileLiveHeader;
    private ImageView mSwitchCamera, mIvMobileLiveParams;
    private LocationClient mLocationClient = null;
    private BDLocationListener myListener = new MyLocationListener();
    private PrepareMobileLiveParamPopup mParamPopup;
    private ArrayList<String> mResolutionsStrList, mRatesStr, mCodeTypeList;
    private MobileLiveStartMgmt mMobileLiveStartMgmt;
    private ActivityManager mActivityManager;
    private PopupLocalMobileParam mLocalParamPopup;
    private MobileLiveStopMgmt mMobileLiveStopMgmt;
    private String mMobileLiveTitle, mPhoneType,  mAddress;
    private InputMethodManager mManager;
    private boolean mIsMobileLive;
    private int mCurrentCodeType = HardCoding.VIDEO_DATA_SOFT;
    private long mOldTotalTxBytes;
    private long mOldTotalRxBytes;
    private SessionConfig mSessionConfig;
    private LYLiveBroadcast mMobileBroadcast;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()) {
                case R.id.tb_use_flash_light:
                    mMobileBroadcast.toggleFlash();
                    break;
                case R.id.cb_mobile_live_volume:


                    break;
                case R.id.cb_mobile_live_location:
                    if (mLocation.isChecked()) {
                        mLocationClient.start();
                        mLocationClient.registerLocationListener(myListener);
                        mTvLocation.setText(getString(R.string.is_gainning));
                    } else {
                        mLocationClient.stop();
                        mTvLocation.setText("");
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private BroadcastListener mBroadcastListener
            = new BroadcastListener() {
        @Override
        public void onBroadcastStart() {
            CLog.d("onBroadcastStart");
        }

        @Override
        public void onBroadcastLive() {
            CLog.d("onBroadcastLive");
            mIsMobileLive = true;
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            CLog.v("success");
            initMobileLiveView();
        }

        @Override
        public void onBroadcastStop() {
            CLog.d("onBroadcastStop");
        }

        @Override
        public void onBroadcastError(LYException e) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            CLog.e("onBroadcastError");
            mIsMobileLive = false;
            showToast(e.getMessage());
        }
    };
    private Runnable mStopMobileLiveRunnable = new Runnable() {
        @Override
        public void run() {
            CLog.d("StopMobileLive");
            stopMobileLive();
        }
    };
    private LYGLCameraEncoderView mLyCameraEncoderView;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_switch_fore_back_camera:
                    mMobileBroadcast.switchCamera();
                    mCurrentCamera = mMobileBroadcast.getCurrentCamera();
                    boolean currentIsBackCamera = mCurrentCamera == Camera.CameraInfo.CAMERA_FACING_BACK;
                    mTbUseFlashLight.setVisibility(currentIsBackCamera ? View.GONE : View.VISIBLE);
                    break;
                case R.id.iv_close_prepare_mobile_live:
                    if (mIsMobileLive) {
                        showStopMobileLiveDialog();
                    } else {
                        finish();
                    }
                    break;
                //选择分辨率
                case R.id.tv_resolution_value:
                    changeToggleButtonStatus(mResolution);
                    showParamPopup(mResolution, mResolutionsStrList, mTvResolutionValue);
                    break;
                case R.id.tb_mobile_live_resolution:
                    showParamPopup(mResolution, mResolutionsStrList, mTvResolutionValue);
                    break;
                //选择码率
                case R.id.tv_rate_value:
                    showParamPopup(mRate, mRatesStr, mTvRateValue);
                    changeToggleButtonStatus(mRate);
                case R.id.tb_mobile_live_rate:
                    showParamPopup(mRate, mRatesStr, mTvRateValue);
                    break;
                //选择编码类型
                case R.id.tv_code_type_value:
                    showParamPopup(mTbCodeType, mCodeTypeList, mTvCodeTypeValue);
                    changeToggleButtonStatus(mTbCodeType);
                case R.id.tb_mobile_live_code_type:
                    showParamPopup(mTbCodeType, mCodeTypeList, mTvCodeTypeValue);
                    break;
                //开始直播
                case R.id.tv_start_mobile_live:
                    CLog.d("mCurrentCamera----" + mCurrentCamera);
                    startMobileLive();
                    break;
                case R.id.rl_mobile_live_params:
                case R.id.iv_mobile_live_params:
                    mLocalParamPopup.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.mobile_msg_bg));
                    mLocalParamPopup.showAtLocation(mIvMobileLiveParams, Gravity.BOTTOM, 0, 150);
                    break;
                case R.id.btn_mobile_live_screenshot:
//                    snapShot();
                    break;
                case R.id.tv_back_to_home:
                    finish();
                    break;
                case R.id.rl_above:
                    //隐藏软键盘
                    if (mManager == null) {
                        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    }
                    if (getCurrentFocus() != null && getCurrentFocus().getId() == R.id.et_mobile_live_title) {
                        mManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
//                    if (mBtnScreenShot.getVisibility() == View.VISIBLE) {
//                        mBtnScreenShot.setVisibility(View.GONE);
//                    } else {
//                        mBtnScreenShot.setVisibility(View.VISIBLE);
//                    }
                    break;
                default:
                    break;
            }
        }
    };
    private int mCurrentCamera;

    private void startMobileLive() {
        if (!NetWorkUtils.isNetworkAvailable(getApplicationContext())) {
            showToast(getString(R.string.app_network_error));
            return;
        }
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        if (LYService.getInstance().isOnline()) {
            initMobileLive();
        }else {
            showToast("云平台离线，重新登录中...");
            LYService.getInstance().stopCloudService();
            LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
            if (localUser!=null) {
                LYService.getInstance().startCloudService(
                        localUser.getUserToken(),
                        localUser.getInitString(),
                        new CallBackListener<Long>() {
                            @Override
                            public void onSuccess(Long aLong) {
                                initMobileLive();
                            }

                            @Override
                            public void onError(LYException e) {
                                mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                                showToast(getString(R.string.cloud_login_fail));
                            }
                        });
            }
        }
    }

    private void initMobileLive() {
        mMobileLiveTitle = mEtMobileLiveTitle.getText().toString();
        if (mMobileLiveTitle.length() == 0) {
            LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
            CLog.d(localUser.toString());
            CLog.d(localUser.getNickName());
            mMobileLiveTitle = localUser
                    .getNickName() + "的手机直播";

        }
        mOldTotalTxBytes = TrafficStats.getUidTxBytes(Utils.getAppUid());
        mOldTotalRxBytes = TrafficStats.getUidRxBytes(Utils.getAppUid());
        CLog.d("MobileLive start get pushUrl!");
        mMobileLiveStartMgmt.getPushUrl(MobileLiveActivity.this, mPhoneType, mAddress,
                mMobileLiveTitle, new BaseCallBack<String>() {
                    @Override
                    public void error(ResponseError object) {
                        mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                        CLog.v("error");
                        if (object != null) {
                            showToast(object.error_msg);
                        } else {
                            showToast("手机直播请求失败");
                        }
                    }

                    @Override
                    public void success(String pushUrl) {
                        CLog.d("pushUrl==="+pushUrl);
                        mMobileBroadcast.setBroadcastListener(mBroadcastListener);
                        CLog.d("mMobileBroadcast.startBroadcasting");
                        mMobileBroadcast.startBroadcasting(pushUrl);
                    }
                });
    }

    @Override
    protected void processMessage(MobileInterconnectResponse.Mobile mobileMessage) {
        super.processMessage(mobileMessage);
        if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CLOSE_PLAY_AND_LIVE)) {
            CLog.d("mobileMessage.message---"+mobileMessage.message);
            if (mIsMobileLive) {
                stopMobileLive();
            }
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mobile_live2);
        mMobileLiveStartMgmt = (MobileLiveStartMgmt) MgmtClassFactory.getInstance().getMgmtClass(MobileLiveStartMgmt.class);
        mMobileLiveStopMgmt = (MobileLiveStopMgmt) MgmtClassFactory.getInstance().getMgmtClass(MobileLiveStopMgmt.class);
        mParamPopup = new PrepareMobileLiveParamPopup(this);
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        initView();
        initData();
        initConfig();
        initLocation();
        mLocationClient.registerLocationListener(myListener);
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
                    showToast("有来电，手机直播已停止！");
                    if (mIsMobileLive) {
                        stopMobileLive();
                    }
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
        mLyCameraEncoderView = (LYGLCameraEncoderView) findViewById(R.id.ly_encoder_view);
        RelativeLayout mRlAbove = (RelativeLayout) findViewById(R.id.rl_above);
        //头部
        mTbUseFlashLight = (ToggleButton) findViewById(R.id.tb_use_flash_light);
        mTvMobileLiveHeader = (TextView) findViewById(R.id.tv_mobile_live_header);
        mTbUseFlashLight.setChecked(false);
        mSwitchCamera = (ImageView) findViewById(R.id.iv_switch_fore_back_camera);
        ImageView mIvClosePrepareMobileLive = (ImageView) findViewById(R.id.iv_close_prepare_mobile_live);
        //参数
        mTvResolutionValue = (TextView) findViewById(R.id.tv_resolution_value);
        mTvRateValue = (TextView) findViewById(R.id.tv_rate_value);
        mTvLocation = (TextView) findViewById(R.id.tv_mobile_location);
        mTvCodeTypeValue = (TextView) findViewById(R.id.tv_code_type_value);

        mResolution = (ToggleButton) findViewById(R.id.tb_mobile_live_resolution);
        mRate = (ToggleButton) findViewById(R.id.tb_mobile_live_rate);
        mTbCodeType = (ToggleButton) findViewById(R.id.tb_mobile_live_code_type);
        mVolume = (CheckBox) findViewById(R.id.cb_mobile_live_volume);
        mLocation = (CheckBox) findViewById(R.id.cb_mobile_live_location);
        if (mLocation != null && mLocation.isChecked()) {
            mLocationClient.start();
        }
        mStartMobileLive = (TextView) findViewById(R.id.tv_start_mobile_live);
        //直播页
        mBtnScreenShot = (Button) findViewById(R.id.btn_mobile_live_screenshot);
        mBtnScreenShot.setVisibility(View.GONE);
        mIvMobileLiveParams = (ImageView) findViewById(R.id.iv_mobile_live_params);
        RelativeLayout mRlMobileLiveParams = (RelativeLayout) findViewById(R.id.rl_mobile_live_params);
        //结束页
        mTvAttractNumber = (TextView) findViewById(R.id.tv_attract_audience_number);
        TextView mTvBackToHome = (TextView) findViewById(R.id.tv_back_to_home);
        //隐藏显示
        mLlPrepareMobileLiveHeader = (LinearLayout) findViewById(R.id.ll_prepare_mobile_live_header);
        mTvMobileLiveTime = (TextView) findViewById(R.id.tv_mobile_live_time);
        mEtMobileLiveTitle = (EditText) findViewById(R.id.et_mobile_live_title);
        mPrepareMobileLiveParam = (LinearLayout) findViewById(R.id.ll_prepare_mobile_live);
        mRlMobileLive = (RelativeLayout) findViewById(R.id.rl_mobile_live);
        mRlMobileLiveEnd = (RelativeLayout) findViewById(R.id.rl_mobile_live_end);
        mTbUseFlashLight.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mResolution.setOnClickListener(mOnClickListener);
        mRate.setOnClickListener(mOnClickListener);
        mTbCodeType.setOnClickListener(mOnClickListener);
        mTvResolutionValue.setOnClickListener(mOnClickListener);
        mTvRateValue.setOnClickListener(mOnClickListener);
        mTvCodeTypeValue.setOnClickListener(mOnClickListener);
        mTvBackToHome.setOnClickListener(mOnClickListener);
        mVolume.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mLocation.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mStartMobileLive.setOnClickListener(mOnClickListener);
        mSwitchCamera.setOnClickListener(mOnClickListener);
        mIvClosePrepareMobileLive.setOnClickListener(mOnClickListener);
        mBtnScreenShot.setOnClickListener(mOnClickListener);
        mIvMobileLiveParams.setOnClickListener(mOnClickListener);
        mRlMobileLiveParams.setOnClickListener(mOnClickListener);
        mRlAbove.setOnClickListener(mOnClickListener);
        mParamPopup.setOnDismissListener(mOnDismissListener);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mLyCameraEncoderView.getLayoutParams();
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        layoutParams.width = heightPixels * 3 / 4;
        layoutParams.height = heightPixels;
        int screenWidth = Utils.getScreenWidth(this);
        layoutParams.setMargins(-(heightPixels * 3 / 4 - screenWidth) / 2, 0, 0, 0);//裁剪左右
        mLyCameraEncoderView.setLayoutParams(layoutParams);

    }

    /**
     * 获取分辨率信息
     */
    private void initData() {
        mPhoneType = Build.BRAND + "-" + Build.MODEL;
        mAddress = getResources().getString(R.string.mars);

        mResolutionsStrList = new ArrayList<String>();
        mResolutionsStrList.add("320*240");
        mResolutionsStrList.add("640*480");
        mResolutionsStrList.add("800*600");

        mRatesStr = new ArrayList<String>();
//        mRatesStr.add(BIT_RATE_LOW);
        mRatesStr.add(BIT_RATE_MIDDLE);
        mRatesStr.add(BIT_RATE_HIGH);
        mRatesStr.add(BIT_RATE_HIGHER);

        mCodeTypeList = new ArrayList<String>();
        mCodeTypeList.add(getString(R.string.coding_hard));
        mCodeTypeList.add(getString(R.string.coding_soft));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvRateValue.setText(mRatesStr.get(1));
            }
        });
    }
    int mBitrate  = 512;
    int mWidth  = 640;
    int mHeight  = 480;
    boolean mIsHardwareEncode = false;
    private void initConfig() {
        mTvResolutionValue.setText(String.format("%s*%s",mWidth,mHeight));

        mSessionConfig = new SessionConfig.Builder()
                .withVideoBitrate(mBitrate * 1024)
                .withAudioSampleRateInHz(16000)//音频采样率
                .withVideoResolution(mWidth, mHeight)
                .withDesireadCamera(Camera.CameraInfo.CAMERA_FACING_BACK)
                .withCameraDisplayOrientation(90)
                .withAudioChannels(1)
                .useHardAudioEncode(false)
                .useHardVideoEncode(mIsHardwareEncode)
                .useAudio(true)
                .useVideo(true)
                .build();

        mMobileBroadcast = new LYLiveBroadcast(this, mSessionConfig);
        mMobileBroadcast.setLocalPreview(mLyCameraEncoderView);
    }


    /**
     * 初始化定位设置
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy() {
        if (mLocalParamPopup != null) {
            mLocalParamPopup.stopRunnable();
        }
        mMobileBroadcast.release();
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        CLog.v("onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        CLog.v("onPause");
        mMobileBroadcast.onHostActivityPaused();
        mLocationClient.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        CLog.v("onResume");
        super.onResume();
        try {
            mMobileBroadcast.onHostActivityResumed();
        } catch (Exception e) {
            CLog.d(e.getMessage());
            showToast(getString(R.string.check_camera_permission));
        }
    }

    @Override
    protected void onStop() {
        CLog.v("onStop");
        super.onStop();
    }

    @Override
    protected void processNetworkChange(boolean isConnect) {
        super.processNetworkChange(isConnect);
        if (!isConnect) {
            if (mIsMobileLive) {
                mHandler.postDelayed(mStopMobileLiveRunnable, 1000);
            }
        }
    }

    private void changeToggleButtonStatus(ToggleButton tb) {
        if (tb != null) {
            tb.setChecked(!tb.isChecked());
        }
    }

    private void showParamPopup(ToggleButton tb, ArrayList<String> datas, final TextView tvParam) {
        if (tb.isChecked()) {
            PrepareMobileLiveParamAdapter adapter = new PrepareMobileLiveParamAdapter(this, datas);
            adapter.setChooseParamCallBackListener(new PrepareMobileLiveParamAdapter.ChooseParamListener() {
                @Override
                public void choose(String paramValue, int poi) {
                    mParamPopup.dismiss();
                    tvParam.setText(paramValue);
                    switch (tvParam.getId()) {
                        case R.id.tv_resolution_value:
                            if (mResolutionsStrList != null && mResolutionsStrList.size() > 0) {
                                String resolutionStr = mResolutionsStrList.get(poi);
                                String[] split = resolutionStr.split("\\*");
                                try {
                                    mWidth  = Integer.parseInt(split[0]);
                                    mHeight  = Integer.parseInt(split[1]);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    return;
                                }
                                CLog.d("--- " + split[0]+ " " + split[1]);
                                resetConfig(mBitrate,mWidth,mHeight,mIsHardwareEncode);
                            }
                            break;
                        case R.id.tv_rate_value:
                            mBitrate = Integer.parseInt(paramValue.split("kbps")[0]);
                            resetConfig(mBitrate,mWidth,mHeight,mIsHardwareEncode);
                            break;
                        case R.id.tv_code_type_value:
                            if (paramValue.equals(getString(R.string.coding_soft))) {
                                mCurrentCodeType = HardCoding.VIDEO_DATA_SOFT;
                                mIsHardwareEncode = false;
                            } else {
                                mCurrentCodeType = HardCoding.VIDEO_DATA_HARD;
                                mIsHardwareEncode = true;
                            }
                            resetConfig(mBitrate,mWidth,mHeight,mIsHardwareEncode);
                            break;
                        default:
                            break;
                    }
                }
            });
            mParamPopup.setAdapter(adapter);
            mParamPopup.setWidth(tvParam.getWidth() * 3 / 2 + tb.getWidth());
            mParamPopup.showAsDropDown(tvParam, 0, 0);
        }
    }

    private void resetConfig(int bitrate,int width,int height,boolean isHardwareEncode) {
        mSessionConfig = new SessionConfig.Builder()
                .withVideoBitrate(bitrate * 1024)
                .withAudioSampleRateInHz(16000)//音频采样率
                .withVideoResolution(width, height)
                .withDesireadCamera(mCurrentCamera)
                .withCameraDisplayOrientation(90)
                .withAudioChannels(1)
                .useHardAudioEncode(false)
                .useHardVideoEncode(isHardwareEncode)
                .useAudio(false)
                .useVideo(true)
                .build();
        mMobileBroadcast.reset(mSessionConfig);
    }


    private void initMobileLiveView() {
        mHandler.post(mUpdateTimeRunnable);
        if (mLocalParamPopup == null) {
            mLocalParamPopup = new PopupLocalMobileParam(this, mActivityManager,
                    mPhoneType, mAddress, mCurrentCodeType, mOldTotalTxBytes, mOldTotalRxBytes);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEtMobileLiveTitle.setVisibility(View.GONE);
                mPrepareMobileLiveParam.setVisibility(View.GONE);
                mRlMobileLive.setVisibility(View.VISIBLE);
                mTvMobileLiveTime.setVisibility(View.VISIBLE);
                mTvMobileLiveHeader.setText(getString(R.string.livecamera_state_living));
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mIsMobileLive) {
                showStopMobileLiveDialog();
                return false;
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);

    }

    private void showStopMobileLiveDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.system_prompt))
                .setMessage(getString(R.string.live_over_again))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                stopMobileLive();
            }
        }).create().show();
    }

    private void stopMobileLive() {
        CLog.d("stopMobileLive");
        mIsMobileLive = false;
        mMobileLiveTime = 0;
        if (mLocalParamPopup != null) {
            mLocalParamPopup.stopRunnable();
        }
        mHandler.removeCallbacks(mUpdateTimeRunnable);
        mLlPrepareMobileLiveHeader.setVisibility(View.GONE);
        mRlMobileLive.setVisibility(View.GONE);
        mTvMobileLiveTime.setVisibility(View.GONE);
        mRlMobileLiveEnd.setVisibility(View.VISIBLE);
        mMobileBroadcast.stopBroadcasting();
        mMobileLiveStopMgmt.stopMobileLive(getApplicationContext(), mAddress, mStopMobileLiveCallback);
    }

    /**
     * 定位信息监听
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getPoiList() != null && location.getPoiList().size() != 0) {
                Poi poi = (Poi) location.getPoiList().get(0);
                mAddress = location.getCountry() + " " + location.getProvince() +
                        " " + location.getCity() + " " + poi.getName();
                if (location.getCountry() == null || location.getProvince() == null
                        || location.getCity() == null) {
                    mAddress = poi.getName();
                }
                CLog.v("mAddress: " + mAddress);
            }
            mTvLocation.setSelected(true);
            mTvLocation.setText(mAddress);
            mLocationClient.unRegisterLocationListener(myListener);
        }
    }
}
