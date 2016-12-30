package com.lingyang.camera.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.entity.GetCameraSetResponse.CameraSet;

/**
 * 文件名：SetDefinitionSelectorActivity
 * 描述：
 * 此类是选择摄像机的清晰度，高清，标清，流畅
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class SetDefinitionSelectorActivity extends Activity {
    private int mRate;

    OnClickListener mListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_heder_back) {
                finish();
            } else {
                Intent intent = new Intent();
                int rate = (Integer) v.getTag();
                intent.putExtra(Const.IntentKeyConst.KEY_CAMERA_RATE, rate);
                setResult(Const.SET_RESULT_RESPONSE_CODE_ATTACH_DATA, intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_definition_selector);
        mRate = getIntent().getIntExtra(Const.IntentKeyConst.KEY_CAMERA_RATE, 3);
        initView();
    }

    private void initView() {
        TextView titleText = (TextView) findViewById(R.id.tv_header_title);
        ImageView back = (ImageView) findViewById(R.id.iv_heder_back);
        RelativeLayout HDLayout = (RelativeLayout) findViewById(R.id.layout_hd);
        RelativeLayout SDLayout = (RelativeLayout) findViewById(R.id.layout_sd);
        RelativeLayout fluentLayout = (RelativeLayout) findViewById(R.id.layout_fluent);
        ImageView HDImg = (ImageView) findViewById(R.id.iv_hd);
        ImageView SDImg = (ImageView) findViewById(R.id.iv_sd);
        ImageView fluentImg = (ImageView) findViewById(R.id.iv_fluent);

        titleText.setText(getText(R.string.set_definition));
        HDLayout.setTag(CameraSet.RATE_HD);
        SDLayout.setTag(CameraSet.RATE_SD);
        fluentLayout.setTag(CameraSet.RATE_FLUENT);

        HDLayout.setOnClickListener(mListener);
        SDLayout.setOnClickListener(mListener);
        fluentLayout.setOnClickListener(mListener);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(mListener);

        switch (mRate) {
            case CameraSet.RATE_HD:
                HDImg.setVisibility(View.VISIBLE);
                break;
            case CameraSet.RATE_SD:
                SDImg.setVisibility(View.VISIBLE);
                break;
            case CameraSet.RATE_FLUENT:
                fluentImg.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
}
