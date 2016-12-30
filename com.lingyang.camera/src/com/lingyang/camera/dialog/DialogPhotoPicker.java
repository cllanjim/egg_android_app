package com.lingyang.camera.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.util.PhotoPickerUtils;
import com.lingyang.camera.util.Utils;

/**
 * 文件名：DialogPhotoPicker
 * 此dialog是拍照和相册选择弹出框
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class DialogPhotoPicker extends Dialog implements
        View.OnClickListener {
    public static final int CAMERA_DATA = 113;
    public static final int PICTURE_DATA = 111;
    public static final int PHOTO_CROP = 112;
    private Context mContext;
    private String mFilePath;

    public DialogPhotoPicker(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View content = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_photo_picker, null);
        setContentView(content);
        getWindow().setWindowAnimations(R.style.AnimationDialog);
        Utils.setDialogAttributes((Activity) mContext, this, 1f, 0,
                Gravity.BOTTOM);
        init();

        getWindow().setBackgroundDrawableResource(android.R.color.white);
    }

    private void init() {
        TextView photo = (TextView) findViewById(R.id.photo);
        TextView camera = (TextView) findViewById(R.id.camera);
        TextView cancel = (TextView) findViewById(R.id.cancel);

        photo.setOnClickListener(this);
        camera.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.photo:
                PhotoPickerUtils.startGallery(mContext);
                break;
            case R.id.camera:
                mFilePath = PhotoPickerUtils.startTakePhoto(mContext);
                break;
            case R.id.cancel:
                break;
            default:
                break;
        }
    }

    public String getPhotoPath() {
        return mFilePath;
    }
}
