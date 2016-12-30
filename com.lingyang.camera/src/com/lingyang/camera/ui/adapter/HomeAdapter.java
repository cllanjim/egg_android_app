package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.ui.fragment.AttentionFragment;
import com.lingyang.camera.ui.fragment.PublicCameraFragment;

import java.util.ArrayList;

public class HomeAdapter extends FragmentPagerAdapter {

    FragmentManager fm;
    private String[] mTabs;
    public ArrayList<String> mTagList;
    private Context mContext;
    private ArrayList<Fragment> mFragments;

    public HomeAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.fm = fm;
        this.mContext = context;
        this.mTabs = mContext.getResources().getStringArray(R.array.TabsHome);
        this.mTagList = new ArrayList<String>(mTabs.length);
        this.mFragments = new ArrayList<Fragment>();
        this.mFragments.add(new AttentionFragment());
//        this.mFragments.add(new PublicCameraFragment());
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
        if (mFragments != null) {
            return mFragments.size();
        }
        return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs[position];
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
    }
    
    
    public void refreshPublicFragment(int position,boolean isUnBind,Camera c,boolean isGroup,int type){
    	if(position >= mTagList.size()||mTagList.get(position).equals("")){
    		return;
    	}
    	 Fragment fragment = fm.findFragmentByTag(mTagList.get(position));
    	 try {
			PublicCameraFragment f=(PublicCameraFragment) fragment;
             if(isGroup){
                 f.groupRefresh(type);
             }else{
			f.refreshFromSet(isUnBind, c);
             }
		} catch (Exception e) {
			// TODO: handle exception
		}
    }

    private String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }


}
