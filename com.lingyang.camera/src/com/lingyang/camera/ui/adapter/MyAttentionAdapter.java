package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MyAttentionAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private Handler mHandler;
    private CallBackUpdate mCbUpdate;
    OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //取消关注
            mCbUpdate.update((Camera) v.getTag());
        }
    };
    private List<Camera> list = new ArrayList<Camera>();

    public MyAttentionAdapter(Context c, Handler mHandler, CallBackUpdate mCbUpdate) {
        this.mContext = c;
        this.mHandler = mHandler;
        this.mCbUpdate = mCbUpdate;
        inflater = LayoutInflater.from(c);
    }

    public void setData(List<Camera> l) {
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
    public Camera getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_myattention, null);
            holder.mNameTv = (TextView) convertView.findViewById(R.id.name);
            holder.mCancelAttention = (TextView) convertView.findViewById(R.id.cancel_attention);
            holder.img = (RoundImageView) convertView.findViewById(R.id.iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Camera mAttention = getItem(position);
        holder.mNameTv.setText(mAttention.getCName());
        holder.mCancelAttention.setTag(mAttention);
        holder.mCancelAttention.setOnClickListener(mListener);
        Utils.displayUserIconImageView(holder.img, mAttention.faceimage);
        return convertView;
    }

    public interface CallBackUpdate {
        void update(Camera mCamera);
    }

    class ViewHolder {
        TextView mNameTv;
        TextView mCancelAttention;
        RoundImageView img;
    }
}
