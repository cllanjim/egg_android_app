package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.ui.adapter.RecordDateListAdapter;

import java.util.Date;

/**
 * 文件名: RecordDateSelPopup
 * 描    述:该类负责选择日期播放云存储视频
 * 创建人:廖雷
 * 创建时间: 2015/9
 */
public class RecordDateSelPopup extends PopupWindow {

    RecordDateListAdapter mAdapter;
    Date mDate;
    RecordDateListAdapter.OnSelDateCallback mOnResultCallback;
    private Context mContext;

    public RecordDateSelPopup(Context context, Date date) {
        mDate = date;
        CLog.v("mDate:" + mDate);
        this.mContext = context;
        // 设置可以获得焦点
        this.setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);
        // 设置弹窗的宽度和高度
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(
                new ColorDrawable(mContext.getResources().getColor(R.color.half_transparent)));
        // 设置弹窗的布局界面
        setContentView(LayoutInflater.from(mContext).inflate(R.layout.popup_date, null));
        initView();
    }

    private void initView() {
        mAdapter = new RecordDateListAdapter(mContext, mDate);
        GridView gridView = (GridView) getContentView().findViewById(R.id.gv_date_list);
        gridView.setAdapter(mAdapter);
        mAdapter.setOnSelCallBack(new RecordDateListAdapter.OnSelDateCallback() {
            @Override
            public void onSelDate(String dateString, Date date, String preDateString, Date preDate) {
                CLog.v("onSelDate：onSelDate");
                dismiss();
                if (mOnResultCallback != null) {
                    mOnResultCallback.onSelDate(dateString, date, preDateString, preDate);
                }
            }
        });
    }

    public void setOnResultCallback(RecordDateListAdapter.OnSelDateCallback callback) {
        mOnResultCallback = callback;
    }
}