package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.config.Const;
import com.lingyang.camera.entity.CameraResponse.MyCameras.Camera;
import com.lingyang.camera.framework.MgmtClassFactory;
import com.lingyang.camera.mgmt.SetCameraToPublicMgmt;
import com.lingyang.camera.preferences.MyPreference;
import com.lingyang.camera.util.Utils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class LiveAdapter extends BaseAdapter {

    private Context mContext;
    /**
     * 加载图片结果回调
     */
    ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            //图片加载失败，调用本地封面截图重新加载
            Camera camera = (Camera) view.getTag();
            CLog.v("displaycover-fail--" + camera);
            displayLocalCache((ImageView)view, camera);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    };
    @NonNull
    private void displayLocalCache(ImageView view, Camera camera) {
        MyPreference myPreference = MyPreference.getInstance();
        myPreference.setPreferenceKey(camera.cid);
        Utils.displayCaptureView(view, Const.FILE_PROTOCOL
                + myPreference.getString(mContext, MyPreference.SNAPSHOT_OF_COVER, ""));
    }
    private LayoutInflater inflater;
    private List<Camera> mList = new ArrayList<Camera>();
    private SetCameraToPublicMgmt mPublicMgmt;
    private UpdateUiListener mUpdateListener;
    private CameraClickCallBackListener mCameraOnClickCallBackListener;

    public LiveAdapter(Context context) {
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        mPublicMgmt = (SetCameraToPublicMgmt) MgmtClassFactory.getInstance()
                .getMgmtClass(SetCameraToPublicMgmt.class);
    }

    public void setUpdateListener(UpdateUiListener mUpdateListener) {
        this.mUpdateListener = mUpdateListener;
    }

    public void setData(List<Camera> l) {
        if (l != null) {
            mList.clear();
            mList.addAll(l);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
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
            convertView = inflater.inflate(R.layout.item_live, null);
            holder.mCNameText = (TextView) convertView.findViewById(R.id.tv_cname);
            //holder.mNickNameText = (TextView) convertView.findViewById(R.id.tv_nickname);
            holder.mIsPublicIb = (ImageButton) convertView.findViewById(R.id.ib_isPublic);
            holder.mCoverImg = (ImageView) convertView.findViewById(R.id.img_cover);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Camera camera = (Camera) getItem(position);
        holder.mCoverImg.setTag(camera);
        holder.mCNameText.setText(camera.cname);
        if (!TextUtils.isEmpty(camera.cover_url)) {
            Utils.displayCaptureView(holder.mCoverImg,
                    camera.cover_url, mImageLoadingListener);
        }else {
            displayLocalCache(holder.mCoverImg, camera);
        }
        if (mCameraOnClickCallBackListener != null) {
            holder.mCoverImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = camera.cover_url;
                    mCameraOnClickCallBackListener.viewLive(camera, url);
                }
            });
        }
        //type  1公众  0私有
        holder.mIsPublicIb.setImageResource(camera.type == Camera.TYPE_PUBLIC ? R.drawable.button_set_live_pressed : R.drawable.button_set_live_nor);

        holder.mIsPublicIb.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mUpdateListener.update(camera, camera.type == Camera.TYPE_PUBLIC ? false : true);
            }
        });

        return convertView;
    }

    public void setCameraOnClickCallBackListener(
            CameraClickCallBackListener cameraOnClickCallBackListener) {
        mCameraOnClickCallBackListener = cameraOnClickCallBackListener;
    }

    public interface UpdateUiListener {
        void update(Camera c, boolean isToPublic);
    }

    public interface CameraClickCallBackListener {
        void viewLive(Camera camera, String url);
    }

    class ViewHolder {
        TextView mCNameText, mNickNameText;
        ImageButton mIsPublicIb;
        ImageView mCoverImg;

    }

}
