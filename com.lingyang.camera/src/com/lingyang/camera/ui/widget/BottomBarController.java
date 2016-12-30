package com.lingyang.camera.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.sdk.util.CLog;

import java.util.ArrayList;
import java.util.List;


public class BottomBarController {

	private Context mContext;

	
	private List<TextView> mTextList=new ArrayList<TextView>();

	private List<TextView> mCountList=new ArrayList<TextView>();

	private ViewPager mViewPager;

	private int priSelectedPos = -1;

	private final int normalColor=R.color.text_dark;

	private final int selectedColor=R.color.mine_myfile_selected;

	public BottomBarController(Context mContext, List<TextView> mTextList,List<TextView> mCountList, ViewPager viewPager) {
		this.mContext = mContext;

		this.mTextList=mTextList;
		this.mCountList=mCountList;
		
		if(mTextList.size() != 3)
			try {
				throw new Exception("only support three item");
			} catch (Exception e) {
				e.printStackTrace();
			}
		mViewPager = viewPager;
	}

	public void selectedItem(int pos){
		if(pos == priSelectedPos)		
			return;
		
		configSelectedItem(pos);
		configNormalItem(priSelectedPos);
		priSelectedPos = pos;
		mViewPager.setCurrentItem(pos);
	}
	
	public void configSelectedItem(int pos){
		CLog.v("configSelectedItem -pos:"+pos);
		mTextList.get(pos).setTextColor(mContext.getResources().getColor(selectedColor));
		mCountList.get(pos).setTextColor(mContext.getResources().getColor(selectedColor));
		
	}
	
	public void configNormalItem(int pos){
		if(pos == -1)
			return;
		CLog.v("configNormalItem -pos:"+pos);
		mTextList.get(pos).setTextColor(mContext.getResources().getColor(normalColor));
		mCountList.get(pos).setTextColor(mContext.getResources().getColor(normalColor));
	}
}
