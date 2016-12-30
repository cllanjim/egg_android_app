package com.lingyang.camera.framework;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment{
	
	protected FragmentActivity mActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity=getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = createAndInitView(inflater, container, savedInstanceState);
		return view;
	}
	
	protected abstract View createAndInitView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState);
	
}
