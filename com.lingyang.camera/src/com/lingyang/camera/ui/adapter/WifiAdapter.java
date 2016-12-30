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
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.Group;
import com.lingyang.camera.entity.Wifi;
import com.lingyang.camera.util.Utils;
import com.lingyang.camera.util.WifiAdmin;

public class WifiAdapter extends BaseAdapter {

    private Group<Wifi> wifis;
    private Context context;
    private LayoutInflater inflate;
    private String mSSID = "";

    public WifiAdapter(Context context) {
        this.context = context;
        this.wifis = new Group<Wifi>();
        this.inflate = LayoutInflater.from(context);
    }

    public Group<Wifi> getWifis() {
        return wifis;
    }

    public void setWifis(Group<Wifi> wifis) {
        this.wifis = wifis;
        WifiAdmin mWifiAdmin = new WifiAdmin(context);
        if (mWifiAdmin.getSSID() != null && !mWifiAdmin.getSSID().equals("NULL")) {
            mSSID = mWifiAdmin.getSSID().trim().replace("\"", "");
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (wifis != null)
            return wifis.size();
        return 0;
    }

    @Override
    public Wifi getItem(int arg0) {
        if (wifis != null && arg0 < getCount())
            return wifis.get(arg0);
        return new Wifi();
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int arg0, View view, ViewGroup arg2) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflate.inflate(R.layout.wifi_list_adapter, null);
            holder.ssid = (TextView) view.findViewById(R.id.wifi_ssid);
            holder.signal = (ImageView) view.findViewById(R.id.wifi_signal);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (getItem(arg0).getAUTHTYPE() != 4 && mSSID.equals(getItem(arg0).getSSID())) {// 选中
            holder.ssid.setTextColor(context.getResources().getColor(R.color.guide_green));
        } else {
            if (getItem(arg0).getAUTHTYPE() == 4) {
                holder.ssid.setTextColor(context.getResources().getColor(
                        R.color.state_offLine_color));
            } else {
                holder.ssid.setTextColor(context.getResources().getColor(R.color.text_dark));
            }

        }

        holder.ssid.setText(getItem(arg0).getSSID());
        holder.signal.setImageResource(getSignalRes(getItem(arg0)));
        return view;
    }

    class ViewHolder {

        TextView ssid;
        ImageView signal;
    }

    int getSignalRes(Wifi wifi) {
        int resId;
        boolean isSecure = Utils.getAuth((wifi)) != Constants.WifiType.WIFI_TYPE_NOPASSWORD;
        CLog.v("wifi.getSSID():" + wifi.getSSID() + "-wifi.getAUTH():" + wifi.getAUTH()
                + "Utils.getAuth((wifi)):" + Utils.getAuth((wifi)));
        int level = wifi.getLevel();
        if (level >= 3) {
            resId = isSecure ? R.drawable.ic_wifi_lock_signal_4_light
                    : R.drawable.ic_wifi_signal_4_light;
        } else if (level == 2) {
            resId = isSecure ? R.drawable.ic_wifi_lock_signal_3_light
                    : R.drawable.ic_wifi_signal_3_light;
        } else if (level == 1) {
            resId = isSecure ? R.drawable.ic_wifi_lock_signal_2_light
                    : R.drawable.ic_wifi_signal_2_light;
        } else {
            resId = isSecure ? R.drawable.ic_wifi_lock_signal_1_light
                    : R.drawable.ic_wifi_signal_1_light;
        }
        return resId;
    }

}
