package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalCallListWrapper;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalCall;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.CallContactResponse.CallContact;
import com.lingyang.camera.entity.LoginToken;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.LoginWithPhoneMgmt;
import com.lingyang.camera.mgmt.SearchCallContactMgmt;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.ui.adapter.ContactAdapter;
import com.lingyang.camera.ui.adapter.ContactsAndOnlineUsersAdapter;
import com.lingyang.camera.ui.adapter.SearchContactAdapter;
import com.lingyang.camera.ui.adapter.SearchContactAdapter.OnDialOutListener;
import com.lingyang.camera.ui.fragment.ContactsFragment;
import com.lingyang.camera.ui.fragment.OnlineUsersFragment;
import com.lingyang.camera.ui.widget.CustomEditText;
import com.lingyang.camera.ui.widget.CustomViewPager;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：ContactsActivity
 * 描述：该类负责查询用户，保存呼叫记录
 * 创建人：杜舒
 * 时间：2015/10
 */
public class ContactsActivity extends AppBaseActivity implements View.OnClickListener {

    private static final int RESULT_CODE = 102;
    private CustomEditText mEt;
    private Context mContext;
    //    private PullToRefreshListView mlvContacts;
    OnDialOutListener mOnDialOutListener = new OnDialOutListener() {
        @Override
        public void dialOut(CallContact call) {
            //缓存到本地数据库
            if (!call.name.equals(LocalUserWrapper.getInstance().getLocalUser().getUid())) {
                LocalCall localCall = new LocalCall(call.nickname, call.face_image, call.phonenumber,
                        System.currentTimeMillis() / 1000,
                        LocalUserWrapper.getInstance().getLocalUser().getUid(),
                        call.name, LocalCall.DIAL_OUT);
                boolean flg;
                if (LocalCallListWrapper.getInstance().LocalCallIsExit(call.name)) {
                    flg = LocalCallListWrapper.getInstance().updateLocalCall(localCall);
                } else {
                    flg = LocalCallListWrapper.getInstance().addLocalCall(localCall);
                }
                CLog.v("contact-add-update--" + flg);
                refreshContacts();
            }
            mCallContact = call;
            checkNetWorkAvailable();
            checkCloudIsOnline();
        }
    };
    private CallContact mCallContact;

    private void checkNetWorkAvailable() {
        if (!NetWorkUtils.isNetworkAvailable(mContext)) {
            showToast(getString(R.string.app_network_error));
            return;
        }
    }

