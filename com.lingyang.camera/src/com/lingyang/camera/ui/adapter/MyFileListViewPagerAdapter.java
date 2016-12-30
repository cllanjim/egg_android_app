package com.lingyang.camera.ui.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lingyang.camera.config.Const;
import com.lingyang.camera.db.bean.LocalRecord;
import com.lingyang.camera.ui.fragment.MineMyFileFragment;

import java.util.ArrayList;
import java.util.List;

public class MyFileListViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList = new ArrayList<Fragment>();

    public MyFileListViewPagerAdapter(FragmentManager fm) {
        super(fm);
        MineMyFileFragment allFragment = new MineMyFileFragment();
        Bundle b = new Bundle();
        b.putInt(Const.IntentKeyConst.KEY_FILE_TYPE, LocalRecord.TYPE_MEDIA_ALL);
        allFragment.setArguments(b);

        MineMyFileFragment allFragment2 = new MineMyFileFragment();
        Bundle b2 = new Bundle();
        b2.putInt(Const.IntentKeyConst.KEY_FILE_TYPE, LocalRecord.TYPE_MEDIA_PHOTO);
        allFragment2.setArguments(b2);

        MineMyFileFragment allFragment3 = new MineMyFileFragment();
        Bundle b3 = new Bundle();
        b3.putInt(Const.IntentKeyConst.KEY_FILE_TYPE, LocalRecord.TYPE_MEDIA_VIDEO);
        allFragment3.setArguments(b3);

        mFragmentList.add(allFragment);
        mFragmentList.add(allFragment2);
        mFragmentList.add(allFragment3);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void refresh(int position) {
        MineMyFileFragment allFileFm = (MineMyFileFragment) mFragmentList
                .get(LocalRecord.TYPE_MEDIA_ALL);
        MineMyFileFragment photoFileFm = (MineMyFileFragment) mFragmentList
                .get(LocalRecord.TYPE_MEDIA_PHOTO);
        MineMyFileFragment videoFileFm = (MineMyFileFragment) mFragmentList
                .get(LocalRecord.TYPE_MEDIA_VIDEO);
        switch (position) {
            case LocalRecord.TYPE_MEDIA_ALL:
                photoFileFm.deleteUpdate();
                videoFileFm.deleteUpdate();
                break;
            case LocalRecord.TYPE_MEDIA_PHOTO:
                allFileFm.deleteUpdate();
                videoFileFm.deleteUpdate();
                break;
            case LocalRecord.TYPE_MEDIA_VIDEO:
                photoFileFm.deleteUpdate();
                allFileFm.deleteUpdate();
                break;
            default:
                break;
        }
    }

}
