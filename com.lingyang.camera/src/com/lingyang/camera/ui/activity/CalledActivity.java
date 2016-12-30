package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalCallListWrapper;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalCall;
import com.lingyang.camera.entity.CallContactResponse;
import com.lingyang.camera.entity.MobileInterconnectResponse.Mobile;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.MessageSendMgmt;
import com.lingyang.camera.mgmt.SearchCallContactMgmt;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.FastBlur;
import com.lingyang.camera.util.Utils;

import java.util.List;

/**
 * 文件名：CalledActivity
 * 描述：此类是来电处理类，包括锁屏来电，来电铃声，来电接听/拒听
 * 创建人：廖蕾
 * 时间：2015/11
 */
public class CalledActivity extends AppBaseActivity {

    BaseCallBack<String> mRefuseAnswerCallback = new BaseCallBack<String>() {
        @Override
        public void error(ResponseError object) {
            CLog.v("mRefuseAnswerCallback  error");
        }

        @Override
        public void success(String s) {
            CLog.v(s);
        }
    };
    BaseCallBack<String> mBusyCallback = new BaseCallBack<String>() {
        @Override
        public void error(ResponseError object) {
            CLog.d("mBusyCallback error");
        }

        @Override
        public void success(String s) {
            CLog.d("mBusyCallback success");
        }
    };
    private ImageView mBgImg;
    private MessageSendMgmt mMessageSendMgmt;
    private Mobile mMobile;
    private String mPhonenumber;
    View.OnClickListener mOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_cancel:
                    Mobile mobile = new Mobile();
                    mobile.message = Const.IntentKeyConst.KEY_CONNECT_REFUSE;
                    mMessageSendMgmt.sendMessage(CalledActivity.this, mobile.toString(),
                            mPhonenumber, mRefuseAnswerCallback);
                    finish();
                    break;
                case R.id.btn_answer:
                    gotoMobileInterconnect();
                    break;
                default:
                    break;
            }
        }
    };
    private void sendClosePlayerBroadcast(){
        Intent intent = new Intent();
        intent.setAction(Const.Actions.ACTION_MOBILE_MESSAGE);
        intent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,Const.IntentKeyConst.REFRESH_FROM_CALL_ACTIVITY);
        intent.putExtra(Const.IntentKeyConst.KEY_MOBILE_MESSAGE,Const.IntentKeyConst.KEY_CLOSE_PLAY_AND_LIVE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void gotoMobileInterconnect() {
        Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_MOBILE_INTERCONNECT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                Const.IntentKeyConst.KEY_FROM_OTHER);
        intent.putExtra(Const.IntentKeyConst.KEY_PHONE_NUMBER, mMobile.phoneNumber);
        intent.putExtra(Const.IntentKeyConst.KEY_P2P_URL,
                mMobile.p2pUrl);
        intent.putExtra(Const.IntentKeyConst.KEY_SESSION_ID,mMobile.sid);
        ActivityUtil.startActivity(CalledActivity.this, intent);
        finish();
    }
    @Override
    protected void processNetworkChange(boolean isConnect) {
        super.processNetworkChange(isConnect);
        if (!isConnect) {
            finish();
        }
    }

    Runnable mCallTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Mobile mobile = new Mobile();
            mobile.message = Const.IntentKeyConst.KEY_NO_ANSWER;
            mMessageSendMgmt.sendMessage(CalledActivity.this, mobile.toString(),
                    mPhonenumber, mRefuseAnswerCallback);
            finish();
        }
    };
    private MediaPlayer mCurrentMediaPlayer;
    private Context mContext;
    private SearchCallContactMgmt mSearchCallContactMgmt;
    private RoundImageView mHead;
    private BaseCallBack<List<CallContactResponse.CallContact>> mGetFaceImageCallback
            = new BaseCallBack<List<CallContactResponse.CallContact>>() {
        @Override
        public void error(ResponseError object) {
            showToast(getString(R.string.faceimage_get_fail));
            //缓存到本地数据库
            saveCallHistory(mMobile, "");
        }

        @Override
        public void success(List<CallContactResponse.CallContact> callContacts) {
            if (callContacts != null && callContacts.size() > 0) {
                final CallContactResponse.CallContact callContact = callContacts.get(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.displayUserIconImageView(mHead, callContact.face_image);
                        Utils.displayUserIconImageView(mBgImg, callContact.face_image);
                    }
                });
                //缓存到本地数据库
                CLog.v("callContact.face_image " + callContact.face_image);
                saveCallHistory(mMobile, callContact.face_image);
                sendRefreshContactsBroadcast();
            }
        }
    };

    private void sendRefreshContactsBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Const.Actions.ACTION_REFRESH_CONTACTS_BROADCAST);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_called);
        mContext = CalledActivity.this;
        //添加标记，分别是锁屏状态下显示，解锁，保持屏幕长亮，打开屏幕
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        playSound();
        mMessageSendMgmt = (MessageSendMgmt)
                MgmtClassFactory.getInstance().getMgmtClass(MessageSendMgmt.class);
        mSearchCallContactMgmt = (SearchCallContactMgmt) MgmtClassFactory.
                getInstance().getMgmtClass(SearchCallContactMgmt.class);
        initView();
        mHandler.postDelayed(mCallTimeOutRunnable, 29000);
        sendClosePlayerBroadcast();
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
                    showToast("有来电，请稍候再试！");
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

    @Override
    protected void onStart() {
        super.onStart();
        CLog.d("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        CLog.d("onResume");
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mCallTimeOutRunnable);
        stopPlaySound();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        CLog.v("onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        CLog.v("onStop");
        super.onStop();
    }

    @Override
    protected void processMessage(Mobile mobileMessage) {
        super.processMessage(mobileMessage);
        if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECT_HANG_UP)
                && mobileMessage.phoneNumber.equals(mPhonenumber)) {
            showToast(getString(R.string.has_been_hung_up));
            finish();
        } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_LINE_IS_BUSY)) {
            CLog.v("第三人来电 " + mobileMessage.phoneNumber);
            Mobile anotherCall = new Mobile();
            anotherCall.message = Const.IntentKeyConst.KEY_LINE_IS_BUSY;
            anotherCall.phoneNumber = mobileMessage.phoneNumber;
            mMessageSendMgmt.sendMessage(mContext, anotherCall.toString(), anotherCall.phoneNumber, mBusyCallback);
        } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_LINE_IS_BUSY)) {
            showToast(getString(R.string.line_is_busy));
            finish();
        } else if (mobileMessage.message.equals(Const.IntentKeyConst.KEY_CONNECTION_CLOSED)) {
            CLog.d("mobileMessage.message---" + mobileMessage.message);
        }
    }

    /**
     * 停止播放来电铃声
     */
    private void stopPlaySound() {
        if (null != mCurrentMediaPlayer) {
            mCurrentMediaPlayer.stop();
            mCurrentMediaPlayer.reset();
            mCurrentMediaPlayer.release();
            mCurrentMediaPlayer = null;
        }
    }

    /**
     * 播放来电铃声
     */
    private void playSound() {
        if (mCurrentMediaPlayer == null)
            mCurrentMediaPlayer = MediaPlayer.create(CalledActivity.this, R.raw.bigbang_if_you);
        mCurrentMediaPlayer.start();
    }

    private void initView() {
        mMobile = (Mobile) getIntent().getSerializableExtra(Const.IntentKeyConst.KEY_MOBILE_MSG);
        mPhonenumber = getIntent().getStringExtra(Const.IntentKeyConst.KEY_PHONE_NUMBER);
        CLog.v("mPhonenumber " + mPhonenumber);
        mBgImg = (ImageView) findViewById(R.id.img_bg);
        TextView nameTextView = (TextView) findViewById(R.id.tv_name);
        TextView msgTextView = (TextView) findViewById(R.id.tv_msg);
        final TextView mTv = (TextView) findViewById(R.id.tv);
        mHead = (RoundImageView) findViewById(R.id.rimg_head);
        Button cancelBtn = (Button) findViewById(R.id.btn_cancel);
        Button answerBtn = (Button) findViewById(R.id.btn_answer);

        answerBtn.setOnClickListener(mOnclickListener);
        cancelBtn.setOnClickListener(mOnclickListener);

        nameTextView.setText(mMobile.nickname);
        msgTextView.setText(mMobile.nickname + getString(R.string.invite_video_call));

        getFaceImageAndDisplay();

        mBgImg.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mBgImg.buildDrawingCache();
                Bitmap bm = mBgImg.getDrawingCache();
                blur(bm, mTv);
                return true;
            }
        });
    }

    /**
     * 获取头像地址
     *
     * @return
     */
    private void getFaceImageAndDisplay() {
        mSearchCallContactMgmt.SearchCallContact(
                getApplicationContext(), mGetFaceImageCallback, mPhonenumber);
    }

    /**
     * 模糊图片
     *
     * @param bkg
     * @param view
     */
    private void blur(Bitmap bkg, View view) {
        float scaleFactor = 8;
        float radius = 2;
        if (view.getMeasuredWidth() / scaleFactor <= 0
                || view.getMeasuredHeight() / scaleFactor <= 0) {
            return;
        }
        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);
        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
    }

    /**
     * 缓存历史记录到本地数据库
     *
     * @param mobileMessage
     * @param face_image
     */
    private void saveCallHistory(Mobile mobileMessage, String face_image) {
        LocalCall localCall = new LocalCall(mobileMessage.nickname, face_image, mobileMessage.phoneNumber
                , System.currentTimeMillis() / 1000, LocalUserWrapper.getInstance().getLocalUser().getUid()
                , mobileMessage.uid, LocalCall.DIAL_ON);
        boolean flg;
        if (LocalCallListWrapper.getInstance().LocalCallIsExit(mobileMessage.uid)) {
            flg = LocalCallListWrapper.getInstance().updateLocalCall(localCall);
        } else {
            flg = LocalCallListWrapper.getInstance().addLocalCall(localCall);
        }
        CLog.v("s_contact-add-update--" + flg);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode != KeyEvent.KEYCODE_BACK && super.onKeyDown(keyCode, event);
    }

}