    private CustomViewPager mVpContact;
    private TextView mTvContactHead;
    private ListView mLvSearch;
    private SearchCallContactMgmt mSearchCallContactMgmt;
    private ContactAdapter mContactAdapter;
    private SearchContactAdapter mSearchAdapter;
    BaseCallBack<List<CallContact>> mCallBack = new BaseCallBack<List<CallContact>>() {
        @Override
        public void error(ResponseError object) {
            if (object != null) {
                showToast(object.error_code + object.error_msg);
            }
        }

        @Override
        public void success(final List<CallContact> callContacts) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (callContacts.size() > 0) {
                        if (!mEt.getText().toString().equals("")) {
                            mSearchAdapter.setData(callContacts);
                            setVisible(true);
                        }
                    } else {
                        showToast("未搜索到相关用户");
                        mVpContact.setVisibility(View.GONE);
                        mLvSearch.setVisibility(View.GONE);
                    }
                }
            });

        }
    };
    private List<LocalCall> mContactList = new ArrayList<LocalCall>();
    private boolean mAddCallback;
    private boolean mSurfaceDestroyed;
    private SurfaceView mSvLocal;
    private TextView mTvPrompt;
    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            CLog.v("onPageSelected " + position);
            if (position == 0) {
                mTvContactHead.setText(getText(R.string.call_history));
            } else {
                mTvContactHead.setText(getText(R.string.online_users));
            }
            if (mTvPrompt.getVisibility() != View.GONE) {
                mTvPrompt.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mTvPrompt.getVisibility() != View.GONE) {
                mTvPrompt.setVisibility(View.GONE);
            }
            if (!s.toString().equals("")) {
                mSearchCallContactMgmt.SearchCallContact(ContactsActivity.this,
                        mCallBack, s.toString());
            } else {
                setVisible(false);
            }
        }
    };
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            mSurfaceDestroyed = false;
            startLocalPreview(Camera.CameraInfo.CAMERA_FACING_FRONT);
            CLog.v("surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            CLog.v("surfaceChanged" + width + "  " + height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            CLog.v("surfaceDestroyed");
            mSurfaceDestroyed = true;
            closeCamera();
        }
    };
    private String mPhoneNumber;
    private String mPassword;
    private LoginWithPhoneMgmt mLoginMgmt;

    private void refreshContacts() {
        Intent intent = new Intent();
        intent.setAction(Const.Actions.ACTION_REFRESH_CONTACTS_BROADCAST);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
//    private BroadcastReceiver mRefreshContactsListener = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            refreshContacts();
//        }
//    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel:
                setResult(RESULT_CODE);
                onBackPressed();
                break;
            case R.id.ib_call:
                String str = mEt.getText().toString();
                if (str.equals("")) {
                    showToast("请输入昵称或手机号");
                    return;
                }
                call(str, str);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_contacts);
        mContext = ContactsActivity.this;
        mSearchCallContactMgmt = (SearchCallContactMgmt) MgmtClassFactory.
                getInstance().getMgmtClass(SearchCallContactMgmt.class);
        mLoginMgmt = (LoginWithPhoneMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(LoginWithPhoneMgmt.class);
//        IntentFilter filter= new IntentFilter();
//        filter.addAction(Const.Actions.ACTION_REFRESH_CONTACTS_BROADCAST);
//        LocalBroadcastManager.getInstance(mContext).registerReceiver(mRefreshContactsListener,filter);
        initView();
        initData();
    }


    @Override
    protected void onDestroy() {
//        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mRefreshContactsListener);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        closeCamera();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!mAddCallback || mSurfaceDestroyed) {
            mSurfaceHolder.addCallback(mCallback);
            mAddCallback = true;
        } else {
            startLocalPreview(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        super.onResume();
    }

    /**
     * 开启本地预览
     *
     * @param i
     */
    private void startLocalPreview(final int i) {
        ThreadPoolManagerQuick.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    Camera.Parameters mParameters = mCamera.getParameters();
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
                    List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes();
                    Camera.Size preSize = null;
                    for (Camera.Size size : sizes) {
                        if ((size.width + 0f) / size.height == 4f / 3 && size.width >= 400) {
                            preSize = size;
                            break;
                        }
                    }
                    assert preSize != null;
                    mParameters.setPreviewSize(preSize.width, preSize.height);
                    mCamera.setParameters(mParameters);
                    mCamera.startPreview();//开始预览
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(getString(R.string.check_camera_permission));
                    mCamera = null;
                } finally {
                    if (mCamera == null) {
                        finish();
                    }
                }
            }
        });
    }

    private void closeCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void initView() {
        ImageView mIvCancel = (ImageView) findViewById(R.id.img_cancel);
        mSvLocal = (SurfaceView) findViewById(R.id.sv_local);
        mSurfaceHolder = mSvLocal.getHolder();
        mLvSearch = (ListView) findViewById(R.id.lv_search);
        mEt = (CustomEditText) findViewById(R.id.et_nickname_phonenumber);
        ImageButton callBtn = (ImageButton) findViewById(R.id.ib_call);
        mVpContact = (CustomViewPager) findViewById(R.id.vp_contacts);
        mTvPrompt = (TextView) findViewById(R.id.tv_prompt);
        mTvContactHead = (TextView) findViewById(R.id.tv_contact_head);
        mVpContact.setOnPageChangeListener(mPageChangeListener);
        mTvPrompt.setText(getString(R.string.video_call_support));
        setVisible(false);
        mIvCancel.setOnClickListener(this);
        callBtn.setOnClickListener(this);
        mEt.addTextChangedListener(textWatcher);
        //获取屏幕宽靠 设置surfaceView的位置保证预览画面不变形
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mSvLocal.getLayoutParams();
        int heightPixels = mContext.getResources().getDisplayMetrics().heightPixels;
        layoutParams.width = heightPixels * 3 / 4;
        layoutParams.height = heightPixels;
        int screenWidth = Utils.getScreenWidth(mContext);
        //裁剪左右
        layoutParams.setMargins(-(heightPixels * 3 / 4 - screenWidth) / 2, 0, 0, 0);
        mSvLocal.setLayoutParams(layoutParams);
    }

    private void initData() {

        mSearchAdapter = new SearchContactAdapter(this);
        mSearchAdapter.setOnDialOutListener(mOnDialOutListener);
        mLvSearch.setAdapter(mSearchAdapter);

        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new ContactsFragment());
        fragments.add(new OnlineUsersFragment());
        ContactsAndOnlineUsersAdapter adapter = new ContactsAndOnlineUsersAdapter(getSupportFragmentManager(), fragments);
        mVpContact.setAdapter(adapter);
        mVpContact.setOffscreenPageLimit(3);
        mVpContact.setCurrentItem(0);
        mVpContact.setScrollable(true);
    }

    private void checkCloudIsOnline(){
        if (LYService.getInstance().isOnline()) {
            loginGetPhoneConnectAddr();
        }else {
            LYService.getInstance().stopCloudService();
            showToast("云平台离线，重新登录中...");
            mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
            LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
            if (localUser!=null) {
                LYService.getInstance().startCloudService(
                        localUser.getUserToken(),
                        localUser.getInitString(),
                        new CallBackListener<Long>() {
                            @Override
                            public void onSuccess(Long aLong) {
                                mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
                                loginGetPhoneConnectAddr();
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

    /**
     * 呼叫
     *
     * @param name
     * @param tel
     */
    public void call(final String name, final String tel) {
        if (tel.equals(LocalUserWrapper.getInstance().getLocalUser().getMobile())
                || name.equals(LocalUserWrapper.getInstance().getLocalUser().getNickName())) {
            showToast(getString(R.string.can_not_call_myself));
            return;
        }
        Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_MOBILE_INTERCONNECT);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE, Const.Actions.ACTION_ACTIVITY_CONTACTS);
        intent.putExtra(Const.IntentKeyConst.KEY_PHONE_NUMBER, tel);
        intent.putExtra(Const.IntentKeyConst.KEY_NICKNAME, name);
        ActivityUtil.startActivity(ContactsActivity.this, intent);
    }
    private BaseCallBack<LoginToken.UserToken> mLoginCallBack = new BaseCallBack<LoginToken.UserToken>() {
        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object != null)
                showToast(object.error_msg);
            else {
                showToast("互联地址获取失败！");
            }
        }

        @Override
        public void success(LoginToken.UserToken t) {
            CLog.d("login success");
            LocalUser localUser = LocalUserWrapper.getInstance().exsitUserByPhone(mPhoneNumber, mPassword);
            if (localUser != null) {
                localUser.setAccessToken(t.access_token);
                localUser.setExpire(t.expire);
                localUser.setLastLoginTime(System.currentTimeMillis());
                localUser.setIP(NetWorkUtils.getHostIp());
                localUser.setHead(t.faceimage);
                localUser.setNickName(t.nickname);
                localUser.setUId(t.uname);
                localUser.setMobile(t.phonenumber);
                localUser.setControl(t.control);
                localUser.setInitString(t.init_string);
                localUser.setPhoneConnectAddr(t.phone_connect_addr);
                localUser.setUserToken(t.user_token);
                localUser.setUserTokenExpire(t.user_token_expire);

                Boolean result = LocalUserWrapper.getInstance().updateUser(localUser);
                CLog.v("mLoginCallBack updateUser result:" + result);
                LocalUserWrapper.getInstance().setLocalUser(localUser);
            }
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            call(mCallContact.nickname, mCallContact.phonenumber);
        }
    };
    private void loginGetPhoneConnectAddr(){
        mPhoneNumber = MyPreference.getInstance().getString(
                getApplicationContext(), MyPreference.LOGIN_PHONE, "");
        mPassword = MyPreference.getInstance().getString(
                getApplicationContext(), MyPreference.LOGIN_PASSWORD, "");
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        mLoginMgmt.loginWithPhone(getApplicationContext(), mPhoneNumber, mPassword, mLoginCallBack);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    /**
     * 设置搜索结果和联系人是否可见
     *
     * @param isSearch 是否是搜索
     */
    public void setVisible(boolean isSearch) {
        if (isSearch) {
            mVpContact.setVisibility(View.GONE);
            mLvSearch.setVisibility(View.VISIBLE);
        } else {
            mVpContact.setVisibility(View.VISIBLE);
            mLvSearch.setVisibility(View.GONE);
        }
    }
}
