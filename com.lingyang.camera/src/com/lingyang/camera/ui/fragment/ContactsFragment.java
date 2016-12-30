package com.lingyang.camera.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.base.utils.ThreadPoolManagerNormal;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalCallListWrapper;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalCall;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.CallContactResponse;
import com.lingyang.camera.entity.LoginToken;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.LoginWithPhoneMgmt;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.ui.activity.AppBaseActivity;
import com.lingyang.camera.ui.activity.ContactsActivity;
import com.lingyang.camera.ui.adapter.ContactAdapter;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名: ContactsFragment
 * 描    述: [该类的简要描述]
 * 创建人: dushu
 * 创建时间: 2016/3/3
 */
public class ContactsFragment extends Fragment {
    private List<LocalCall> mContactList = new ArrayList<LocalCall>();
    private ContactAdapter mContactAdapter;
    private ListView mLvContacts;
    AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CLog.v("position " + position);
            LocalCall localCall = mContactList.get(position);
            localCall.setmTime(System.currentTimeMillis() / 1000);
            LocalCallListWrapper.getInstance().updateLocalCall(localCall);
            ThreadPoolManagerNormal.execute(new Runnable() {
                @Override
                public void run() {
                    final List<LocalCall> list = LocalCallListWrapper.getInstance().getList(LocalUserWrapper.
                            getInstance().getLocalUser().getUid());
                    mContactList.clear();
                    mContactList.addAll(list);
                    for (LocalCall localCall1 : mContactList) {
                        for (CallContactResponse.CallContact tempOnlineUser : mTempOnlineUsers) {
                            if (localCall1.getmMobile().equals(tempOnlineUser.phonenumber)) {
                                localCall1.setIsOnline(true);
                            }
                        }
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mContactAdapter.setData(mContactList);
                        }
                    });
                }
            });
            mNickname = localCall.getmNickName();
            mCallMobile = localCall.getmMobile();
            // 呼叫前判断云平台在线，并获取最新互联地址
            checkNetWorkAvailable();
            checkCloudIsOnline();

