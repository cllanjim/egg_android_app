package com.lingyang.camera.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * 文件名: ContactsAndOnlineUsersAdapter
 * 描    述: [该类的简要描述]
 * 创建人: dushu
 * 创建时间: 2016/3/3
 */
public class ContactsAndOnlineUsersAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;

    public ContactsAndOnlineUsersAdapter(FragmentManager fm,List<Fragment> fragments) {
        super(fm);
        // TODO Auto-generated constructor stub
        mFragments=fragments;
    }

    @Override
    public Fragment getItem(int arg0) {
        // TODO Auto-generated method stub
        return mFragments.get(arg0);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mFragments.size();
    }

}

