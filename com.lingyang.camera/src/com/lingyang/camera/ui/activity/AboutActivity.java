package com.lingyang.camera.ui.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.base.utils.DeviceUtils;
import com.lingyang.base.utils.ThreadPoolManagerNormal;
import com.lingyang.camera.BuildConfig;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.exception.LogUtil;
import com.lingyang.camera.util.ActivityUtil;
import com.lingyang.camera.util.FileUtil;
import com.lingyang.sdk.cloud.LYService;

import java.io.File;

/**
 * 文件名：AboutActivity
 * 描述：该类负责获取sdk，cloud和app的版本号
 * 创建人：杜舒
 * 时间：2015/10
 */
public class AboutActivity extends AppBaseActivity {

    TextView mTitleTv, mVersionNumber;
    ImageView mBackIv;
    private DialogInterface.OnClickListener mOnDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    dialog.dismiss();
                    if (checkLogExist()) {
                        showToast(getString(R.string.log_sending));
                        sendLog();
                    } else {
                        showToast(getString(R.string.log_not_exist));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void sendLog() {
        ThreadPoolManagerNormal.execute(new Runnable() {
            @Override
            public void run() {
                LogUtil.getInstance().saveSystemLog2File();
                final boolean b = LogUtil.getInstance().uploadDebugLog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(b ? getString(R.string.send_success) :
                                getString(R.string.send_fail));
                    }
                });
            }
        });
    }

    private boolean checkLogExist() {
        File dir = FileUtil.getInstance().getLogFile();
        if (dir == null || !dir.exists()) {
            return false;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().contains("debug")) {
                    return true;
                }
            }
        }
        return false;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_heder_back:
                    finish();
                    break;
                case R.id.btn_official_website:
                    ActivityUtil.startActivity(AboutActivity.this, OfficialWebActivity.class);
                    break;
                case R.id.btn_user_agreement:
                    ActivityUtil.startActivity(AboutActivity.this, UserAgreementActivity.class);
                    break;
                case R.id.btn_send_log:
                    new AlertDialog.Builder(AboutActivity.this)
                            .setTitle(R.string.system_prompt)
                            .setMessage(R.string.confirm_send_log)
                            .setNegativeButton(R.string.cancel, mOnDialogClickListener)
                            .setPositiveButton(R.string.confirm, mOnDialogClickListener)
                            .create().show();
                    break;
                case R.id.btn_update_history:
                    ActivityUtil.startActivity(AboutActivity.this, UpdateHistoryActivity.class);
                    break;
                case R.id.btn_app_download:
                    ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData.newPlainText("text", Const.APP_DOWNLOAD_URL);
                    cmb.setPrimaryClip(ClipData.newPlainText("text", Const.APP_DOWNLOAD_URL));
                    showToast(getString(R.string.app_download_url_copy));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        mTitleTv = (TextView) findViewById(R.id.tv_header_title);
        mBackIv = (ImageView) findViewById(R.id.iv_heder_back);
        mVersionNumber = (TextView) findViewById(R.id.tv_version_number);
//        mVersionNumber.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(AboutActivity.this,AutoTestActivity.class));
//            }
//        });
        Button btnOfficialWebsite = (Button) findViewById(R.id.btn_official_website);
        Button btnUserAgreement = (Button) findViewById(R.id.btn_user_agreement);
        Button btnSendLog = (Button) findViewById(R.id.btn_send_log);
        Button btnUpdateHistory = (Button) findViewById(R.id.btn_update_history);
        Button btnAppDownload = (Button) findViewById(R.id.btn_app_download);
        btnOfficialWebsite.setOnClickListener(mOnClickListener);
        btnUserAgreement.setOnClickListener(mOnClickListener);
        btnSendLog.setOnClickListener(mOnClickListener);
        btnUpdateHistory.setOnClickListener(mOnClickListener);
        btnAppDownload.setOnClickListener(mOnClickListener);
        mBackIv.setOnClickListener(mOnClickListener);

        mTitleTv.setText(getText(R.string.mine_about));
        mBackIv.setVisibility(View.VISIBLE);
        mVersionNumber.setText(String.format("%s , APP V%s C%s %s",
                LYService.getInstance().getSDKVersion(),
                DeviceUtils.getAppVersionName(this),
                DeviceUtils.getAppVersionCode(this),
                BuildConfig.BUILD_TYPE));
    }
}
