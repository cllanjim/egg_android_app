package com.lingyang.camera.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingyang.base.utils.CLog;
import com.lingyang.camera.R;
import com.lingyang.camera.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecordDateListAdapter extends BaseAdapter {

    private static final int TOTAL_DAYS = 7;
    Calendar mCalendar;
    OnSelDateCallback mOnSelDateCallback;
    private List<Date> mDateList;
    private Context mContext;


    public RecordDateListAdapter(Context context, Date date) {
        super();
        mContext = context;
        init(date);
    }

    private void init(Date initDate) {
        mDateList = new ArrayList<Date>();
        mCalendar = Calendar.getInstance();
        Date today = new Date(mCalendar.get(Calendar.YEAR) - 1900, mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH));
        mCalendar.setTime(today);
        if (DateTimeUtil.daysBetween(initDate, today) != 0) {
            mDateList.add(today);
        }
        for (int i = 1; i < TOTAL_DAYS; i++) {
            mCalendar.add(Calendar.DAY_OF_YEAR, -1);
            if (DateTimeUtil.daysBetween(initDate, mCalendar.getTime()) != 0) {
                mDateList.add(mCalendar.getTime());
                CLog.v("calendar.getTime()-" + mCalendar.getTime());
            }
        }
    }

    public void setOnSelCallBack(OnSelDateCallback onSelCallBack) {
        mOnSelDateCallback = onSelCallBack;
    }

    @Override
    public int getCount() {
        return mDateList.size();
    }

    @Override
    public Date getItem(int position) {
        return mDateList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        final Date date = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.record_date_list_item,
                    null);
            holder.dateDayTextView = (TextView) convertView.findViewById(R.id.tv_record_date_day);
            holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.rl_record_layout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (DateTimeUtil.daysBetween(date, new Date()) == 0) {
            holder.dateDayTextView.setText(R.string.date_today);
        } else if (DateTimeUtil.daysBetween(date, new Date()) == 1) {
            holder.dateDayTextView.setText(R.string.date_lastday);
        } else {
            holder.dateDayTextView.setText(DateTimeUtil.dateToStringByFormat(date, "MM月dd日"));
        }
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSelDateCallback != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    Date predate;
                    String preDateString = "";
                    if (DateTimeUtil.daysBetween(date, new Date()) == TOTAL_DAYS - 1) {
                        predate = null;
                    } else {
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                        predate = calendar.getTime();
                    }
                    if (predate != null) {
                        if (DateTimeUtil.daysBetween(predate, new Date()) == 1) {
                            preDateString = mContext.getResources().getString(R.string.date_lastday);
                        } else {
                            preDateString = DateTimeUtil.dateToStringByFormat(predate, "MM月dd日");
                        }
                    }
                    CLog.v("today:" + holder.dateDayTextView.getText().toString() + " -date:"
                            + date + " -preDateString:" + preDateString + " -predate:" + predate);
                    mOnSelDateCallback.onSelDate(holder.dateDayTextView.getText().toString(),
                            date, preDateString, predate);

                }
            }
        });
        return convertView;
    }

    public interface OnSelDateCallback {
        void onSelDate(String dateString, Date date, String preDateString, Date preDate);
    }

    static class ViewHolder {
        RelativeLayout relativeLayout;
        TextView dateDayTextView;
    }

}
