package com.lingyang.camera.ui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.db.LocalRecordWrapper;
import com.lingyang.camera.db.LocalUserWrapper;
import com.lingyang.camera.db.bean.LocalRecord;
import com.lingyang.camera.ui.adapter.MyFileListViewPagerAdapter;
import com.lingyang.camera.ui.widget.BottomBarController;
import com.lingyang.camera.ui.widget.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名：MyFileListActivity
 * 描述：此类是显示本地视频和截图的容器类，用fragment的具体展示
 * 创建人：廖蕾
 * 时间：2015/10
 */
public class MyFileListActivity extends AppBaseActivity implements
        OnClickListener {
    private TextView mAllText, mPictureText, mVideoText;
    private TextView mAllCountText, mPictureCountText, mVideoCountText;
    private CustomViewPager mViewPager;
    private List<TextView> mTextList = new ArrayList<TextView>();
    private List<TextView> mCountList = new ArrayList<TextView>();
    private BottomBarController mController;
    OnPageChangeListener listener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mController.selectedItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };
    private MyFileListViewPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_file_list);
        initView();
        initData();
        updateUi();
    }

    private void initView() {
        TextView titleTv = (TextView) findViewById(R.id.tv_header_title);
        ImageView backImg = (ImageView) findViewById(R.id.iv_heder_back);
        mAllText = (TextView) findViewById(R.id.tv_all);
        mPictureText = (TextView) findViewById(R.id.tv_picture);
        mVideoText = (TextView) findViewById(R.id.tv_video);
        mAllCountText = (TextView) findViewById(R.id.tv_all_count);
        mPictureCountText = (TextView) findViewById(R.id.tv_picture_count);
        mVideoCountText = (TextView) findViewById(R.id.tv_video_count);
        LinearLayout allLayout = (LinearLayout) findViewById(R.id.file_all);
        LinearLayout pictureLayout = (LinearLayout) findViewById(R.id.file_picture);
        LinearLayout videoLayout = (LinearLayout) findViewById(R.id.file_video);
        mViewPager = (CustomViewPager) findViewById(R.id.viewpager);

        titleTv.setText(getText(R.string.mine_file));
        backImg.setVisibility(View.VISIBLE);

        allLayout.setOnClickListener(this);
        pictureLayout.setOnClickListener(this);
        videoLayout.setOnClickListener(this);
        backImg.setOnClickListener(this);
        mViewPager.setOnPageChangeListener(listener);
    }

    private void initData() {
        mTextList.add(mAllText);
        mTextList.add(mPictureText);
        mTextList.add(mVideoText);

        mCountList.add(mAllCountText);
        mCountList.add(mPictureCountText);
        mCountList.add(mVideoCountText);

        mController = new BottomBarController(this, mTextList, mCountList,
                mViewPager);
        mAdapter = new MyFileListViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setCurrentItem(0);
        mViewPager.setScrollable(true);
        mController.selectedItem(0);
    }

    /**
     * 刷新文件数量
     */
    public void updateUi() {
        String uid = LocalUserWrapper.getInstance().getLocalUser().getUid();
        mAllCountText.setText(LocalRecordWrapper.getInstance().rawQuery(
                LocalRecord.TYPE_MEDIA_ALL, uid)
                + "");
        mPictureCountText.setText(LocalRecordWrapper.getInstance().rawQuery(
                LocalRecord.TYPE_MEDIA_PHOTO, uid)
                + "");
        mVideoCountText.setText(LocalRecordWrapper.getInstance().rawQuery(
                LocalRecord.TYPE_MEDIA_VIDEO, uid)
                + "");
    }

    public MyFileListViewPagerAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new MyFileListViewPagerAdapter(getSupportFragmentManager());
        }
        return mAdapter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_all:
                mController.selectedItem(0);
                break;
            case R.id.file_picture:
                mController.selectedItem(1);
                break;
            case R.id.file_video:
                mController.selectedItem(2);
                break;
            case R.id.iv_heder_back:
                finish();
                break;
            default:
                break;
        }
    }
}
