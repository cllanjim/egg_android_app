package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.entity.GetCameraSetResponse;
import com.lingyang.camera.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名: PopupResolution
 * 描    述:该类负责选择不同分辨率看直播
 * 创建人:廖雷
 * 创建时间: 2015/11
 */
public class PopupResolution extends PopupWindow {

    private Context mContext;
    private View v;
    private OnResolutionListener mGroupListener;
    private List<TextView> mTvList = new ArrayList<TextView>();
    private int[] mPressedBgs = new int[]{R.drawable.shape_resolution_up_bg,
            R.drawable.shape_resolution_middle_bg, R.drawable.shape_resolution_down_bg};
    private int mPriSelectedPos = 0;
    View.OnClickListener mOnclickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_hd:
                    selectedItem(0);
                    mGroupListener.callBack(GetCameraSetResponse.CameraSet.RATE_HD);
                    dismiss();
                    break;
                case R.id.tv_sd:
                    selectedItem(1);
                    mGroupListener.callBack(GetCameraSetResponse.CameraSet.RATE_SD);
                    dismiss();
                    break;
                case R.id.tv_fluent:
                    selectedItem(2);
                    mGroupListener.callBack(GetCameraSetResponse.CameraSet.RATE_FLUENT);
                    dismiss();
                    break;
            }
        }
    };

    public PopupResolution(Context context) {
        mContext = context;
        // 设置可以获得焦点
        this.setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);
        // 设置弹窗的宽度和高度
        setWidth(Utils.dip2px(mContext, 90));
        setHeight(Utils.dip2px(mContext, 150));
        this.setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.transparent)));
        // 设置弹窗的布局界面
        v = LayoutInflater.from(mContext).inflate(R.layout.popup_resolution, null);
        setContentView(v);
        initView();
    }

    private void initView() {
        TextView hdTv = (TextView) v.findViewById(R.id.tv_hd);
        TextView sdTv = (TextView) v.findViewById(R.id.tv_sd);
        TextView fluentTv = (TextView) v.findViewById(R.id.tv_fluent);

        mTvList.add(hdTv);
        mTvList.add(sdTv);
        mTvList.add(fluentTv);

        hdTv.setOnClickListener(mOnclickListener);
        sdTv.setOnClickListener(mOnclickListener);
        fluentTv.setOnClickListener(mOnclickListener);
    }

    public void selectedItem(int pos) {
        if (pos == mPriSelectedPos)
            return;
        configSelectedItem(pos);
        setDrawableImg(mTvList.get(pos), R.drawable.choice);
        configNormalItem(mPriSelectedPos);
        setDrawableImg(mTvList.get(mPriSelectedPos), -1);
        mPriSelectedPos = pos;
    }

    private void configSelectedItem(int pos) {
//        mTvList.get(pos).setBackgroundResource(mPressedBgs[pos]);
    }

    private void setDrawableImg(TextView tv, int src) {
        if (src == -1) {
            tv.setCompoundDrawables(null, null, null, null);
            return;
        }
        Drawable drawable = mContext.getResources().getDrawable(src);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tv.setCompoundDrawables(drawable, null, null, null);
    }

    private void configNormalItem(int pos) {
        mTvList.get(pos).setBackgroundResource(R.color.transparent);
    }

    /**
     * 选择清晰度
     */
    public void setOnResolutionListener(OnResolutionListener l) {
        mGroupListener = l;
    }

    public interface OnResolutionListener {
        void callBack(int type);
    }
}