//            call(localCall.getmNickName(), localCall.getmMobile());
        }
    };
    private String mPhoneNumber;
    private String mPassword;
    private LoginWithPhoneMgmt mLoginMgmt;
    private String mNickname;
    private String mCallMobile;

    private void checkNetWorkAvailable() {
        if (!NetWorkUtils.isNetworkAvailable(getActivity())) {
            ((AppBaseActivity)getActivity()).showToast(getString(R.string.app_network_error));
            return;
        }
    }
    private void checkCloudIsOnline(){
        if (LYService.getInstance().isOnline()) {
            loginGetPhoneConnectAddr();
        }else {
            LYService.getInstance().stopCloudService();
            ((AppBaseActivity)getActivity()).showToast("云平台离线，重新登录中...");
            ((AppBaseActivity)getActivity()).mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
            LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
            if (localUser!=null) {
                LYService.getInstance().startCloudService(
                        localUser.getUserToken(),
                        localUser.getInitString(),
                        new CallBackListener<Long>() {
                            @Override
                            public void onSuccess(Long aLong) {
                                ((AppBaseActivity)getActivity()).mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
                                loginGetPhoneConnectAddr();
                            }

                            @Override
                            public void onError(LYException e) {
                                ((AppBaseActivity)getActivity()).mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                                ((AppBaseActivity)getActivity()).showToast(getString(R.string.cloud_login_fail));
                            }
                        });
            }
        }
    }
    private void loginGetPhoneConnectAddr(){
        mPhoneNumber = MyPreference.getInstance().getString(
                getActivity(), MyPreference.LOGIN_PHONE, "");
        mPassword = MyPreference.getInstance().getString(
                getActivity(), MyPreference.LOGIN_PASSWORD, "");
        ((AppBaseActivity)getActivity()).mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        mLoginMgmt.loginWithPhone(getActivity(), mPhoneNumber, mPassword, mLoginCallBack);
    }
    private BaseCallBack<LoginToken.UserToken> mLoginCallBack = new BaseCallBack<LoginToken.UserToken>() {
        @Override
        public void error(ResponseError object) {
            ((AppBaseActivity)getActivity()).mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object != null)
                ((AppBaseActivity)getActivity()).showToast(object.error_msg);
            else {
                ((AppBaseActivity)getActivity()).showToast("互联地址获取失败！");
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
            ((AppBaseActivity)getActivity()).mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            call(mNickname, mCallMobile);
        }
    };


    List<CallContactResponse.CallContact> mTempOnlineUsers = new ArrayList<CallContactResponse.CallContact>();
    private BroadcastReceiver mRefreshContactsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final List<CallContactResponse.CallContact> onlineUsers = (List<CallContactResponse.CallContact>)
                    intent.getSerializableExtra(Const.IntentKeyConst.KEY_ONLINE_USER_LIST);
            if (onlineUsers != null && mContactList != null) {
                //更新通话记录联系人在线状态
                mTempOnlineUsers = onlineUsers;
                for (LocalCall localCall : mContactList) {
                    localCall.setIsOnline(false);
                    for (CallContactResponse.CallContact onlineUser : onlineUsers) {
                        if (localCall.getmMobile().equals(onlineUser.phonenumber)) {
                            localCall.setIsOnline(true);
                            CLog.v(localCall.getmNickName() + " isOnline1" + localCall.isOnline());
                            LocalCallListWrapper.getInstance().updateLocalCall(localCall);
                        }
                        if (!mContactList.contains(localCall)) {
                            mContactList.add(localCall);
                        }
                    }
                }
                mContactAdapter.setData(mContactList);
            } else {
                //来电，在线用户列表呼叫，搜索用户列表呼叫，刷新通话记录`界面在线状态
                CLog.v("======================");
                ThreadPoolManagerNormal.execute(new Runnable() {
                    @Override
                    public void run() {
                        final List<LocalCall> list = LocalCallListWrapper.getInstance().getList(LocalUserWrapper.
                                getInstance().getLocalUser().getUid());
                        mContactList.clear();
                        mContactList.addAll(list);
                        for (LocalCall localCall : mContactList) {
                            for (CallContactResponse.CallContact callContact2 : mTempOnlineUsers) {
                                if (localCall.getmMobile().equals(callContact2.phonenumber)) {
                                    localCall.setIsOnline(true);
                                    CLog.v(localCall.getmNickName() + " isOnline1" + localCall.isOnline());
                                    LocalCallListWrapper.getInstance().updateLocalCall(localCall);
                                }
                                if (!mContactList.contains(localCall)) {
                                    mContactList.add(localCall);
                                }
                            }
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mContactAdapter.setData(mContactList);
                            }
                        });
                    }
                });
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        CLog.v("onCreateView");
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        mLoginMgmt = (LoginWithPhoneMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(LoginWithPhoneMgmt.class);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CLog.v("onViewCreated");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.Actions.ACTION_REFRESH_CONTACTS_BROADCAST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRefreshContactsReceiver, filter);
        initData();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRefreshContactsReceiver);
        super.onDestroy();
    }

    private void initData() {
        mContactAdapter = new ContactAdapter(getActivity());
        mContactList.addAll(LocalCallListWrapper.getInstance().getList(LocalUserWrapper.
                getInstance().getLocalUser().getUid()));
        for (LocalCall localCall : mContactList) {
            CLog.d(localCall.toString());
        }
        mContactAdapter.setData(mContactList);
        mLvContacts.setAdapter(mContactAdapter);
    }

    private void initView(View view) {
        mLvContacts = (ListView) view.findViewById(R.id.lv_contacts);
        mLvContacts.setOnItemClickListener(mOnItemClickListener);
        CLog.v("mOnItemClickListener");
    }

    /**
     * 呼叫
     *
     * @param name
     * @param tel
     */
    public void call(final String name, final String tel) {
        if (!NetWorkUtils.isNetworkAvailable(getActivity())) {
            ((ContactsActivity) getActivity()).showToast(getString(R.string.app_network_error));
            return;
        }
        if (tel.equals(LocalUserWrapper.getInstance().getLocalUser().getMobile())
                || name.equals(LocalUserWrapper.getInstance().getLocalUser().getNickName())) {
            ((ContactsActivity) getActivity()).showToast(getString(R.string.can_not_call_myself));
            return;
        }
        if (LYService.getInstance().isOnline()) {
            gotoMobileInterconnect(name, tel);
        }else {
            ((AppBaseActivity)getActivity()).showToast("云平台离线，重新登录中...");
            ((AppBaseActivity)getActivity()).mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
            LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
            if (localUser!=null) {
                LYService.getInstance().startCloudService(
                        localUser.getUserToken(),
                        localUser.getInitString(),
                        new CallBackListener<Long>() {
                            @Override
                            public void onSuccess(Long aLong) {
                                ((AppBaseActivity)getActivity()).mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
                                gotoMobileInterconnect(name, tel);
                            }

                            @Override
                            public void onError(LYException e) {
                                ((AppBaseActivity)getActivity()).mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                                ((AppBaseActivity)getActivity()).showToast(getString(R.string.cloud_login_fail));
                            }
                        });
            }
        }
    }

    private void gotoMobileInterconnect(String name, String tel) {
        Intent intent = new Intent(Const.Actions.ACTION_ACTIVITY_MOBILE_INTERCONNECT);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE, Const.Actions.ACTION_ACTIVITY_CONTACTS);
        intent.putExtra(Const.IntentKeyConst.KEY_PHONE_NUMBER, tel);
        intent.putExtra(Const.IntentKeyConst.KEY_NICKNAME, name);
        ActivityUtil.startActivity(getActivity(), intent);
    }
}
