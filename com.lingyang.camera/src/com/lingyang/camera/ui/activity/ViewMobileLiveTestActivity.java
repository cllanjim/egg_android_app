package com.lingyang.camera.ui.activity;

/**
 * 文件名: ViewMobileLiveTestActivity
 * 描    述: 视频采集，播放测试类
 * 创建人:
 * 创建时间: 2015/10
 */
public class ViewMobileLiveTestActivity extends AppBaseActivity {

    /*VideoPlayerView mLYPlayer;
    IMobileLivePlayer mPlayer;
    SurfaceView mSurfaceView;
    VideoCaptureAndroid m_VideoCaptureAndroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CLog.v("onCreate");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_view_mobile_live);
        m_VideoCaptureAndroid = new VideoCaptureAndroid(0, 0);
        initView();
    }

    @Override
    protected void onResume() {
        mLYPlayer.resumeFromBackground();
        ThreadPoolManagerQuick.execute(new Runnable() {

            @Override
            public void run() {
//                play();
                playFromOther();
            }
        });
        super.onResume();
    }

    @Override
    protected void onPause() {
        mLYPlayer.pauseToBackground();
        ThreadPoolManagerQuick.execute(new Runnable() {

            @Override
            public void run() {
                m_VideoCaptureAndroid.stopCapture();
                mPlayer.stopTalk();
                mPlayer.disconnect();
            }
        });
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            return;
        }
        super.onBackPressed();
    }

    void playFromOther() {
        mPlayer.connectClient("35574C00662C0000F89DC3CB5000", new IMobileLivePlayer.ConnectClientListener() {
            @Override
            public void onConnectted(long time) {
                v("onConnectted");
                sendOther();
            }

            @Override
            public void onFail() {
                v("onFail");
            }
        });

    }

    private void sendOther() {
        mPlayer.initEncoder(480, 640, 512);
        mPlayer.startTalk();
        m_VideoCaptureAndroid.startCapture(640, 480);
        mPlayer.setOnPlayingListener(new ILivePlayer.OnPlayingListener() {
            @Override
            public void onPreparing() {
                v("onPreparing");
            }

            @Override
            public void onDeviceOffline() {
                v("onDeviceOffline");
            }

            @Override
            public void onConnecting() {
                v("onConnecting");
            }

            @Override
            public void onLiving(long time) {
                v("onLiving:" + time);
            }

            @Override
            public void onStoping() {
                v("onStoping");
            }

            @Override
            public void onStopped() {

            }

            @Override
            public void onError(int errorCode, String msg) {
                v("onError");
            }
        });
    }

    private void initView() {
        mLYPlayer = (VideoPlayerView) findViewById(R.id.videoplayer_layout);
        mLYPlayer.setScreenType(VideoPlayerView.TYPE_PLAYER_RATIO_PROP_3X4);
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_mobilelive);
        mLYPlayer.setPlayerType(IPlayer.TPPE_MOBILE);
        mPlayer = (IMobileLivePlayer) mLYPlayer.getPlayer();
        m_VideoCaptureAndroid.setLocalPreview(mSurfaceView.getHolder());
    }

    private void play() {
        String token = mPlayer.getConnectionToken();
        CLog.v(" mPlayer.getConnectionToken()" + token);
        mPlayer.initEncoder(480, 640, 512);
        mPlayer.startTalk();
        mPlayer.setOnPlayingListener(new ILivePlayer.OnPlayingListener() {
            @Override
            public void onPreparing() {
                v("onPreparing");
            }

            @Override
            public void onDeviceOffline() {
                v("onDeviceOffline");
            }

            @Override
            public void onConnecting() {
                v("onConnecting");
            }

            @Override
            public void onLiving(long time) {
                v("onLiving:" + time);
            }

            @Override
            public void onStoping() {
                v("onStoping");
            }

            @Override
            public void onStopped() {

            }

            @Override
            public void onError(int errorCode, String msg) {
                v("onError");
            }
        });
        m_VideoCaptureAndroid.startCapture(640, 480);

    }

    public void onClick_Back(View view) {
        onBackPressed();
    }*/

}
