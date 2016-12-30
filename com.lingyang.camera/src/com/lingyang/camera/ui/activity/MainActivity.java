package com.lingyang.camera.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;

import com.lingyang.base.utils.CLog;
import com.lingyang.base.utils.executor.ThreadPoolManager;
import com.lingyang.camera.CameraApplication;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.ui.adapter.MainAdapter;
import com.lingyang.camera.ui.fragment.HomeFragment;
import com.lingyang.camera.ui.widget.CustomTabPageIndicator;
import com.lingyang.camera.ui.widget.CustomViewPager;
import com.lingyang.camera.util.ActivityUtil;

/**
 * 文件名：MainActivity
 * 描述：此类类似于一个容器，包含了看视频，直播，视频通话和我四个模块界面
 * 创建人：廖蕾
 * 时间：2015/9
 */
public class MainActivity extends AppBaseActivity implements ViewPager.OnPageChangeListener {
    boolean mAttentionNeedRefresh = false;
    boolean mPublicNeedRefresh = false;
    HomeFragment mAttentionFragment = null;
    private MainAdapter mMainAdapter;
    private CustomViewPager mPager;
    private CustomTabPageIndicator mIndicator;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);

        initView();
        initData();
    }


    private void initView() {
        mPager = (CustomViewPager) findViewById(R.id.main_pager);
        mIndicator = (CustomTabPageIndicator) findViewById(R.id.main_indicator);
    }

    private void initData() {
        mMainAdapter = new MainAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(mMainAdapter);
        mPager.setOffscreenPageLimit(mMainAdapter.getCount() - 1);
        mIndicator.setViewPager(mPager);
        mIndicator.setOnPageChangeListener(this);
        mIndicator.setCurrentItem(0);
        //点击第2,3个tab,直接跳转
        mIndicator.addRedirectTabPage(1);
        mIndicator.addRedirectTabPage(2);
        mIndicator.setOnRedirectListener(new CustomTabPageIndicator.OnRedirectListener() {
            @Override
            public void redirect(int selected) {
                if (selected == 1) {
                    ActivityUtil.startActivity(MainActivity.this, Const.Actions.ACTION_ACTIVITY_USE_MOBILE_LIVE2);
                }else if (selected==2) {
                    ActivityUtil.startActivity(MainActivity.this, Const.Actions.ACTION_ACTIVITY_CONTACTS);
                }
            }
        });
        mAttentionFragment = (HomeFragment) mMainAdapter.getItem(0);
    }

    @Override
    protected void onDestroy() {
        ThreadPoolManager.getInstance(ThreadPoolManager.TYPE_QUICK).clear();
        ThreadPoolManager.getInstance(ThreadPoolManager.TYPE_NORMAL).clear();
        super.onDestroy();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0 && mAttentionNeedRefresh) {
            mMainAdapter.refresh(position, false, false);
            mAttentionNeedRefresh = false;
        } else if (position == 1 && mPublicNeedRefresh) {
            mMainAdapter.refresh(position, false, false);
            mPublicNeedRefresh = false;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 跳转至绑定界面
     *
     * @param view
     */
    public void btn_add_bind_click(View view) {
        startActivityFromFragment(mMainAdapter.getItem(0), new Intent(
                Const.Actions.ACTION_ACTIVITY_FIRST_OF_ADD_DEVICE), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mAlertDialog==null) {
                mAlertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.system_prompt)
                        .setMessage(R.string.confirm_exit)
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .setPositiveButton(R.string.confirm,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        ((CameraApplication) getApplication())
                                                .AppExit();
                                        dialog.dismiss();
                                    }
                                }).create();

            }
            if (!isFinishing()&&!mAlertDialog.isShowing()) {
                mAlertDialog.show();
            }
        }
        return false;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        CLog.v("onNewIntent");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CLog.v("onActivityResult ");
        super.onActivityResult(requestCode, resultCode, data);   //this
    }
}
