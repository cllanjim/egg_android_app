package com.lingyang.camera.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.lingyang.camera.R;
import com.lingyang.camera.util.Utils;

/**
 * 文件名：DialogAddDeviceNotify
 * 描述：此dialog是绑定第一步设置摄像机初始化状态提醒
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class DialogAddDeviceNotify extends Dialog implements View.OnClickListener {
    private Context mContext;

    public DialogAddDeviceNotify(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View content = LayoutInflater.from(mContext).inflate(R.layout.dialog_adddevice_notify, null);
        setContentView(content);
        getWindow().setWindowAnimations(R.style.AnimationDialog);
        Utils.setDialogAttributes((Activity) mContext, this, 0, 0, Gravity.CENTER);
        init();

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void init() {
        ImageButton cancel = (ImageButton) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            default:
                break;
        }
    }

}
