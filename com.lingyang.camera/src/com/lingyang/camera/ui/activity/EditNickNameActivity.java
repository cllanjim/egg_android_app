package com.lingyang.camera.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.entity.BaseResponse;
import com.lingyang.camera.entity.ChangeNickNameEntity.NickNameCls;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.ChangeCameraNameMgmt;
import com.lingyang.camera.mgmt.ChangeNickNameMgmt;

/**
 * 文件名：EditNickNameActivity
 * 描述：此类修改摄像机名称和用户昵称类
 * 创建人：廖蕾
 * 时间：2015/9
 */
public class EditNickNameActivity extends AppBaseActivity implements
        OnClickListener {
    BaseCallBack<NickNameCls> mCallBack = new BaseCallBack<NickNameCls>() {
        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object == null) {
                showToast((String) getText(R.string.mine_commit_fail));
            } else {
                showToast((object.error_code + "--" + object.error_msg));
            }
        }

        @Override
        public void success(NickNameCls t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            if (t != null) {
                showToast((String) getText(R.string.mine_commit_suc));
                LocalUser user = LocalUserWrapper.getInstance().getLocalUser();
                user.setNickName(t.nickname);
                LocalUserWrapper.getInstance().updateUser(user);
                Intent intent = new Intent();
                intent.putExtra(Const.IntentKeyConst.KEY_NICKNAME, t.nickname);
                setResult(Const.SET_RESULT_RESPONSE_CODE_ATTACH_DATA, intent);
                finish();
            }
        }
    };
    private ImageButton mSaveBtn;
    private EditText mNameEdt;
    private String mName;
    BaseCallBack<BaseResponse> mCameraCallBack = new BaseCallBack<BaseResponse>() {
        @Override
        public void success(BaseResponse t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            Intent it = new Intent();
            it.putExtra(Const.IntentKeyConst.KEY_CNAME, mName);
            setResult(Const.SET_RESULT_RESPONSE_CODE_NOT_DATA, it);
            finish();
        }

        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            if (object == null) {
                showToast((String) getText(R.string.mine_commit_fail));
            } else {
                showToast((object.error_code + "--" + object.error_msg));
            }
        }
    };
    private int mTag;
    private String mCid;
    private ChangeNickNameMgmt mChangeNickNameMgmt;
    private ChangeCameraNameMgmt mChangeCameraNameMgmt;
    private TextWatcher mTextChangeListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() > 0) {
                mSaveBtn.setEnabled(true);
            } else {
                mSaveBtn.setEnabled(false);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_nick_name);
        mTag = getIntent().getIntExtra(Const.IntentKeyConst.KEY_EDIT_FORM_WHERE, 0);
        mCid = getIntent().getStringExtra(Const.IntentKeyConst.KEY_CID);
        mName = getIntent().getStringExtra(Const.IntentKeyConst.KEY_CNAME);
        mChangeNickNameMgmt = (ChangeNickNameMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(ChangeNickNameMgmt.class);
        mChangeCameraNameMgmt = (ChangeCameraNameMgmt) MgmtClassFactory
                .getInstance().getMgmtClass(ChangeCameraNameMgmt.class);
        initView();
    }

    private void initView() {
        TextView titleText = (TextView) findViewById(R.id.tv_header_title);
        mSaveBtn = (ImageButton) findViewById(R.id.imgbtn_save);
        ImageButton deleteBtn = (ImageButton) findViewById(R.id.imgbtn_delete);
        mNameEdt = (EditText) findViewById(R.id.edt_name);
        ImageView backImg = (ImageView) findViewById(R.id.iv_heder_back);

        titleText.setText(getText(R.string.mine_name));
        mNameEdt.setText(LocalUserWrapper.getInstance().getLocalUser()
                .getNickName());
        backImg.setVisibility(View.VISIBLE);
        mSaveBtn.setVisibility(View.VISIBLE);
        mSaveBtn.setEnabled(false);

        if (mName != null) {
            mNameEdt.setText(mName);
            mNameEdt.setSelection(mName.length());
        }

        mNameEdt.addTextChangedListener(mTextChangeListener);

        mSaveBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        backImg.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgbtn_save:
                mName = mNameEdt.getText().toString().trim();
                if (TextUtils.isEmpty(mName)) {
                    showToast((String) getText(R.string.mine_name_can_not_null));
                    return;
                }
                if (mTag == Const.IntentKeyConst.EDIT_FROM_CAMERA) {
                    mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
                    mChangeCameraNameMgmt.changeName(this, mCid, mName,
                            mCameraCallBack);

                } else if (mTag == Const.IntentKeyConst.EDIT_FROM_USER) {
                    mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
                    mChangeNickNameMgmt.changeName(this, mName, mCallBack);
                }
                break;
            case R.id.imgbtn_delete:
                mNameEdt.setText("");
                break;
            case R.id.iv_heder_back:
                setResult(Const.SET_RESULT_RESPONSE_CODE_NOT_DATA);
                finish();
                break;
            default:
                break;
        }
    }

}
