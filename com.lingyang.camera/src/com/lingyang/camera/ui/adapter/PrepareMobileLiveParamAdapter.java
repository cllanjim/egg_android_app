package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;

import java.util.List;

/**
 * 文件名: PrepareMobileLiveParamAdapter
 * 描    述:手机直播前分辨率和码率的Adapter
 * 创建人: 杜舒
 * 创建时间: 2015/11
 */
public class PrepareMobileLiveParamAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mParams;
    private ChooseParamListener mChooseParamListener;


    @Override
    public int getCount() {
        return mParams == null ? 0 : mParams.size();
    }

    @Override
    public String getItem(int i) {
        if (mParams == null || mParams.isEmpty()) {
            return null;
        }
        return mParams.get(i);
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    public PrepareMobileLiveParamAdapter(Context context, List<String> list) {
        super();
        mContext = context;
        mParams = list;
    }

    private void setViewHolder(ViewHolder holder, View convertView) {
        holder.paramTextView = (TextView) convertView.findViewById(R.id.tv_param_item);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_prepare_mobile_live_param, null);
            setViewHolder(holder, convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.paramTextView.setText(getItem(position));
        View view = convertView.findViewById(R.id.ll_param_item);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChooseParamListener != null) {
                    CLog.v("paramValue: " + getItem(position));
                    mChooseParamListener.choose(getItem(position), position);
                }
            }
        });
        return convertView;
    }

    public void setChooseParamCallBackListener(
            ChooseParamListener chooseParamListener) {
        mChooseParamListener = chooseParamListener;
    }


    static class ViewHolder {
        TextView paramTextView;
    }

    public interface ChooseParamListener {
        void choose(String paramValue, int position);
    }
}
