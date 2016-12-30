package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingyang.camera.R;
import com.lingyang.camera.config.Constants;
import com.lingyang.camera.entity.Group;
import com.lingyang.camera.entity.Wifi;
import com.lingyang.camera.util.Utils;

public class CameraApAdapter extends BaseAdapter {

	private Group<Wifi> wifis;
	private Context context;
	private LayoutInflater inflate;
	private String mSSID = "";

	public CameraApAdapter(Context context) {
		this.context = context;
		this.wifis = new Group<Wifi>();
		this.inflate = LayoutInflater.from(context);
	}

	public Group<Wifi> getWifis() {
		return wifis;
	}

	public void setSelAP(int pos) {
		Wifi wifi = getItem(pos);
		this.mSSID = wifi.getSSID();
		notifyDataSetChanged();
	}

	public void setWifis(Group<Wifi> wifis) {
		this.wifis = wifis;
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
		holder.signal.setImageResource(R.drawable.shape_null);
		if (getItem(arg0).getAUTHTYPE() != 4 && mSSID.equals(getItem(arg0).getSSID())) {// 选中
			holder.ssid.setTextColor(context.getResources().getColor(R.color.header_back));
			holder.signal.setImageResource(R.drawable.ic_check);
		} else {
			if (getItem(arg0).getAUTHTYPE() == 4) {
				holder.ssid.setTextColor(context.getResources().getColor(
						R.color.state_offLine_color));
			} else {
				holder.ssid.setTextColor(context.getResources().getColor(R.color.text_dark));
			}

		}

		holder.ssid.setText(getItem(arg0).getSSID());
//		holder.signal.setImageResource(getSignalRes(getItem(arg0)));
		return view;
	}

	class ViewHolder {

		TextView ssid;
		ImageView signal;
	}

	int getSignalRes(Wifi wifi) {
		int resId = R.drawable.ic_wifi_signal_1_light;
		boolean isSecure = Utils.getAuth((wifi)) != Constants.WifiType.WIFI_TYPE_NOPASSWORD;
		int level = wifi.getLevel();
		if (level >= 3) {
			resId = isSecure ? R.drawable.ic_wifi_locked
					: R.drawable.ic_wifi;
		} else if (level == 2) {
			resId = isSecure ? R.drawable.ic_wifi_key_3
					: R.drawable.ic_wifi_3;
		} else if (level == 1) {
			resId = isSecure ? R.drawable.ic_wifi_key_2
					: R.drawable.ic_wifi_2;
		} else {
			resId = isSecure ? R.drawable.ic_wifi_key_1
					: R.drawable.ic_wifi_1;
		}
		return resId;
	}

}
