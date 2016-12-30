package com.lingyang.camera.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.ui.activity.FourthOfAddDeviceActivity;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.Utils;

/**
 * 文件名：DialogAddDeviceBackNotify
 * 描述：此dialog是摄像机绑定失败提醒
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class DialogAddDeviceBackNotify extends Dialog implements View.OnClickListener {
    private Context mContext;

    public DialogAddDeviceBackNotify(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View content = LayoutInflater.from(mContext).inflate(R.layout.dialog_adddevice_back_notify, null);
        setContentView(content);
        getWindow().setWindowAnimations(R.style.AnimationDialog);
        Utils.setDialogAttributes((Activity) mContext, this, 0, 0, Gravity.CENTER);
        init();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void init() {
        TextView goMainText = (TextView) findViewById(R.id.gomain);
        goMainText.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }


    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.gomain:
                ActivityUtil.startActivity(mContext, Const.Actions.ACTION_ACTIVITY_MAIN);
                dismiss();
                break;
            default:
                ((FourthOfAddDeviceActivity) mContext).finish();
                break;
        }
    }

}
