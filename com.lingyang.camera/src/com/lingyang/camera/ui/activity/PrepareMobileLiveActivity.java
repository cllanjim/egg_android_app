package com.lingyang.camera.ui.activity;

/**
 * 文件名: PrepareMobileLiveActivity
 * 描    述: 手机直播,该类负责手机直播前，直播中，直播结束的业务处理
 * 创建人: 杜舒
 * 创建时间: 2015/10
 */
public class PrepareMobileLiveActivity extends AppBaseActivity {

    /*private static final String BIT_RATE_LOW = "128kbps";
    private static final String BIT_RATE_MIDDLE = "256kbps";
    private static final String BIT_RATE_HIGH = "512kbps";
    private static final String BIT_RATE_HIGHER = "1024kbps";
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
    boolean isFirstOpen = true;
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
    private Context mContext;
    private LocationClient mLocationClient = null;
    private BDLocationListener myListener = new MyLocationListener();
    private Camera mCamera;
    private int mCurrentCameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;//默认后置
    private PrepareMobileLiveParamPopup mParamPopup;
    private SurfaceHolder mSurfaceHolder;
    private Camera.Parameters mParameters;
    private ArrayList<String> mResolutionsStr, mRatesStr, mCodeTypeList;
    private boolean mIsSwitched = true;
    private MobileLivePlayer mMobileLivePlayer;
    private MobileLiveStartMgmt mMobileLiveStartMgmt;
    private long mCurrentTime;
    private int mBitRateValue = 512;
    private ByteBuffer mCameraPreviewBuffer;
    private HardCoding mHardCoding = new HardCoding();
    private boolean mIsHardCodec = false;
    private ActivityManager mActivityManager;
    private ArrayList<Camera.Size> mSupportResolutions;
    private PopupLocalMobileParam mLocalParamPopup;
    private MobileLiveStopMgmt mMobileLiveStopMgmt;
    private String mMobileLiveTitle, mPhoneType, mRtmpAddr, mAddress;
    private byte[] mTemp;
    private InputMethodManager mManager;
    private Camera.Size mCurrentSize;
    private List<Camera.Size> mFrontSupportedPreviewSize;
    private boolean mAddCallback, mSurfaceDestroyed, mIsMobileLive;
    private int mRotate;
    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            CLog.v("surfaceCreated");
            mSurfaceDestroyed = false;
            startLocalPreview(mCurrentCameraPosition);
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            CLog.v("surfaceDestroyed");
            mSurfaceDestroyed = true;
            closeCamera();
        }
    };
    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()) {
                case R.id.tb_use_flash_light:
                    if (mCurrentCameraPosition == Camera.CameraInfo.CAMERA_FACING_FRONT || !mIsSwitched) {
                        mTbUseFlashLight.setChecked(false);
                        return;
                    }
                    Camera.Parameters param = mCamera.getParameters();
                    if (mTbUseFlashLight.isChecked()) {
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    } else {
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }
                    mCamera.setParameters(param);
                    break;
                case R.id.cb_mobile_live_volume:
                    AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMicrophoneMute(mVolume.isChecked());
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
    private Runnable mStopMobileLiveRunnable = new Runnable() {
        @Override
        public void run() {
            stopMobileLive();
        }
    };
    private HardCoding.VideoDataHandler mVideoDataHandler;
    private int mCurrentCodeType = HardCoding.VIDEO_DATA_HARD;
    *//**
     * 预览回调，在这里发送帧数据
     *//*
    Camera.PreviewCallback
            mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mIsMobileLive) {
                if (data != null) {
                    mTemp = data;
                    if (mCurrentCodeType == HardCoding.VIDEO_DATA_HARD) {
                        mHardCoding.offerEncoder(data, mRotate);
                    } else if (mCameraPreviewBuffer != null) {
                        mCameraPreviewBuffer.clear();
                        mCameraPreviewBuffer.put(data);
                        mMobileLivePlayer.sendVideoDataBuffer(mCameraPreviewBuffer, data.length, mRotate, mCurrentTime, HardCoding.VIDEO_DATA_SOFT);
                    }
                }
            }
            mCamera.addCallbackBuffer(data);
        }
    };
    private long mOldTotalTxBytes;
    private long mOldTotalRxBytes;
    BaseCallBack<String> mGetRtmpCallback = new BaseCallBack<String>() {
        @Override
        public void success(String rtmpAddr) {
            CLog.v("rtmpAddr : " + rtmpAddr);
            mRtmpAddr = rtmpAddr;
            ThreadPoolManagerQuick.execute(new Runnable() {
                @Override
                public void run() {
                    Camera.Size previewSize = mParameters.getPreviewSize();
                    CLog.v("previewSize.height " + previewSize.height +
                            " previewSize.width " + previewSize.width);
                    if (mVolume.isChecked()) {
                        mMobileLivePlayer.startTalk();
                    }
                    mIsHardCodec = mHardCoding.createVideoEncoder(previewSize.width, previewSize.height,
                            mBitRateValue, mRotate);
                    CLog.v("mIsHardCodec " + mIsHardCodec);
                    if (mCurrentCodeType == HardCoding.VIDEO_DATA_HARD) {
                        mHardCoding.setVideoDataHandler(mVideoDataHandler);
                    } else {
                        mMobileLivePlayer.initEncoder(previewSize.height, previewSize.width,
                                mBitRateValue);
                        int size = previewSize.height * previewSize.width * ImageFormat.
                                getBitsPerPixel(ImageFormat.NV21) / 8;
                        mCameraPreviewBuffer = ByteBuffer.allocateDirect(size);
                    }
                }
            });

            initMobileLiveView();
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            mIsMobileLive = true;
            mCurrentTime = SystemClock.currentThreadTimeMillis();
            mHandler.removeCallbacks(mUpdateTimeRunnable);
            mHandler.post(mUpdateTimeRunnable);
            if (mLocalParamPopup == null) {
                mLocalParamPopup = new PopupLocalMobileParam(mContext, mActivityManager,
                        mPhoneType, mAddress, mCurrentCodeType, mOldTotalTxBytes, mOldTotalRxBytes);
            }
        }

        @Override
        public void error(ResponseError object) {
            CLog.v("error");
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object != null) {
                showToast(object.error_code + object.error_msg);
            } else {
                showToast(getString(R.string.mobile_live_request_fail));
            }
            if (mVolume.isChecked()) {
                mMobileLivePlayer.stopTalk();
            }
            mMobileLivePlayer.disconnect();
        }
    };
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_switch_fore_back_camera:
                    if (mIsSwitched) {
                        switchCamera();
                    }
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
                case R.id.tb_mobile_live_resolution:
                    showParamPopup(mResolution, mResolutionsStr, mTvResolutionValue);
                    break;
                //选择码率
                case R.id.tv_rate_value:
                    changeToggleButtonStatus(mRate);
                case R.id.tb_mobile_live_rate:
                    showParamPopup(mRate, mRatesStr, mTvRateValue);
                    break;
                //选择编码类型
                case R.id.tv_code_type_value:
                    changeToggleButtonStatus(mTbCodeType);
                case R.id.tb_mobile_live_code_type:
                    showParamPopup(mTbCodeType, mCodeTypeList, mTvCodeTypeValue);
                    break;
                //开始直播
                case R.id.tv_start_mobile_live:
                    if (!NetWorkUtils.isNetworkAvailable(mContext)) {
                        showToast(getString(R.string.app_network_error));
                        return;
                    }
                    if (mCamera == null) {
                        showToast(getString(R.string.check_camera_permission));
                        return;
                    }
                    mMobileLiveTitle = mEtMobileLiveTitle.getText().toString();
                    if (mMobileLiveTitle.length() == 0) {
                        mMobileLiveTitle = LocalUserWrapper.getInstance().getLocalUser()
                                .getNickName() + "的手机直播";
                    }
                    if (mVideoDataHandler == null) {
                        mVideoDataHandler = new HardCoding.VideoDataHandler() {
                            @Override
                            public void onSendVideoDataBuffer(ByteBuffer data, int length, long videoTimestamp) {
                                mMobileLivePlayer.sendVideoDataBuffer(data, length, mRotate, videoTimestamp, HardCoding.VIDEO_DATA_HARD);
                            }

                            @Override
                            public boolean onProcessCameraPreview(int videoWidth, int videoHeight,
                                                                  int mRotate, int imageFormat,
                                                                  int videoColorFmt, ByteBuffer yuvBuffer,
                                                                  ByteBuffer outputBuffer) {
                                return mMobileLivePlayer.processCameraPreview(videoWidth, videoHeight,
                                        mRotate, imageFormat, videoColorFmt, yuvBuffer, outputBuffer);
                            }
                        };
                    }
                    mOldTotalTxBytes = TrafficStats.getUidTxBytes(Utils.getAppUid());
                    mOldTotalRxBytes = TrafficStats.getUidRxBytes(Utils.getAppUid());
                    startMobileLive();
                    break;
                case R.id.rl_mobile_live_params:
                case R.id.iv_mobile_live_params:

                    mLocalParamPopup.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.mobile_msg_bg));
                    mLocalParamPopup.showAtLocation(mIvMobileLiveParams, Gravity.BOTTOM, 0, 150);
                    break;
                case R.id.btn_mobile_live_screenshot:
                    snapShot();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_prepare_mobile_live);
        mMobileLiveStartMgmt = (MobileLiveStartMgmt) MgmtClassFactory.getInstance().getMgmtClass(MobileLiveStartMgmt.class);
        mMobileLiveStopMgmt = (MobileLiveStopMgmt) MgmtClassFactory.getInstance().getMgmtClass(MobileLiveStopMgmt.class);
        mContext = PrepareMobileLiveActivity.this;
        mParamPopup = new PrepareMobileLiveParamPopup(mContext);
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        initView();
        initLocation();
    }

    private void initView() {
        SurfaceView mSVMobileLivePreview = (SurfaceView) findViewById(R.id.sv_prepare_mobile_live_bg);
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
        if (mLocation.isChecked()) {
            mLocationClient.start();
        }
        mStartMobileLive = (TextView) findViewById(R.id.tv_start_mobile_live);
        //直播页
        mBtnScreenShot = (Button) findViewById(R.id.btn_mobile_live_screenshot);
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

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mSVMobileLivePreview.getLayoutParams();
        int heightPixels = mContext.getResources().getDisplayMetrics().heightPixels;
        layoutParams.width = heightPixels * 3 / 4;
        layoutParams.height = heightPixels;
        int screenWidth = Utils.getScreenWidth(mContext);
        layoutParams.setMargins(-(heightPixels * 3 / 4 - screenWidth) / 2, 0, 0, 0);//裁剪左右
        mSVMobileLivePreview.setLayoutParams(layoutParams);

        mSurfaceHolder = mSVMobileLivePreview.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        getFrontCameraPreviewSizes();

    }

    *//**
     * 初始化定位设置
     *//*
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

    *//**
     * 获取前置相机支持的分辨率
     *//*
    private void getFrontCameraPreviewSizes() {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mFrontSupportedPreviewSize = mCamera.getParameters().getSupportedPreviewSizes();
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            mCamera = null;
            showToast(getString(R.string.check_camera_permission));
        } finally {
            if (mFrontSupportedPreviewSize == null) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        ThreadPoolManagerNormal.clearTask();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        CLog.v("onPause");
        mLocationClient.stop();
        if (mIsMobileLive) {
            stopMobileLive();
        }
        closeCamera();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mAddCallback || mSurfaceDestroyed) {
            mSurfaceHolder.addCallback(mCallback);
            mAddCallback = true;
        } else {
            startLocalPreview(mCurrentCameraPosition);
        }
    }

    *//**
     * 开启本地预览
     *
     * @param i 前置或后置
     *//*
    private void startLocalPreview(final int i) {
        closeCamera();
        mCamera = Camera.open(i);//打开当前选中的摄像头
        mRotate = i == Camera.CameraInfo.CAMERA_FACING_BACK ? 270 : 90;
        CLog.v("打开当前选中的摄像头");
        mParameters = mCamera.getParameters();
        ArrayList<Camera.Size> mResolutions =
                (ArrayList<Camera.Size>) mParameters.getSupportedPreviewSizes();
        initData(mResolutions);
        ThreadPoolManagerQuick.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> focusModes = mParameters.getSupportedFocusModes();
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        mParameters.setFocusMode(
                                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    }
                    mCamera.setDisplayOrientation(90);
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    List<Integer> supportedPictureFormats = mParameters.getSupportedPictureFormats();
                    mParameters.setPictureFormat(supportedPictureFormats.get(0));
                    if (mCurrentSize != null) {
                        CLog.v("mCurrentSize: " + mCurrentSize.width + " " + mCurrentSize.height);
                        mParameters.setPreviewSize(mCurrentSize.width,
                                mCurrentSize.height); // 指定preview的大小
                    } else {
                        mParameters.setPreviewSize(mSupportResolutions.get(0).width,
                                mSupportResolutions.get(0).height);
                    }
                    mCamera.setParameters(mParameters);
                    Camera.Size preSize = mParameters.getPreviewSize();
                    CLog.v("---" + preSize.width + " " + preSize.height);
                    int size = preSize.width * preSize.height * ImageFormat.
                            getBitsPerPixel(ImageFormat.NV21) / 8;
                    mCamera.addCallbackBuffer(new byte[size]);
                    mCamera.addCallbackBuffer(new byte[size]);
                    mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
                    mCamera.startPreview();//开始预览
                    mIsSwitched = true;
                    mSwitchCamera.setClickable(true);
                    mCurrentCameraPosition = i;
                } catch (Exception e) {
                    e.printStackTrace();
                    mCamera = null;
                    showToast(getString(R.string.check_camera_permission));
                } finally {
                    if (mCamera == null) {
                        finish();
                    }
                }
            }
        });
    }

    *//**
     * 获取分辨率信息
     *
     * @param mResolutions
     *//*
    private void initData(ArrayList<Camera.Size> mResolutions) {
        mSupportResolutions = new ArrayList<Camera.Size>();
        //对分辨率从小到大排序
        Collections.sort(mResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                Integer wl = lhs.width;
                Integer wr = rhs.width;
                return wl.compareTo(wr);
            }
        });
        //取前后置分辨率交集
        List<Camera.Size> intersection = Utils.getIntersection(mFrontSupportedPreviewSize, mResolutions);
        mResolutionsStr = new ArrayList<String>();
        for (Camera.Size size : intersection) {
            if ((size.width + 0f) / size.height == 4f / 3 && size.width <= 1300) {
                mResolutionsStr.add(size.width + "*" + size.height);
                mSupportResolutions.add(size);
            }
        }
        mRatesStr = new ArrayList<String>();
        mRatesStr.add(BIT_RATE_LOW);
        mRatesStr.add(BIT_RATE_MIDDLE);
        mRatesStr.add(BIT_RATE_HIGH);
        mRatesStr.add(BIT_RATE_HIGHER);

        mCodeTypeList = new ArrayList<String>();
        mCodeTypeList.add(getString(R.string.coding_soft));
        if (mHardCoding.isSupportHardCoding()) {
            mCodeTypeList.add(getString(R.string.coding_hard));
        }
        if (!mIsMobileLive) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentSize == null) {
                        mCurrentSize = mSupportResolutions.get(0);
                        mTvResolutionValue.setText(mResolutionsStr.get(0));
                        mTvRateValue.setText(mRatesStr.get(2));
                    }
                }
            });
            if (isFirstOpen) {
                if (mSupportResolutions != null && mSupportResolutions.size() > 0) {
                    for (int i = 1; i < mSupportResolutions.size(); i++) {
                        Camera.Size size = mSupportResolutions.get(i);
                        if (size.width >= 480 && size.width <= 640) {
                            mCurrentSize = size;
                            final int finalI = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTvResolutionValue.setText(mResolutionsStr.get(finalI));
                                }
                            });
                            break;
                        }
                    }
                }
            }
            isFirstOpen = false;
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
            mStartMobileLive.setOnClickListener(null);
            if (mIsMobileLive) {
                mHandler.postDelayed(mStopMobileLiveRunnable, 1000);
            }
        } else {
            mStartMobileLive.setOnClickListener(mOnClickListener);
        }
    }

    *//**
     * 停止手机直播
     *//*
    private void stopMobileLive() {
        if (mEtMobileLiveTitle.getVisibility() == View.GONE) {
            //正在直播
            mIsMobileLive = false;
            mMobileLiveTime = 0;
            if (mLocalParamPopup != null) {
                mHandler.removeCallbacks(mLocalParamPopup.mRunnable);
            }
            mHandler.removeCallbacks(mUpdateTimeRunnable);
            mLlPrepareMobileLiveHeader.setVisibility(View.GONE);
            mRlMobileLive.setVisibility(View.GONE);
            mTvMobileLiveTime.setVisibility(View.GONE);
            mRlMobileLiveEnd.setVisibility(View.VISIBLE);
            stopMobileLiveThread();
            if (mVolume.isChecked()) {
                mMobileLivePlayer.stopTalk();
            }
            mMobileLivePlayer.disconnect();
            mHardCoding.releaseVideoEncoder();
            mCameraPreviewBuffer = null;
            CLog.v("disconnect");
        } else {
            //返回首页
            finish();
        }
    }

    *//**
     * 关闭相机
     *//*
    private void closeCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void stopMobileLiveThread() {
        ThreadPoolManagerNormal.execute(new Runnable() {
            @Override
            public void run() {
                mMobileLiveStopMgmt.stopMobileLive(mContext, mAddress, mStopMobileLiveCallback);
            }
        });
    }

    *//**
     * 切换前后置
     *//*
    private void switchCamera() {
        mSwitchCamera.setClickable(false);
        mIsSwitched = false;
        //切换前后摄像头
        final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        final int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (mCurrentCameraPosition == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                //现在是前置，变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTbUseFlashLight.setChecked(false);
                            mTbUseFlashLight.setVisibility(View.VISIBLE);
                        }
                    });
                    startLocalPreview(Camera.CameraInfo.CAMERA_FACING_BACK);
                    break;
                }
            } else {
                //现在是前置1， 变更为后置0
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTbUseFlashLight.setChecked(false);
                            mTbUseFlashLight.setVisibility(View.INVISIBLE);
                        }
                    });
                    startLocalPreview(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    break;
                }
            }
        }
    }

    private void changeToggleButtonStatus(ToggleButton tb) {
        if (tb.isChecked()) {
            tb.setChecked(false);
        } else {
            tb.setChecked(true);
        }
    }

    private void showParamPopup(ToggleButton tb, ArrayList<String> datas, final TextView tvParam) {
        if (tb.isChecked()) {
            PrepareMobileLiveParamAdapter adapter = new PrepareMobileLiveParamAdapter(mContext, datas);
            adapter.setChooseParamCallBackListener(new PrepareMobileLiveParamAdapter.ChooseParamListener() {
                @Override
                public void choose(String paramValue, int poi) {
                    mParamPopup.dismiss();
                    tvParam.setText(paramValue);
                    switch (tvParam.getId()) {
                        case R.id.tv_resolution_value:
                            changeResolutionPreview(poi);
                            break;
                        case R.id.tv_rate_value:
                            mBitRateValue = Integer.parseInt(paramValue.split("kbps")[0]);
                            break;
                        case R.id.tv_code_type_value:
                            if (paramValue.equals(getString(R.string.coding_soft))) {
                                mCurrentCodeType = HardCoding.VIDEO_DATA_SOFT;
                            } else {
                                mCurrentCodeType = HardCoding.VIDEO_DATA_HARD;
                            }
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

    *//**
     * 改变分辨率并预览
     *
     * @param poi
     *//*
    private void changeResolutionPreview(final int poi) {
        if (mCamera == null) {
            return;
        }
        mCamera.stopPreview();
        mCurrentSize = mSupportResolutions.get(poi);
        ThreadPoolManagerQuick.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    CLog.v("mCurrentSize: " + mCurrentSize.width + " " + mCurrentSize.height);
                    mParameters.setPreviewSize(mCurrentSize.width, mCurrentSize.height); // 指定preview的大小
                    mCamera.setParameters(mParameters);
                    mCamera.setPreviewCallback(mPreviewCallback);
                    Camera.Size preSize = mParameters.getPreviewSize();
                    CLog.v(preSize.width + "  " + preSize.height);
                    int s = preSize.width * preSize.height * ImageFormat.
                            getBitsPerPixel(ImageFormat.NV21) / 8;
                    mCamera.addCallbackBuffer(new byte[s]);
                    mCamera.addCallbackBuffer(new byte[s]);
                    mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
                    mCamera.startPreview();//开始预览
                    mIsSwitched = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    *//**
     * 开始手机直播
     *//*
    private void startMobileLive() {
        ThreadPoolManagerQuick.execute(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
                mMobileLivePlayer = MobileLivePlayer.getInstance(mContext);
                mMobileLivePlayer.connectServer(IMobileLivePlayer.MODE_LIVE,
                        new IMobileLivePlayer.CallBackListener() {
                            @Override
                            public void onError() {
                                mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast(getString(R.string.connect_cloud_server_fail));
                                    }
                                });
                            }

                            @Override
                            public void onSuccess(Object o) {
                                getRtmpAddr();
                            }
                        });
            }
        });
    }

    public void getRtmpAddr() {
        mPhoneType = Build.BRAND + "-" + Build.MODEL;
        if (mAddress == null || !mLocation.isChecked()) {
            mAddress = getString(R.string.mars);
        }
        mMobileLiveStartMgmt.getPushUrl(mContext, mPhoneType, mAddress, mMobileLiveTitle, mGetRtmpCallback);
    }

    private void initMobileLiveView() {
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

    *//**
     * 截图
     *//*
    public void snapShot() {
        final long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        final String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                .format(date) + "_" + System.currentTimeMillis() % 1000;
        final String snapPath = FileUtil.getInstance().getSnapshotFile()
                .getAbsolutePath();
        final String saveFullPath = snapPath + "/" + name + ".jpg";
        ThreadPoolManagerQuick.execute(new Runnable() {
            @Override
            public void run() {
                takePicture(timestamp, name, saveFullPath);
            }
        });
    }

    private void takePicture(final long timestamp, final String name, final String saveFullPath) {
        Camera.Size size = mParameters.getPreviewSize();
        try {
            YuvImage image = new YuvImage(mTemp, ImageFormat.NV21, size.width, size.height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream);
            Bitmap rotateBm = getRotateBitmap(stream);
            //保存图片
            File file = new File(saveFullPath);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            rotateBm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.close();
            stream.close();
            String cid = mRtmpAddr.substring(mRtmpAddr.lastIndexOf('/'));
            boolean flg = LocalRecordWrapper.getInstance().addLocalMedia(
                    new LocalRecord(name, timestamp, 0, LocalRecord.TYPE_MEDIA_PHOTO,
                            saveFullPath, saveFullPath, timestamp, cid,
                            LocalUserWrapper.getInstance().getLocalUser().getUid()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(getString(R.string.snapshot_suc));
                }
            });
            CLog.v("addLocalMedia-mobile -flg:" + flg);
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(getString(R.string.snapshot_fail));
                }
            });
            e.printStackTrace();
        }
    }

    *//**
     * 旋转图片
     *
     * @param stream
     * @return
     *//*
    private Bitmap getRotateBitmap(ByteArrayOutputStream stream) {
        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
        //判断前置后置 进行旋转
        Matrix matrix = new Matrix();
        matrix.postRotate(mCurrentCameraPosition == Camera.CameraInfo.CAMERA_FACING_BACK ? 90 : -90);
        if (mCurrentCameraPosition == 1) {
            matrix.postScale(-1, 1);//水平翻转
        }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
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
        new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.live_over))
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

    *//**
     * 定位信息监听
     *//*
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
            } else {
                mAddress = getString(R.string.mars);
            }
            mTvLocation.setSelected(true);
            mTvLocation.setText(mAddress);
            mLocationClient.unRegisterLocationListener(myListener);
        }
    }*/

}
