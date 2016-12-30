package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingyang.camera.R;

import java.util.ArrayList;

/**
 * 文件名: UpdateHistoryAdapter
 * 描    述: [该类的简要描述]
 * 创建人: 杜舒
 * 创建时间: 2016/4/28
 */
public class UpdateHistoryAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<String> list = new ArrayList<String>();

    public UpdateHistoryAdapter(Context c) {
        mInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
        if (list.size() == 0) {
            return "";
        }
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_simple_text, null);
            holder.mTextView = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mTextView.setText(list.get(position));
        return convertView;
    }

    public void setData(ArrayList<String> data) {
        list.clear();
        list.addAll(data);
    }

    class ViewHolder {
        TextView mTextView;
    }
}
