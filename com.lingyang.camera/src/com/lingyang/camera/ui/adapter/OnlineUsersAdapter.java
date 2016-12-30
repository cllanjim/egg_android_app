package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.entity.CallContactResponse;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiaoLei on 2015/11/20.
 */
public class OnlineUsersAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater inflater;

    private List<CallContactResponse.CallContact> list = new ArrayList<CallContactResponse.CallContact>();

    public OnlineUsersAdapter(Context c) {
        this.mContext = c;
        inflater = LayoutInflater.from(c);

    }

    public void setData(List<CallContactResponse.CallContact> l) {
        if (list != null) {
            list.clear();
            list.addAll(l);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CallContactResponse.CallContact getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_online_users, null);
            holder.mNameTv = (TextView) convertView.findViewById(R.id.tv_name);
            holder.mTelTv = (TextView) convertView.findViewById(R.id.tv_tel);
            holder.mHeadRIv = (RoundImageView) convertView.findViewById(R.id.ri_head);
            holder.mCallIv = (ImageView) convertView.findViewById(R.id.ib_call);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CallContactResponse.CallContact call = getItem(position);

        holder.mNameTv.setText(call.nickname);
        holder.mTelTv.setText(call.phonenumber);
        Utils.displayUserIconImageView(holder.mHeadRIv, call.face_image);
        return convertView;
    }

    class ViewHolder {
        TextView mNameTv, mTelTv;
        RoundImageView mHeadRIv;
        ImageView mCallIv;
    }

}
