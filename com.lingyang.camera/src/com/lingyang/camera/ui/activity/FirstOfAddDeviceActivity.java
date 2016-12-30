package com.lingyang.camera.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.dialog.DialogAddDeviceNotify;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.Utils;

/**
 * 文件名：FirstOfAddDeviceActivity
 * 描述：
 * 此类是绑定摄像机的第一步，提醒用户检查摄像机是否处于初始状态（摄像机是否红蓝闪烁）
 * 只有当摄像机处于红蓝闪烁状态时才能正常绑定
 * 创建人：廖蕾
 * 时间：2015/9/16
 */
public class FirstOfAddDeviceActivity extends AppBaseActivity {

    TextView mTwinkleTextView;
    TextView mUnTwinkleTextView;
    TextView mTitleTextView;
    ImageView mBackImageView;
    Button mNextButton;

    OnClickListener mNextListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            goToSecond();
        }
    };
    /**
     * 没有看到红蓝闪烁，提示用户
     */
    OnClickListener mUnTwinkleListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogAddDeviceNotify n = new DialogAddDeviceNotify(
                    FirstOfAddDeviceActivity.this);
            n.show();
        }
    };
    OnClickListener mBackListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_of_add_device);
        initView();

    }

    private void initView() {
        mTwinkleTextView = (TextView) findViewById(R.id.tv_twinkle);
        mUnTwinkleTextView = (TextView) findViewById(R.id.tv_untwinkle);
        mTitleTextView = (TextView) findViewById(R.id.tv_header_title);
        mBackImageView = (ImageView) findViewById(R.id.iv_heder_back);
        mNextButton = (Button) findViewById(R.id.btn_next);

        mNextButton.setOnClickListener(mNextListener);
        mUnTwinkleTextView.setOnClickListener(mUnTwinkleListener);
        mBackImageView.setOnClickListener(mBackListener);

        mBackImageView.setVisibility(View.VISIBLE);
        mTitleTextView.setText(getText(R.string.add_first_step));
        mTwinkleTextView.setText(Utils.makeTextWithTag(
                (String) getText(R.string.red),
                getResources().getColor(R.color.red), null).append(
                Utils.makeTextWithTag((String) getText(R.string.blue),
                        getResources().getColor(R.color.blue),
                        (String) getText(R.string.add_twinkle))));
    }

    /**
     * 去绑定步骤第二步连接WiFi
     */
    public void goToSecond() {
        ActivityUtil.startActivity(FirstOfAddDeviceActivity.this,
                Const.Actions.ACTION_ACTIVITY_SECOND_OF_ADD_DEVICE);
    }
}
