package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.lingyang.camera.R;
import com.lingyang.camera.ui.adapter.PrepareMobileLiveParamAdapter;
import com.lingyang.camera.util.Utils;

/**
 * 文件名: PrepareMobileLiveParamPopup
 * 描    述:手机直播前选择参数的PopupWindow
 * 创建人: 杜舒
 * 创建时间: 2015/11
 */
public class PrepareMobileLiveParamPopup extends PopupWindow {
    private View v;
    private ListView mLvParam;

    public PrepareMobileLiveParamPopup(Context context) {
        super(context);
        // 设置可以获得焦点
        this.setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);
        // 设置弹窗的宽度和高度
        setWidth(Utils.getScreenWidth(context) / 3);
        setHeight(LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.shape_prepare_mobile_live_param_pop));
        // 设置弹窗的布局界面
        v = LayoutInflater.from(context).inflate(R.layout.popup_prepare_mobile_live_param, null);
        setContentView(v);
        initView();
    }

    public void setAdapter(PrepareMobileLiveParamAdapter adapter) {
        mLvParam.setAdapter(adapter);
    }

    private void initView() {
        mLvParam = (ListView) v.findViewById(R.id.lv_prepare_mobile_live_param);
    }
}
