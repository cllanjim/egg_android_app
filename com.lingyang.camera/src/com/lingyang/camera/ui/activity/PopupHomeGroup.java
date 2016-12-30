package com.lingyang.camera.ui.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件名: PopupHomeGroup
 * 描    述:该类负责筛选全部、摄像头、手机直播
 * 创建人:廖雷
 * 创建时间: 2015/11
 */
public class PopupHomeGroup extends PopupWindow {
    public static final int GROUP_ALL = 0;
    public static final int GROUP_CAMERA = 1;
    public static final int GROUP_MOBILE = 2;
    private Context mContext;
    private View v;
    private OnGroupListener mGroupListener;
    private List<TextView> mTvList = new ArrayList<TextView>();
    private int[] mNorBgs = new int[]{R.drawable.ic_all_nor, R.drawable.ic_camear_nor,
            R.drawable.ic_iphone_nor};
    private int[] mPressedBgs = new int[]{R.drawable.ic_all_pressed, R.drawable.ic_camear_pressed,
            R.drawable.ic_iphone_pressed};
    private int mPriSelectedPos = 0;
    View.OnClickListener mOnclickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_all:
                    selectedItem(0);
                    mGroupListener.callBack(GROUP_ALL);
                    dismiss();
                    break;
                case R.id.tv_camera:
                    selectedItem(1);
                    mGroupListener.callBack(GROUP_CAMERA);
                    dismiss();
                    break;
                case R.id.tv_mobile:
                    selectedItem(2);
                    mGroupListener.callBack(GROUP_MOBILE);
                    dismiss();
                    break;
            }
        }
    };

    public PopupHomeGroup(Context context) {
        mContext = context;
        // 设置可以获得焦点
        this.setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);

        // 设置弹窗的宽度和高度
        setWidth(context.getResources().getDisplayMetrics().widthPixels);
        setHeight(Utils.dip2px(mContext, 64));

        this.setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.white)));
        // 设置弹窗的布局界面
        v = LayoutInflater.from(mContext).inflate(R.layout.popup_group, null);
        setContentView(v);
        initView();
    }

    private void initView() {
        TextView allTv = (TextView) v.findViewById(R.id.tv_all);
        TextView cameraTv = (TextView) v.findViewById(R.id.tv_camera);
        TextView mobileTv = (TextView) v.findViewById(R.id.tv_mobile);

        mTvList.add(allTv);
        mTvList.add(cameraTv);
        mTvList.add(mobileTv);

        allTv.setOnClickListener(mOnclickListener);
        cameraTv.setOnClickListener(mOnclickListener);
        mobileTv.setOnClickListener(mOnclickListener);
    }

    private void selectedItem(int pos) {
        if (pos == mPriSelectedPos)
            return;
        configSelectedItem(pos);
        setDrawableImg(mTvList.get(pos), mPressedBgs[pos]);
        configNormalItem(mPriSelectedPos);
        setDrawableImg(mTvList.get(mPriSelectedPos), mNorBgs[mPriSelectedPos]);
        mPriSelectedPos = pos;
    }

    private void configSelectedItem(int pos) {
        int selectedColor = R.color.orange;
        mTvList.get(pos).setTextColor(mContext.getResources().getColor(selectedColor));
    }

    private void setDrawableImg(TextView tv, int src) {
        Drawable drawable = mContext.getResources().getDrawable(src);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tv.setCompoundDrawables(null, drawable, null, null);
    }

    private void configNormalItem(int pos) {
        int normalColor = R.color.line;
        mTvList.get(pos).setTextColor(mContext.getResources().getColor(normalColor));
    }

    public void setOnGroupListener(OnGroupListener l) {
        mGroupListener = l;
    }


    public interface OnGroupListener {
        void callBack(int type);
    }

}