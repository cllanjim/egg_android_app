package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.entity.ShareUserResponse.ShareUser;
import com.lingyang.camera.ui.widget.RoundImageView;
import com.lingyang.camera.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.List;

public class ShareUserAdapter extends BaseAdapter {

    private Context mContext;
    private List<ShareUser> mShareUsersList;
    private RemoveOnCallBackListener mShareUserOnClickCallBackListener;
    private DisplayImageOptions options;

    public List<ShareUser> getShareUsersList() {
        return mShareUsersList;
    }

    @Override
    public int getCount() {
        return mShareUsersList.size();
    }

    @Override
    public ShareUser getItem(int i) {
        if (mShareUsersList == null || mShareUsersList.isEmpty()) {
            return null;
        }
        return mShareUsersList.get(i);
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    public ShareUserAdapter(Context context, List<ShareUser> list) {
        super();
        mShareUsersList = list;
        mContext = context;
    }

    public void refresh(List<ShareUser> list) {
        mShareUsersList.clear();
        mShareUsersList.addAll(list);
        notifyDataSetChanged();
    }

    public boolean removeShareUser(String nickName) {
        boolean flg = false;
        for (ShareUser temp : mShareUsersList) {
            if (nickName.equals(temp.nickname)) {
                flg = mShareUsersList.remove(temp);
                break;
            }
        }
        notifyDataSetChanged();
        return flg;
    }

    public void addShareUser(ShareUser shareUser) {
        mShareUsersList.add(0, shareUser);
        notifyDataSetChanged();
    }

    private void setViewHolder(ViewHolder holder, View convertView) {
        holder.userIconImageView = (RoundImageView) convertView.findViewById(R.id.iv_share_user_icon);
        holder.userNameTextView = (TextView) convertView.findViewById(R.id.tv_share_nickname);
        holder.delImageView = (ImageView) convertView.findViewById(R.id.iv_share_remove);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        final ShareUser shareUser = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.shareuser_list_item, null);
            setViewHolder(holder, convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mShareUserOnClickCallBackListener != null) {
            holder.delImageView.setTag(shareUser);
            holder.delImageView.setOnClickListener(mOnClickListener);
        }
        holder.userNameTextView.setText(shareUser.nickname);
        Utils.displayUserIconImageView(holder.userIconImageView, shareUser.faceimage);
        return convertView;
    }

    public void setShareUserCallBackListener(
            RemoveOnCallBackListener cameraOnClickCallBackListener) {
        mShareUserOnClickCallBackListener = cameraOnClickCallBackListener;
    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ShareUser shareUser = (ShareUser) v.getTag();
            switch (v.getId()) {
                case R.id.iv_share_remove:
                    if (mShareUserOnClickCallBackListener != null) {
                        CLog.v("shareUser" + shareUser);
                        mShareUserOnClickCallBackListener.remove(shareUser);
                    }
                    break;

                default:
                    break;
            }

        }
    };

    static class ViewHolder {
        RoundImageView userIconImageView;
        TextView userNameTextView;
        ImageView delImageView;
    }

    public interface RemoveOnCallBackListener {

        void remove(ShareUser shareUser);

    }
}
