package com.lingyang.camera.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.camera.R;

/**
 * 用户协议
 */
public class UserAgreementActivity extends Activity {

    private TextView mTitleTv;
    private ImageView mBackIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_agreement);
        initView();
    }

    private void initView() {
        mTitleTv = (TextView) findViewById(R.id.tv_header_title);
        mBackIv = (ImageView) findViewById(R.id.iv_heder_back);

        mTitleTv.setText(getText(R.string.user_agreement));
        mBackIv.setOnClickListener(mListener);
    }
    View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
