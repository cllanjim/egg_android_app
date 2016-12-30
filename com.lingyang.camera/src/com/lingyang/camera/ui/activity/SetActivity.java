package com.lingyang.camera.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.entity.GetCameraSetResponse.CameraSet;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.GetCameraSetMgmt;
import com.lingyang.camera.mgmt.RelieveBindMgmt;
import com.lingyang.camera.mgmt.UpdateCameraSetMgmt;
import com.lingyang.camera.ui.fragment.AttentionFragment;
import com.lingyang.camera.util.FileUtil;

import java.io.File;

/**
 * 文件名：SetDefinitionSelectorActivity
 * 描述：
 * 此类是选择摄像机的清晰度，高清，标清，流畅
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class SetActivity extends AppBaseActivity implements OnClickListener {

    private TextView mNameText;
    private TextView mDefinitionText;
    private CheckBox mPublicBox, mMicBox, mCloudBox;
    private GetCameraSetMgmt mGetCameraSetMgmt;
    private UpdateCameraSetMgmt mUpdateCameraSetMgmt;
    private RelieveBindMgmt mRelieveMgmt;
    private String mCId;
    private String mCName, mTempName;
    private CameraSet mCamera = new CameraSet(), mTempCamera = new CameraSet();
    /**
     * 获取设置回调
     */
    BaseCallBack<CameraSet> mGetCallBack = new BaseCallBack<CameraSet>() {

        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object != null) {
                CLog.e("getset---" + object.error_code + object.error_msg);
                showToast(object.error_code + object.error_msg);
            } else {
                showToast(getString(R.string.get_camera_set_fail));
            }
        }

        @Override
        public void success(final CameraSet t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (t != null) {
                        mCamera = t;
                        mTempCamera.camera_type = t.camera_type;
                        mTempCamera.rate = t.rate;
                        mTempCamera.silence = t.silence;
                        mPublicBox.setChecked(t.isPublic());
                        mMicBox.setChecked(t.isSilence());
                        mCloudBox.setChecked(t.isCloud());
                        mDefinitionText.setText(getText(t.getRate()));

                    }
                }
            });
        }
    };
    private Intent mIntent;
    /**
     * 解除绑定回调
     */
    BaseCallBack<BaseResponse> mRelieveCallBack = new BaseCallBack<BaseResponse>() {

        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object != null) {
                showToast(object.error_code + object.error_msg);
            } else {
                showToast(getString(R.string.unbind_fail));
            }
        }

        @Override
        public void success(BaseResponse t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            showToast((String) getText(R.string.set_relieve_suc));
            deleteOldCover(mCId);
            mIntent.putExtra(Const.IntentKeyConst.KEY_SET_IS_UNBIND, true);
            LocalBroadcastManager.getInstance(SetActivity.this).sendBroadcast(
                    mIntent);
            finish();
        }
    };
    private void deleteOldCover(final String cid) {
        ThreadPoolManagerQuick.execute(new Runnable() {
            @Override
            public void run() {
                File dir = FileUtil.getInstance().getSnapshotFile();
                if (dir == null || !dir.exists()) {
                    return;
                }
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (file != null && file.isFile()) {
                        if (file.getName().contains(cid)) {
                            file.delete();
                        }
                    }
                }
            }
        });
    }
    private int mType;
    /**
     * 保存设置回调
     */
    BaseCallBack<BaseResponse> mUpdateCallBack = new BaseCallBack<BaseResponse>() {

        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object != null) {
                showToast(object.error_msg);
            }
            finish();
        }

        @Override
        public void success(BaseResponse t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            showToast((String) getText(R.string.set_suc));

            mIntent.putExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE, mType);
            mIntent.putExtra(Const.IntentKeyConst.KEY_CNAME, mCName);
            mIntent.putExtra(Const.IntentKeyConst.KEY_CID, mCId);
            setResult(AttentionFragment.RESULT_CODE_SETTING);
            LocalBroadcastManager.getInstance(SetActivity.this).sendBroadcast(
                    mIntent);
            CLog.v("mUpdateCallBack");
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        mCId = getIntent().getStringExtra(Const.IntentKeyConst.KEY_CID);
        mCName = getIntent().getStringExtra(Const.IntentKeyConst.KEY_CNAME);
        mType = getIntent().getIntExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE, 0);
        mTempName = mCName;
        mGetCameraSetMgmt = (GetCameraSetMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(GetCameraSetMgmt.class);
        mUpdateCameraSetMgmt = (UpdateCameraSetMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(UpdateCameraSetMgmt.class);
        mRelieveMgmt = (RelieveBindMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(RelieveBindMgmt.class);
        mIntent = new Intent(Const.Actions.ACTION_IS_PUBLIC_REFRESH);
        mIntent.putExtra(Const.IntentKeyConst.KEY_CID, mCId);
        mIntent.putExtra(Const.IntentKeyConst.KEY_FROM_WHERE, Const.IntentKeyConst.REFRESH_FROM_SET);

        initView();
        getSet();
    }

    private void initView() {
        mNameText = (TextView) findViewById(R.id.tv_name);
        mDefinitionText = (TextView) findViewById(R.id.tv_definition);
        TextView relieveText = (TextView) findViewById(R.id.tv_relieve);
        TextView titleText = (TextView) findViewById(R.id.tv_header_title);
        ImageView backImg = (ImageView) findViewById(R.id.iv_heder_back);
        RelativeLayout nameLayout = (RelativeLayout) findViewById(R.id.layout_name);
        RelativeLayout definitionLayout = (RelativeLayout) findViewById(R.id.layout_definition);
        mPublicBox = (CheckBox) findViewById(R.id.box_public);
        mMicBox = (CheckBox) findViewById(R.id.box_mic);
        mCloudBox = (CheckBox) findViewById(R.id.box_cloud);

        mNameText.setText(mCName);
        titleText.setText(getText(R.string.set));
        backImg.setVisibility(View.VISIBLE);
        backImg.setOnClickListener(this);
        nameLayout.setOnClickListener(this);
        definitionLayout.setOnClickListener(this);
        relieveText.setOnClickListener(this);
    }

    /**
     * 获取配置信息
     */
    public void getSet() {
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        mGetCameraSetMgmt.getCameraSet(getApplicationContext(), mCId, mGetCallBack);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_heder_back:
                saveSet();
                break;
            case R.id.layout_name:
                // 修改摄像机名称
                Intent intent = new Intent(
                        Const.Actions.ACTION_ACTIVITY_EDIT_NICKNAME);
                intent.putExtra(Const.IntentKeyConst.KEY_EDIT_FORM_WHERE,
                        Const.IntentKeyConst.EDIT_FROM_CAMERA);
                intent.putExtra(Const.IntentKeyConst.KEY_CID, mCId);
                intent.putExtra(Const.IntentKeyConst.KEY_CNAME, mCName);
                startActivityForResult(intent, Const.SET_RESULT_REQUEST_CODE);
                break;
            case R.id.layout_definition:
                // 修改清晰度
                Intent it = new Intent(
                        Const.Actions.ACTION_ACTIVITY_SET_DEFINITIONG_SELECTOR);
                it.putExtra(Const.IntentKeyConst.KEY_CAMERA_RATE, mCamera.rate);
                startActivityForResult(it, Const.SET_RESULT_REQUEST_CODE);
                break;
            case R.id.tv_relieve:
                // 解除绑定
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.remove_bind_of_camera))
                        .setMessage(getString(R.string.confirm_remove_bind_of_camera))
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRelieveMgmt.relieveBind(SetActivity.this, mCId, mRelieveCallBack);
                    }
                }).create().show();
                break;
            default:
                break;
        }
    }

    /**
     * 保存设置
     */
    public void saveSet() {
        if (!mHasNetWork) {
            return;
        }
        if (mMicBox.isChecked()) {
            mCamera.silence = CameraSet.SILENCE_TRUE;
        } else {
            mCamera.silence = CameraSet.SILENCE_FALSE;
        }
        if (mPublicBox.isChecked() && mCloudBox.isChecked()) {
            mCamera.camera_type = CameraSet.TYPE_PUBLIC_AND_CLOUD;
            mType = Camera.TYPE_PUBLIC;
        } else if (!mPublicBox.isChecked() && !mCloudBox.isChecked()) {
            mCamera.camera_type = CameraSet.TYPE_PRIVATE;
            mType = Camera.TYPE_PRIVATE;
        } else if (!mPublicBox.isChecked()) {
            mCamera.camera_type = CameraSet.TYPE_CLOUD;
            mType = Camera.TYPE_PRIVATE;
        } else {
            mCamera.camera_type = CameraSet.TYPE_PUBLIC;
            mType = Camera.TYPE_PUBLIC;
        }
        if (mCamera.camera_type != mTempCamera.camera_type || mCamera.rate != mTempCamera.rate
                || mCamera.silence != mTempCamera.silence) {
            mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
            mUpdateCameraSetMgmt.UpdateCameraSet(this, mCId, mCamera,
                    mUpdateCallBack);
        } else {
            if (mCName.equals(mTempName)) {
                finish();
            } else {
                mIntent.putExtra(Const.IntentKeyConst.KEY_CNAME, mCName);
                mIntent.putExtra(Const.IntentKeyConst.KEY_CAMERA_TYPE, mType);
                LocalBroadcastManager.getInstance(SetActivity.this).sendBroadcast(
                        mIntent);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.SET_RESULT_REQUEST_CODE && data != null) {
            if (resultCode == Const.SET_RESULT_RESPONSE_CODE_ATTACH_DATA) {
                // 清晰度
                mCamera.rate = data.getIntExtra(
                        Const.IntentKeyConst.KEY_CAMERA_RATE, 3);
                mDefinitionText.setText(getText(mCamera.getRate()));
            } else if (resultCode == Const.SET_RESULT_RESPONSE_CODE_NOT_DATA) {
                // 摄像机名字
                mCName = data.getStringExtra(Const.IntentKeyConst.KEY_CNAME);
                mNameText.setText(mCName);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            saveSet();
        }
        return super.onKeyDown(keyCode, event);
    }

}
