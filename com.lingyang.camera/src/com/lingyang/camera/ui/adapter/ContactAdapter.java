package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.db.bean.LocalCall;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.DateTimeUtil;
import com.lingyang.camera.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiaoLei on 2015/11/20.
 */
public class ContactAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater inflater;

    private List<LocalCall> list = new ArrayList<LocalCall>();

    public ContactAdapter(Context c) {
        this.mContext = c;
        inflater = LayoutInflater.from(c);

    }

    public void setData(List<LocalCall> l) {
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
    public LocalCall getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_contact, null);
            holder.mNameTv = (TextView) convertView.findViewById(R.id.tv_name);
            holder.mTelTv = (TextView) convertView.findViewById(R.id.tv_tel);
            holder.mTimeTv = (TextView) convertView.findViewById(R.id.tv_time);
            holder.mTypeIv = (ImageView) convertView.findViewById(R.id.iv_type);
            holder.mHeadRIv = (RoundImageView) convertView.findViewById(R.id.ri_head);
            holder.mState = convertView.findViewById(R.id.v_user_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LocalCall call = getItem(position);

        holder.mNameTv.setText(call.getmNickName());
        holder.mTelTv.setText(call.getmMobile());
        holder.mTimeTv.setText(DateTimeUtil.timeStampToDateByFormat(call.getmTime(), "MM/dd HH:mm"));
        Utils.displayUserIconImageView(holder.mHeadRIv, call.getmHead());
        holder.mTypeIv.setImageResource(call.getmType() == LocalCall.DIAL_ON ? R.drawable.ic_dial_out : R.drawable.ic_dial_on);
        CLog.v("holder.mState " +call.isOnline());
        holder.mState.setBackgroundResource(call.isOnline() ? R.drawable.shape_circle_user_online : R.drawable.shape_circle_user_offline);
        return convertView;
    }

    class ViewHolder {
        TextView mNameTv, mTelTv, mTimeTv;
        RoundImageView mHeadRIv;
        ImageView mTypeIv;
        View mState;
    }

}
