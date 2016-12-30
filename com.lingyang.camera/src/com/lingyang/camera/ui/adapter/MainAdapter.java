package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.ui.fragment.HomeFragment;
import com.lingyang.camera.ui.fragment.MineFragment;
import com.lingyang.camera.ui.widget.IconPagerAdapter;

import java.util.ArrayList;

public class MainAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

    FragmentManager fm;
    private String[] mTabs;
    public ArrayList<String> mTagList;
    private Context mContext;
    private ArrayList<Fragment> mFragments;
    private final int[] ICONS = new int[]{R.drawable.group_home, R.drawable.group_live,
            R.drawable.group_me,R.drawable.group_me};//group_phone

    public MainAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.fm = fm;
        this.mContext = context;
        this.mTabs = mContext.getResources().getStringArray(R.array.Tabs);
        this.mTagList = new ArrayList<String>(mTabs.length);
        this.mFragments = new ArrayList<Fragment>();
        this.mFragments.add(new HomeFragment());
        this.mFragments.add(new Fragment());
        this.mFragments.add(new Fragment());
        this.mFragments.add(new MineFragment());
        for (int i = 0; i < mTabs.length; i++) {
            mTagList.add("");
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (mFragments != null) {
            return mFragments.get(position);
        }
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        mTagList.set(position, makeFragmentName(container.getId(), position));
        return super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        if (mTabs != null) {
            return mTabs.length;
        }
        return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs[position];
    }

    @Override
    public int getIconResId(int index) {
        return ICONS[index];
    }

    public void cancelDialog() {
        if (getItem(0) != null && getItem(0) instanceof MineFragment) {

        }
    }



    /*
     *
     * @param isShowToast 是否提示
     *
     * @param compel 强制刷新
     */
    public void refresh(int position, boolean isShowToast, boolean compel) {
        if (position >= mTagList.size()) {
            CLog.e("refresh Fragment :" + position + ">mTagList.size()" + mTagList.size());
            return;
        }
        if (mTagList.get(position).equals("")) {
            CLog.e("refresh Fragment :Tag is null  position=" + position);
            return;
        }
        Fragment fragment = fm.findFragmentByTag(mTagList.get(position));
        if (fragment == null) {
            CLog.e("refresh Fragment :findFragmentByTag is null position=" + position + "tag="
                    + mTagList.get(position));
            return;
        }
        if (fragment instanceof HomeFragment) {
            HomeFragment attentionFragment = (HomeFragment) fragment;
//			attentionFragment.refreshList();
        } else if (fragment instanceof MineFragment) {
            // ((RecordFragment) fragment).refresh();
        } else {
            CLog.e("refresh Fragment :unknown fragment ,position =" + position + "tag="
                    + mTagList.get(position));
        }
        CLog.v("refresh Fragment " + fragment + "  position:" + position);
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }
}
