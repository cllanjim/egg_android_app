package com.lingyang.camera.ui.activity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cms.voice.encoder.NumberEncoder;
import com.cms.voice.encoder.VoicePlayer;
import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.entity.Wifi;
import com.lingyang.camera.ui.widget.WaveformView;
import com.lingyang.camera.util.ActivityUtil;

public class VoiceBindingActivity extends AppBaseActivity {

    static final String SPILT_STRING = "ly";
    private BindReceiver mBindReceiver;
    private Wifi mWifi;
    private VoicePlayer mVoicePlayer;
    private AudioManager mAudioManager;
    private WaveformView mWave;
    private ImageView mIvScan, mIvTip;
    private Animation mAnimation;
    private boolean mIsSendEnd = true;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_voice_back:
                    if (mIsSendEnd) {
                        finish();
                    }
                    break;
                case R.id.tv_send:
                case R.id.rl_send:
                    mVoicePlayer.setFreqs(Const.SOUNDWAVE_FREQUENCY);
                    String uid = LocalUserWrapper.getInstance().getLocalUser().getUid().substring(4);
                    CLog.d("uid " + uid);
                    mVoicePlayer.play(NumberEncoder.encodeSSIDWiFi(
                            uid
                                    + SPILT_STRING + mWifi.getSSID()
                            , mWifi.getPASSWORD()), 1, 0);
                    break;
                case R.id.btn_next:

                    break;
                default:
                    break;
            }
        }
    };
    private TextView mTipTitleTextView, mCountDownTextView;
    private ProgressBar mConnPb;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mConnPb.getProgress() < 100)
                mConnPb.setProgress(mConnPb.getProgress() + 1);
            mHandler.postDelayed(mRunnable, 600);
        }
    };
    private FrameLayout mVoiceAnimationLayout;
    private TextView mTvSend;
    CountDownTimer mCountDownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            CLog.v("voice_countdown---" + millisUntilFinished);
            if (millisUntilFinished > 2000) {
                mCountDownTextView.setText(millisUntilFinished / 1000 + getText(R.string.voice_binding_countdown).toString());
            } else {
                mHandler.removeCallbacks(mRunnable);
                sendVoiceInit();
                showToast(getText(R.string.voice_conn_fail).toString());
            }
        }

        @Override
        public void onFinish() {

        }
    };
    VoicePlayer.Listener mListener = new VoicePlayer.Listener() {
        @Override
        public void onPlayStart() {
            mIsSendEnd = false;
            showToast(getString(R.string.voice_send_ing));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWave.setVisibility(View.VISIBLE);
                    mTvSend.setText("");
                    mIvScan.setBackground(getResources().getDrawable(R.drawable.loading_ic));
                    mIvScan.setVisibility(View.VISIBLE);
                    mIvScan.startAnimation(mAnimation);
                }
            });
        }

        @Override
        public void onPlayEnd() {
            showToast(getString(R.string.voice_send_success));
            mIsSendEnd = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvSend.setText(R.string.voice_send_success);
                    mWave.setVisibility(View.INVISIBLE);
                    mIvScan.clearAnimation();
                    mIvScan.setBackground(getResources().getDrawable(R.drawable.shape_send_voice_end));
                    cameraConnInit();

                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_binding);
        mWifi = (Wifi) getIntent().getSerializableExtra(Const.IntentKeyConst.KEY_WIFI);
        mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0);

        mVoicePlayer = new VoicePlayer();
        mVoicePlayer.setListener(mListener);
        mBindReceiver = new BindReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.Actions.ACTION_DEVICE_BIND_CONFIRM);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBindReceiver, filter);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_send_voice);
        mAnimation.setRepeatMode(Animation.RESTART);
        mAnimation.setRepeatCount(Animation.INFINITE);
        init();
    }

    private void init() {
        ImageView back = (ImageView) findViewById(R.id.iv_voice_back);
        TextView title = (TextView) findViewById(R.id.tv_header_title);
        mTvSend = (TextView) findViewById(R.id.tv_send);
        RelativeLayout send_voice = (RelativeLayout) findViewById(R.id.rl_send);
        Button next = (Button) findViewById(R.id.btn_next);
        mWave = (WaveformView) findViewById(R.id.wv_wave);
        mIvScan = (ImageView) findViewById(R.id.iv_scan);
        mIvTip = (ImageView) findViewById(R.id.img_tip);
        mTipTitleTextView = (TextView) findViewById(R.id.tv_tip);
        mCountDownTextView = (TextView) findViewById(R.id.tv_count_down);
        mConnPb = (ProgressBar) findViewById(R.id.pb_progressbar);
        mVoiceAnimationLayout = (FrameLayout) findViewById(R.id.layout_voice_animation);

        sendVoiceInit();

        title.setText(getText(R.string.app_bind));
        back.setOnClickListener(mOnClickListener);
        send_voice.setOnClickListener(mOnClickListener);
        next.setOnClickListener(mOnClickListener);
        mTvSend.setOnClickListener(mOnClickListener);
    }

    private void sendVoiceInit() {
        mTvSend.setText(R.string.voice_binding_send);
        mTipTitleTextView.setText(getText(R.string.voice_binding_tip));
        mIvTip.setImageResource(R.drawable.iphone_sound_wave);
        mConnPb.setVisibility(View.GONE);
        mCountDownTextView.setVisibility(View.GONE);
        mVoiceAnimationLayout.setVisibility(View.VISIBLE);
        mWave.setVisibility(View.INVISIBLE);
        mIvScan.setVisibility(View.INVISIBLE);
        mConnPb.setProgress(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBindReceiver);
        mCountDownTimer.cancel();
    }

    private void cameraConnInit() {
        mTipTitleTextView.setText(getText(R.string.camera_is_binding));
        mIvTip.setImageResource(R.drawable.camera_net);
        mConnPb.setVisibility(View.VISIBLE);
        mCountDownTextView.setVisibility(View.VISIBLE);
        mVoiceAnimationLayout.setVisibility(View.GONE);
        mHandler.postDelayed(mRunnable, 1000);
        mCountDownTimer.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC
                        , AudioManager.ADJUST_RAISE
                        , AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC
                        , AudioManager.ADJUST_LOWER
                        , AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
        }
        return mIsSendEnd && super.onKeyDown(keyCode, event);
    }

    public class BindReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String sn = intent.getStringExtra(Const.IntentKeyConst.KEY_MSG_DEVICE_BIND_CONFIRM);
            if (sn != null) {
                mConnPb.setProgress(100);
                mCountDownTimer.cancel();
                Intent intentAp = new Intent(
                        Const.Actions.ACTION_ACTIVITY_FOURTH_OF_ADD_DEVICE);
                intentAp.putExtra(Const.IntentKeyConst.KEY_SN, sn);
                ActivityUtil.startActivity(
                        VoiceBindingActivity.this, intentAp);
            }
        }
    }
}
