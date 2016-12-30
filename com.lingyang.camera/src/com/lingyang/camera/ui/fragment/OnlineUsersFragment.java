package com.lingyang.camera.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.NetWorkUtils;
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
import com.lingyang.camera.mgmt.GetOnlineUsersMgmt;
import com.lingyang.camera.mgmt.LoginWithPhoneMgmt;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.ui.activity.AppBaseActivity;
import com.lingyang.camera.ui.activity.ContactsActivity;
import com.lingyang.camera.ui.adapter.OnlineUsersAdapter;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件名: ContactsFragment
 * 描    述: [该类的简要描述]
 * 创建人: dushu
 * 创建时间: 2016/3/3
 */
public class OnlineUsersFragment extends Fragment {

    private OnlineUsersAdapter mOnlineUsersAdapter;
    private List<CallContactResponse.CallContact> mOnlineUsersList = new ArrayList<CallContactResponse.CallContact>();
    private GetOnlineUsersMgmt mMgmtClass;
    private PullToRefreshListView mLvOnlineUsers;
    private String mNickname;
    private String mCallMobile;
    AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CLog.v("position " + position);
            //缓存到本地数据库
            CallContactResponse.CallContact call = mOnlineUsersList.get(position - 1);
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
            }
            Intent intent = new Intent();
            intent.setAction(Const.Actions.ACTION_REFRESH_CONTACTS_BROADCAST);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

            mNickname = call.nickname;
            mCallMobile = call.phonenumber;
            // 呼叫前判断云平台在线，并获取最新互联地址
            checkNetWorkAvailable();
            checkCloudIsOnline();
//            call(call.nickname, call.phonenumber);
        }
    };
    private LoginWithPhoneMgmt mLoginMgmt;
    private String mPhoneNumber;
    private String mPassword;

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
    private BaseCallBack<List<CallContactResponse.CallContact>> mGetOnlineUsersCallback =
            new BaseCallBack<List<CallContactResponse.CallContact>>() {
                @Override
                public void error(ResponseError object) {
                    if (object != null) {
                        ((ContactsActivity) getActivity()).showToast(object.error_code + object.error_msg);
                    } else {
                        if (isAdded()) {
                            ((ContactsActivity) getActivity()).showToast(getString(R.string.get_online_users_fail));
                        }
                    }
                }

                @Override
                public void success(final List<CallContactResponse.CallContact> data) {
                    ContactsActivity activity = (ContactsActivity) getActivity();
                    if (activity!=null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLvOnlineUsers.onRefreshComplete();
                                if (data != null) {
                                    mOnlineUsersList.clear();
                                    mOnlineUsersList.addAll(data);
                                }
                                mOnlineUsersAdapter.setData(mOnlineUsersList);
                                mLvOnlineUsers.setAdapter(mOnlineUsersAdapter);

                                Intent intent = new Intent();
                                intent.setAction(Const.Actions.ACTION_REFRESH_CONTACTS_BROADCAST);
                                intent.putExtra(Const.IntentKeyConst.KEY_ONLINE_USER_LIST, (Serializable) mOnlineUsersList);
                                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                            }
                        });
                    }
                }
            };
    private PullToRefreshBase.OnRefreshListener<ListView> mRefreshListener
            = new PullToRefreshBase.OnRefreshListener<ListView>() {
        @Override
        public void onRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
            refreshOnlineUsers();
        }
    };

    private void refreshOnlineUsers(){
        mMgmtClass.getOnlineUsers(getActivity(), mGetOnlineUsersCallback);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMgmtClass = (GetOnlineUsersMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(GetOnlineUsersMgmt.class);
        View view = inflater.inflate(R.layout.fragment_onlineusers, container, false);
        mLvOnlineUsers = (PullToRefreshListView) view.findViewById(R.id.lv_onlineusers);
        mLvOnlineUsers.setOnItemClickListener(mOnItemClickListener);
        mLvOnlineUsers.setOnRefreshListener(mRefreshListener);
        mLoginMgmt = (LoginWithPhoneMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(LoginWithPhoneMgmt.class);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOnlineUsersAdapter = new OnlineUsersAdapter(getActivity());
        mOnlineUsersAdapter.setData(mOnlineUsersList);
        mLvOnlineUsers.setAdapter(mOnlineUsersAdapter);
        refreshOnlineUsers();
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
