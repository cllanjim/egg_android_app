package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.entity.CallContactResponse.CallContact;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiaoLei on 2015/11/20.
 */
public class SearchContactAdapter extends BaseAdapter {
    private Context mContext;

    private LayoutInflater inflater;

    private List<CallContact> list = new ArrayList<CallContact>();

    private OnDialOutListener mOnDialOutListener;

    public SearchContactAdapter(Context c) {
        this.mContext = c;
        inflater = LayoutInflater.from(c);

    }

    public void setData(List<CallContact> l) {
        if (list != null) {
            list.clear();
        }
        list.addAll(l);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CallContact getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_searchcontact, null);
            holder.mContactRl = (RelativeLayout) convertView.findViewById(R.id.rl_search_contact_item);
            holder.mNameTv = (TextView) convertView.findViewById(R.id.tv_name);
            holder.mTelTv= (TextView) convertView.findViewById(R.id.tv_tel);
            holder.mHeadRIv= (RoundImageView) convertView.findViewById(R.id.ri_head);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final CallContact call = getItem(position);

        holder.mNameTv.setText(call.nickname);
        holder.mTelTv.setText(call.phonenumber);
        Utils.displayUserIconImageView(holder.mHeadRIv, call.face_image);

        holder.mContactRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnDialOutListener.dialOut(call);
            }
        });

        return convertView;
    }




    class ViewHolder {
        RelativeLayout mContactRl;
        TextView mNameTv,mTelTv;
        RoundImageView mHeadRIv;
    }

    public void setOnDialOutListener(OnDialOutListener listener ){
        this.mOnDialOutListener =listener;
    }

    public interface OnDialOutListener{
        void dialOut(CallContact call);
    }


}
