package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalUser;
import com.lingyang.camera.dialog.DialogPhotoPicker;
import com.lingyang.camera.entity.LoginToken.UserToken;
import com.lingyang.camera.entity.ResponseError;
import com.lingyang.camera.framework.BaseCallBack;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.ChangeHeadMgmt;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.PhotoPickerUtils;
import com.lingyang.camera.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件名: UserInfoActivity
 * 描    述: 用户信息。该类负责头像及昵称的显示
 * 创建人:
 * 创建时间: 2015/10
 */
public class UserInfoActivity extends AppBaseActivity implements
        OnClickListener {

    BaseCallBack<UserToken> mCallBack = new BaseCallBack<UserToken>() {
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
    private ImageView mBackImg;
    private RoundImageView mHeadCircleImageView;
    private TextView mNameText, mPhoneText, mTitleText;
    private DialogPhotoPicker mPhotoPicker;
    private String croppedPath = null;
    private File mFile;
    private ChangeHeadMgmt mChangeHeadMgmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        mChangeHeadMgmt = (ChangeHeadMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(ChangeHeadMgmt.class);
        initView();
        updateUi();

    }

    private void initView() {
        mTitleText = (TextView) findViewById(R.id.tv_header_title);
        mBackImg = (ImageView) findViewById(R.id.iv_heder_back);
        mHeadCircleImageView = (RoundImageView) findViewById(R.id.img_head);
        mNameText = (TextView) findViewById(R.id.tv_name);
        mPhoneText = (TextView) findViewById(R.id.tv_phone);
        RelativeLayout headLayout = (RelativeLayout) findViewById(R.id.layout_head);
        RelativeLayout nameLayout = (RelativeLayout) findViewById(R.id.layout_name);

        mBackImg.setOnClickListener(this);
        mHeadCircleImageView.setOnClickListener(this);
        headLayout.setOnClickListener(this);
        nameLayout.setOnClickListener(this);
    }

    /**
     * 显示头像、昵称、电话号码
     */
    public void updateUi() {
        mTitleText.setText(getText(R.string.mine_user_info));
        mNameText.setText(LocalUserWrapper.getInstance().getLocalUser()
                .getNickName());
        mPhoneText.setText(LocalUserWrapper.getInstance().getLocalUser()
                .getMobile());
        Utils.displayUserIconImageView(mHeadCircleImageView, LocalUserWrapper
                .getInstance().getLocalUser().getHead());
        mBackImg.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_heder_back:
                setResult(Const.SET_RESULT_RESPONSE_CODE_ATTACH_DATA);
                finish();
                break;
            case R.id.head:
                if (mPhotoPicker == null) {
                    mPhotoPicker = new DialogPhotoPicker(this);
                }
                mPhotoPicker.show();
                break;
            case R.id.img_head:
                if (mPhotoPicker == null) {
                    mPhotoPicker = new DialogPhotoPicker(this);
                }
                mPhotoPicker.show();
                break;
            case R.id.layout_head:
                if (mPhotoPicker == null) {
                    mPhotoPicker = new DialogPhotoPicker(this);
                }
                mPhotoPicker.show();
                break;
            case R.id.layout_name:
                Intent intent = new Intent(
                        Const.Actions.ACTION_ACTIVITY_EDIT_NICKNAME);
                intent.putExtra(Const.IntentKeyConst.KEY_EDIT_FORM_WHERE,
                        Const.IntentKeyConst.EDIT_FROM_USER);
                intent.putExtra(Const.IntentKeyConst.KEY_CNAME, LocalUserWrapper
                        .getInstance().getLocalUser().getNickName());
                startActivityForResult(intent, Const.SET_RESULT_REQUEST_CODE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DialogPhotoPicker.CAMERA_DATA:
                String path = mPhotoPicker != null ? mPhotoPicker.getPhotoPath() : null;
                if (path != null) {
                    croppedPath = PhotoPickerUtils.startPhotoCrop(
                            UserInfoActivity.this, Uri.fromFile(new File(path)),
                            500, 500);
                }
                break;
            case DialogPhotoPicker.PICTURE_DATA:
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        String uriStr = PhotoPickerUtils.getUri(uri,
                                UserInfoActivity.this);
                        if (uriStr != null) {
                            uri = PhotoPickerUtils.getProviderUri(
                                    UserInfoActivity.this, uriStr);
                        }
                        croppedPath = PhotoPickerUtils.startPhotoCrop(
                                UserInfoActivity.this, uri, 500, 500);
                    }
                }
                break;
            case DialogPhotoPicker.PHOTO_CROP:
                mFile = setPicToView(croppedPath);
                if (mFile == null) {
                    Utils.displayUserIconImageView(mHeadCircleImageView, LocalUserWrapper
                            .getInstance().getLocalUser().getHead());
                    break;
                }
                changeHead();
                croppedPath = null;
                path = null;
                break;
            case Const.SET_RESULT_REQUEST_CODE:
                if (resultCode == Const.SET_RESULT_RESPONSE_CODE_ATTACH_DATA) {
                    updateUi();
                }
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
        mHeadCircleImageView.setImageBitmap(photo);
        try {
            file = cacheAvatar(UserInfoActivity.this, photo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        photo = null;
        return file;
    }

    public void changeHead() {
        mHandler.sendEmptyMessage(Constants.TaskState.ISRUNING);
        mChangeHeadMgmt.changeHead(this, mFile, mCallBack);
    }

    // 讲bitmap写到文件，得到文件路径
    public static File cacheAvatar(Context context, Bitmap bitmap)
            throws IOException, FileNotFoundException {
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
