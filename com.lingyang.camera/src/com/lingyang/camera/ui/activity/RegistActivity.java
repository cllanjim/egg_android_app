package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.base.utils.ThreadPoolManagerQuick;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.dialog.DialogPhotoPicker;
import com.lingyang.camera.entity.ChangeNickNameEntity;
import com.lingyang.camera.entity.LoginToken.UserToken;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.ChangeHeadMgmt;
import com.lingyang.camera.mgmt.ChangeNickNameMgmt;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.PhotoPickerUtils;
import com.lingyang.camera.util.Utils;
import com.lingyang.sdk.CallBackListener;
import com.lingyang.sdk.cloud.LYService;
import com.lingyang.sdk.exception.LYException;
import com.lingyang.sdk.util.CLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件名: RegistActivity
 * 描    述:该类负责设置昵称和头像，登录云平台
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class RegistActivity extends AppBaseActivity {
    private RoundImageView mHead;
    private EditText mNameEdt;
    private DialogPhotoPicker photoPicker;
    private File mFile;
    private String mCropPath = null;
    private String mName;
    private ChangeHeadMgmt mChangeHeadMgmt;
    private ChangeNickNameMgmt mChangeNickNameMgmt;

    /**
     * 设置昵称回调
     */
    BaseCallBack<ChangeNickNameEntity.NickNameCls> mSetNameCallBack = new BaseCallBack<ChangeNickNameEntity.NickNameCls>() {
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
        public void success(ChangeNickNameEntity.NickNameCls t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            if (t != null) {
                showToast((String) getText(R.string.mine_commit_suc));
                LocalUser user = LocalUserWrapper.getInstance().getLocalUser();
                user.setNickName(t.nickname);
                LocalUserWrapper.getInstance().updateUser(user);
                loginCloud();
            }
        }
    };
    /**
     * 设置头像回调
     */
    BaseCallBack<UserToken> mSetHeadCallBack = new BaseCallBack<UserToken>() {
        @Override
        public void error(ResponseError object) {
            mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
            showToast((String) getText(R.string.mine_upload_head_fail));
        }

        @Override
        public void success(UserToken t) {
            mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
            if (t != null) {
                LocalUser localUser = LocalUserWrapper.getInstance()
                        .getLocalUser();
                localUser.setHead(t.faceimage + "#" + System.currentTimeMillis());
                LocalUserWrapper.getInstance().updateUser(localUser);
                showToast((String) getText(R.string.mine_upload_head_suc));

            }
        }
    };

    OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.head:
                    if (photoPicker == null) {
                        photoPicker = new DialogPhotoPicker(RegistActivity.this);
                    }
                    photoPicker.show();
                    break;
                case R.id.commit:
                    mName = mNameEdt.getText().toString().trim();
                    if (TextUtils.isEmpty(mName)) {
                        showToast((String) getText(R.string.set_nickname));
                        return;
                    }
                    mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
                    mChangeNickNameMgmt.changeName(RegistActivity.this, mName, mSetNameCallBack);
                    break;
                case R.id.iv_heder_back:
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        mChangeHeadMgmt = (ChangeHeadMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(ChangeHeadMgmt.class);
        mChangeNickNameMgmt = (ChangeNickNameMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(ChangeNickNameMgmt.class);
        initView();
    }

    private void initView() {
        mHead = (RoundImageView) findViewById(R.id.head);
        mNameEdt = (EditText) findViewById(R.id.name);
        Button commitBtn = (Button) findViewById(R.id.commit);
        TextView titleTextView = (TextView) findViewById(R.id.tv_header_title);
        ImageView backImg = (ImageView) findViewById(R.id.iv_heder_back);

        titleTextView.setText(getText(R.string.set));
        backImg.setVisibility(View.VISIBLE);
        backImg.setOnClickListener(mOnClickListener);
        mHead.setOnClickListener(mOnClickListener);
        commitBtn.setOnClickListener(mOnClickListener);
    }

    /**
     * 登录云平台
     */
    public void loginCloud() {
        LocalUser localUser = LocalUserWrapper.getInstance().getLocalUser();
        LocalUserWrapper.getInstance().updateUser(localUser);
        LocalUserWrapper.getInstance().setLocalUser(localUser);
        ThreadPoolManagerQuick.execute(new Runnable() {

            @Override
            public void run() {
                MyPreference.getInstance().setValue(RegistActivity.this,
                        MyPreference.LOGIN_STATE, true);
                MyPreference.getInstance().setValue(RegistActivity.this,
                        MyPreference.LOGIN_PASSWORD, LocalUserWrapper.getInstance().getLocalUser().getPassword());
                CLog.v("Const.TOPVDN_CLOUD_APPID:"
                        + Const.TOPVDN_CLOUD_APPID
                        + "LocalUserWrapper.getInstance().getLocalUser().getUid()："
                        + LocalUserWrapper.getInstance().getLocalUser()
                        .getUid());

                LYService.getInstance().startCloudService(
                        LocalUserWrapper.getInstance().getLocalUser().getUserToken(),
                        LocalUserWrapper.getInstance().getLocalUser().getInitString(),
                        new CallBackListener<Long>() {
                            @Override
                            public void onSuccess(Long aLong) {
                                mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
                                mHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        goToMain();
                                    }
                                });
                            }

                            @Override
                            public void onError(LYException e) {
                                mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                                showToast((String) getText(R.string.cloud_login_fail));
                                goToLogin(false);
                            }
                        }
                );
                /*CloudOpenAPI.getInstance().startCloudService(
                        Const.TOPVDN_CLOUD_APPID,
                        LocalUserWrapper.getInstance()
                                .getLocalUser().getUid().substring(4),
                        Const.CLOUD_DEFALUTPASSWORD, "",
                        new ICloudOpenAPI.onlineLoginStatesChangeListener() {

                            public void onUserOnline(String... args) {
                                mHandler.sendEmptyMessage(Constants.TaskState.SUCCESS);
                                mHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        goToMain();
                                    }
                                });
                            }

                            public void onUserOffline() {
                                mHandler.sendEmptyMessage(Constants.TaskState.FAILURE);
                                showToast((String) getText(R.string.cloud_login_fail));
                                goToLogin(false);
                            }
                        });*/
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DialogPhotoPicker.CAMERA_DATA:
                String path = photoPicker != null ? photoPicker.getPhotoPath() : null;
                if (path != null) {
                    mCropPath = PhotoPickerUtils.startPhotoCrop(
                            RegistActivity.this, Uri.fromFile(new File(path)), 500,
                            500);
                }
                break;
            case DialogPhotoPicker.PICTURE_DATA:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        String uriStr = PhotoPickerUtils.getUri(uri,
                                RegistActivity.this);
                        if (uriStr != null) {
                            uri = PhotoPickerUtils.getProviderUri(
                                    RegistActivity.this, uriStr);
                        }
                        mCropPath = PhotoPickerUtils.startPhotoCrop(
                                RegistActivity.this, uri, 500, 500);
                    }
                }
                break;
            case DialogPhotoPicker.PHOTO_CROP:
                mFile = setPicToView(mCropPath);
                if (mFile == null) {
                    Utils.displayUserIconImageView(mHead, LocalUserWrapper
                            .getInstance().getLocalUser().getHead());
                    break;
                }
                setHead();
                mCropPath = null;
                path = null;
                break;
            default:
                break;
        }
    }

    private File setPicToView(String path) {
        if (path == null)
            return null;
        File file = null;
        Bitmap photo = PhotoPickerUtils.loadImageWithSize(path, 240, 240);
        mHead.setImageBitmap(photo);
        try {
            file = cacheAvatar(RegistActivity.this, photo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        photo = null;
        return file;
    }

    private void setHead() {
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        mChangeHeadMgmt.changeHead(this, mFile, mSetHeadCallBack);
    }

    /**
     * 将bitmap写到文件，得到文件路径
     *
     * @param context
     * @param bitmap
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static File cacheAvatar(Context context, Bitmap bitmap)
            throws IOException {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        File file = new File(context.getCacheDir(), "avatar.jpeg");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
        return file;
    }

}
