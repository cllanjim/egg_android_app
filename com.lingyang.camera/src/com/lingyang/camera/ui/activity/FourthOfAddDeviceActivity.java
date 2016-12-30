package com.lingyang.camera.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.lingyang.base.utils.NetWorkUtils;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.dialog.DialogAddDeviceBackNotify;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.BindCameraEntity.BindEntity;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.BindCameraMgmt;
import com.lingyang.camera.mgmt.SetCameraToPublicMgmt;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;

/**
 * 文件名：FourthOfAddDeviceActivity
 * 描述：此类是将摄像机sn传给后台给摄像机设置名称并绑定，和是否设为公众
 * 创建人：廖蕾
 * 时间：2015/9/19
 */
public class FourthOfAddDeviceActivity extends AppBaseActivity {
    DialogInterface.OnClickListener mListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    ActivityUtil.startActivity(FourthOfAddDeviceActivity.this,
                            Const.Actions.ACTION_ACTIVITY_MAIN);
                    dialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    };
    BaseCallBack<BaseResponse> mPublicCallBack = new BaseCallBack<BaseResponse>() {
        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object != null) {
                showToast(object.error_msg);
            } else {
                showToast((String) getText(R.string.set_public_fail));
            }
            sendBroadcast();
            goToMain();
        }

        @Override
        public void success(BaseResponse t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            showToast((String) getText(R.string.set_public_suc));
            mIntent.setAction(Const.Actions.ACTION_IS_PUBLIC_REFRESH);
            sendBroadcast();
            goToMain();
        }
    };
    private String mSn;
    private EditText mNameEdt;
    private CheckBox mToPublicBox;
    private Button mCompleteBtn;
    private BindCameraMgmt mBindCameraMgmt;
    private SetCameraToPublicMgmt mPublicMgmt;
    private String mCId;
    private Intent mIntent;
    private BaseCallBack mBindCameraCallback = new BaseCallBack<BindEntity>() {
        @Override
        public void success(final BindEntity t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            runOnUiThread(new Runnable() {
                public void run() {
                    showToast((String) getText(R.string.bind_suc));
                    if (t != null) {
                        mCId = t.cid;
                    }
                    // 将摄像头设为公众摄像头
                    if (mToPublicBox.isChecked()) {
                        setCameraToPublic();
                    } else {
                        sendBroadcast();
                        goToMain();
                    }
                }
            });
        }

        @Override
        public void error(final ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            runOnUiThread(new Runnable() {
                public void run() {
                    mCompleteBtn.setEnabled(true);
                }
            });
            if (object == null) {
                showToast((String) getText(R.string.bind_fail));
            } else {
                if (object.error_code.equals("20043")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogAddDeviceBackNotify n = new DialogAddDeviceBackNotify(
                                    FourthOfAddDeviceActivity.this);
                            n.show();
                        }
                    });
                } else {
                    showToast(object.error_code + object.error_msg);
                }
            }
        }
    };
    private CallBackListener<Long> mCallbackListener = new CallBackListener<Long>() {
        @Override
        public void onSuccess(Long aLong) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showToast(getString(R.string.login_cloud_platform_success));
                    setBindCameraMgmt();
                }
            });
        }

        @Override
        public void onError(LYException e) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            showToast(getString(R.string.login_cloud_platform_fail));
            runOnUiThread(new Runnable() {
                public void run() {
                    mCompleteBtn.setEnabled(true);
                }
            });
        }
    };

    private void sendBroadcast(){
        mIntent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE,
                Const.IntentKeyConst.REFRESH_FROM_BIND);
        LocalBroadcastManager.getInstance(FourthOfAddDeviceActivity.this).sendBroadcast(
                mIntent);
    }

    // 完成
    OnClickListener mCompleteListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(mNameEdt.getText().toString().trim())) {
                showToast((String) getText(R.string.bind_input_camera_name));
                return;
            }
            bindCamera();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth_of_add_device);
        mSn = getIntent().getStringExtra(Const.IntentKeyConst.KEY_SN);
        initView();
        mBindCameraMgmt = (BindCameraMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(BindCameraMgmt.class);
        mPublicMgmt = (SetCameraToPublicMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(SetCameraToPublicMgmt.class);
        mIntent=new Intent(Const.Actions.ACTION_IS_ATTENTION_REFRESH);
    }

    private void initView() {
        mNameEdt = (EditText) findViewById(R.id.edt_name);
        mToPublicBox = (CheckBox) findViewById(R.id.checkbox_topublic);
        TextView titleTextView = (TextView) findViewById(R.id.tv_header_title);
        mCompleteBtn = (Button) findViewById(R.id.btn_complete);

        titleTextView.setText(getText(R.string.add_fourth_step));
        mCompleteBtn.setOnClickListener(mCompleteListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 创建退出对话框
            new AlertDialog.Builder(FourthOfAddDeviceActivity.this)
                    .setTitle(R.string.system_prompt).setMessage(R.string.confirm_unbind)
                    .setNegativeButton(R.string.cancel, mListener)
                    .setPositiveButton(R.string.confirm, mListener).create().show();
            // 设置对话框标题
        }
        return false;
    }

    /**
     * 绑定摄像机
     */
    private void bindCamera() {
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        if (!NetWorkUtils.isNetworkAvailable(this)) {
            showToastLong((String) getText(R.string.current_net_unused));
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            return;
        }
        mCompleteBtn.setEnabled(false);
        boolean onlineFlag = LYService.getInstance().isOnline();
        if (!onlineFlag) {
            LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
            if (localUser!=null) {
                LYService.getInstance().startCloudService(
                        localUser.getUserToken(),
                        localUser.getInitString(),mCallbackListener
                );
            }
        } else {
            setBindCameraMgmt();
        }
    }

    private void setBindCameraMgmt() {
        mBindCameraMgmt.bindCamera(this, mSn, mNameEdt.getText().toString()
                .trim(), mBindCameraCallback);
    }

    /**
     * 设为公众
     */
    private void setCameraToPublic() {
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        mPublicMgmt.setPublic(FourthOfAddDeviceActivity.this, mCId,
                mPublicCallBack);
    }

}
