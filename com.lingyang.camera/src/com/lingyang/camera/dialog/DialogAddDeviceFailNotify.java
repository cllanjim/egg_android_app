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
import com.lingyang.camera.ui.activity.ThirdOfAddDeviceActivity;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.Utils;

/**
 * 文件名：DialogAddDeviceFailNotify
 * 描述：此dialog是摄像机绑定时连网失败提醒
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class DialogAddDeviceFailNotify extends Dialog implements View.OnClickListener {
    private Context mContext;
    private String info;

    public DialogAddDeviceFailNotify(Context context,String info) {
        super(context);
        this.mContext = context;
        this.info=info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View content = LayoutInflater.from(mContext).inflate(R.layout.dialog_adddevice_fail_notify, null);
        setContentView(content);
        getWindow().setWindowAnimations(R.style.AnimationDialog);
        Utils.setDialogAttributes((Activity) mContext, this, 0, 0, Gravity.CENTER);
        init();

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void init() {
        TextView addAgainText = (TextView) findViewById(R.id.addagain);
        TextView goMainText = (TextView) findViewById(R.id.gomain);
        TextView mInfoTextView= (TextView) findViewById(R.id.tv_info);
        mInfoTextView.setText(info+"");

        addAgainText.setOnClickListener(this);
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
            case R.id.addagain:
                ActivityUtil.startActivity(mContext, Const.Actions.ACTION_ACTIVITY_FIRST_OF_ADD_DEVICE);
                dismiss();
                break;
            case R.id.gomain:
                ActivityUtil.startActivity(mContext, Const.Actions.ACTION_ACTIVITY_MAIN);
                dismiss();
                break;
            default:
                ((ThirdOfAddDeviceActivity) mContext).finish();
                break;
        }
    }

}
