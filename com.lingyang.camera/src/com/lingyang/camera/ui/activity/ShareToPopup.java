package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.entity.ShareUserResponse;
import com.lingyang.camera.entity.UnShareCamera;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.MessageSendMgmt;
import com.lingyang.camera.mgmt.ShareCameraToOthersMgmt;
import com.lingyang.camera.mgmt.ShareUserMgmt;
import com.lingyang.camera.mgmt.UnShareCameraToOthersMgmt;
import com.lingyang.camera.ui.adapter.ShareUserAdapter;
import com.lingyang.camera.ui.fragment.AttentionFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名: ShareToPopup
 * 描    述:用于展示分享的用户，添加分享，删除分享。
 * 创建人:
 * 创建时间: 2015/10
 */
public class ShareToPopup extends PopupWindow {

    ShareUserAdapter mAdapter;
    List<ShareUserResponse.ShareUser> mShareUserList;
    String mCid;
    int mShared;
    AttentionFragment.ShareResultCallback mShareResultCallback;
    AppBaseActivity mAppBaseActivity;
    private Button mShareButton;
    private EditText mNickNameEditText;
    private Context mContext;
    private TextView mTitleTextView;
    ShareUserAdapter.RemoveOnCallBackListener onRemoveListener =
            new ShareUserAdapter.RemoveOnCallBackListener() {
                @Override
                public void remove(final ShareUserResponse.ShareUser shareUser) {
                    UnShareCameraToOthersMgmt unShareCameraToOthersMgmt =
                            (UnShareCameraToOthersMgmt) MgmtClassFactory.getInstance()
                                    .getMgmtClass(UnShareCameraToOthersMgmt.class);
                    unShareCameraToOthersMgmt.unShareCamaraToOther(mContext, mCid, shareUser.nickname, new BaseCallBack() {
                        @Override
                        public void error(ResponseError object) {
                            if (object == null) {
                                mAppBaseActivity.showToast(mAppBaseActivity.getString(R.string.share_cancel_fail));
                            } else {
                                mAppBaseActivity.showToast(object.error_code + "_" + object.error_msg);
                            }
                        }

                        @Override
                        public void success(Object o) {
                            mAppBaseActivity.showToast(mAppBaseActivity.getString(R.string.share_cancel_success));
                            mShared--;
                            sendMessage(shareUser.phonenumber,Const.IntentKeyConst.KEY_DELETE_CAMERA);
                            mAppBaseActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTitleTextView.setText(String.format(mAppBaseActivity
                                            .getString(R.string.has_shared_to_n_persons), mShared));
                                    mAdapter.removeShareUser(shareUser.nickname);
                                }
                            });

                        }
                    });
                }
            };

    OnClickListener onShareClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String nickName = mNickNameEditText.getText().toString().trim();
            List<ShareUserResponse.ShareUser> list = mAdapter.getShareUsersList();
            for (ShareUserResponse.ShareUser shareUser : list) {
                if (shareUser.nickname.equals(nickName)) {
                    mAppBaseActivity.showToast(mAppBaseActivity.getString(R.string.already_shared));
                    return;
                }
            }
            ShareCameraToOthersMgmt shareCameraToOthersMgmt =
                    (ShareCameraToOthersMgmt) MgmtClassFactory.getInstance()
                            .getMgmtClass(ShareCameraToOthersMgmt.class);

            shareCameraToOthersMgmt.shareCamaraToOther(mContext, mCid, nickName,
                    new BaseCallBack<ShareUserResponse.ShareUser>() {
                        @Override
                        public void error(ResponseError object) {
                            if (object == null) {
                                mAppBaseActivity.showToast(mAppBaseActivity.getString(R.string.share_fail));
                            } else {
                                mAppBaseActivity.showToast(object.error_code + "_" + object.error_msg);
                            }
                        }

                        @Override
                        public void success(final ShareUserResponse.ShareUser shareUser) {
                            mAppBaseActivity.showToast(mAppBaseActivity.getString(R.string.share_success));
                            mShared++;
                            sendMessage(shareUser.phonenumber,Const.IntentKeyConst.KEY_SHARE_CAMERA);
                            mAppBaseActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTitleTextView.setText(String.format(mAppBaseActivity.
                                            getString(R.string.has_shared_to_n_persons), mShared));
                                    mNickNameEditText.setText("");
                                    mAdapter.addShareUser(shareUser);
                                }
                            });

                        }
                    });
        }
    };
    private MessageSendMgmt mSendMessageMgmt;
    private String mCname;

    private void sendMessage(String phoneNumber,String message) {
        UnShareCamera unShareCamera = new UnShareCamera();
        unShareCamera.cid = mCid;
        unShareCamera.cname = mCname;
        LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
        if (localUser!=null) {
            unShareCamera.nickname = localUser.getNickName();
        }
        unShareCamera.message = message;
        mSendMessageMgmt.sendMessage(mContext, unShareCamera.toString(),
                phoneNumber, new BaseCallBack<String>() {
            @Override
            public void error(ResponseError object) {
                CLog.d("---sendUnShareMessage error!");
            }

            @Override
            public void success(String s) {
                CLog.d("---sendUnShareMessage success!");
            }
        });

    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_root_view:
                    CLog.v("rl_root_view");
                    dismiss();
                    break;
                case R.id.ll_center:
                    CLog.v("ll_center");
                    break;
                case R.id.iv_share_pop_close:
                    if (mShareResultCallback != null)
                        mShareResultCallback.onShare(mShared);
                    dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    public ShareToPopup(Context context, String cid,String cname, int shared) {
        // 设置布局的参数
        this(context, context.getResources()
                .getDisplayMetrics().widthPixels * 3 / 4, context.getResources()
                .getDisplayMetrics().heightPixels / 2);
        mSendMessageMgmt = (MessageSendMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(MessageSendMgmt.class);
        mCid = cid;
        mCname = cname;
        mShared = shared;
        mShareUserList = new ArrayList<ShareUserResponse.ShareUser>();
        initView();
        initShareList();
    }

    private ShareToPopup(Context context, int width, int height) {
        this.mContext = context;
        // 设置可以获得焦点
        this.setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);
        // 设置弹窗的宽度和高度
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        // 设置弹窗的布局界面
        setContentView(LayoutInflater.from(mContext).inflate(R.layout.popup_shareto, null));
    }

    private void initView() {
        mAdapter = new ShareUserAdapter(mContext, mShareUserList);
        mTitleTextView = (TextView) getContentView().findViewById(R.id.tv_shareto_title);
        mNickNameEditText = (EditText) getContentView().findViewById(R.id.et_share_nickname);
        mShareButton = (Button) getContentView().findViewById(R.id.btn_shareto);
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                CLog.v(v.getId()+event.getAction()+"");
                return false;
            }
        });
        RelativeLayout rootView = (RelativeLayout) getContentView().findViewById(R.id.rl_root_view);
        LinearLayout llCenter = (LinearLayout) getContentView().findViewById(R.id.ll_center);
        rootView.setOnClickListener(mOnClickListener);
        llCenter.setOnClickListener(mOnClickListener);
        ImageView closeImageView = (ImageView) getContentView().findViewById(R.id.iv_share_pop_close);
        mShareButton.setEnabled(false);
        mTitleTextView.setText(String.format("已分享给%s位家人和朋友", mShared));
        mAppBaseActivity = ((AppBaseActivity) mContext);
        mNickNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mShareButton.setEnabled(true);
                } else {
                    mShareButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        closeImageView.setOnClickListener(mOnClickListener);
        mShareButton.setOnClickListener(onShareClickListener);
        mAdapter.setShareUserCallBackListener(onRemoveListener);
        ListView listView = (ListView) getContentView().findViewById(R.id.list_shareuser);
        listView.setAdapter(mAdapter);
    }

    private void initShareList() {
        ShareUserMgmt shareUserMgmt = (ShareUserMgmt) MgmtClassFactory.getInstance().getMgmtClass(ShareUserMgmt.class);
        shareUserMgmt.getShareUserList(mContext, mCid, new BaseCallBack<List<ShareUserResponse.ShareUser>>() {
            @Override
            public void error(ResponseError object) {
                mAppBaseActivity.showToast(mAppBaseActivity.getString(R.string.get_shared_list_fail));
            }

            @Override
            public void success(List<ShareUserResponse.ShareUser> shareUsers) {
                mAdapter.refresh(shareUsers);
                mShared = shareUsers.size();
                mAppBaseActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTitleTextView.setText(String.format(mAppBaseActivity.
                                getString(R.string.has_shared_to_n_persons), mShared));
                    }
                });
            }
        });

    }

    public void setOnResultCallback(AttentionFragment.ShareResultCallback callback) {
        mShareResultCallback = callback;
    }

}