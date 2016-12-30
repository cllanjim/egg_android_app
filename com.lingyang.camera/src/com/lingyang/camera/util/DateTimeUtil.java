package com.lingyang.camera.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author 波
 *
 */
public class DateTimeUtil {

	public static String timeStampToDate(Long timestamp) {
		// SimpleDateFormat format = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
		String d = format.format(new Date(timestamp * 1000));
		// Date date = null;
		// try {
		// date = format.parse(d);
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
		return d;
	}

	public static String timeStampToDateLong(Long timestamp) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		String d = format.format(new Date(timestamp * 1000));
		// Date date = null;
		// try {
		// date = format.parse(d);
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
		return d;
	}

	/**
	 * @param timestamp 以秒为单位的时间戳
	 * @param formatString
	 * @return
	 */
	public static String timeStampToDateByFormat(Long timestamp, String formatString) {
		// SimpleDateFormat format = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat format = new SimpleDateFormat(formatString);
		String d = format.format(new Date(timestamp * 1000));
		return d;
	}

	public static String dateToStringByFormat(Date date, String formatString) {
		SimpleDateFormat format = new SimpleDateFormat(formatString);
		return format.format(date);
	}

	/**
	 * 计算两个日期之间相差的天数
	 *
	 * @param smdate
	 *            较小的时间
	 * @param bdate
	 *            较大的时间
	 * @return 相差天数
	 * @throws ParseException
	 */
	public static int daysBetween(Date smdate, Date bdate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			smdate = sdf.parse(sdf.format(smdate));
			bdate = sdf.parse(sdf.format(bdate));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);
		return Integer.parseInt(String.valueOf(between_days));
	}

	/**
	 * 字符串的日期格式的计算
	 *
	 * @param smdate
	 * @param bdate
	 * @return
	 * @throws ParseException
	 */
	public static int daysBetween(String smdate, String bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(smdate));
		long time1 = cal.getTimeInMillis();
		cal.setTime(sdf.parse(bdate));
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	public static String formatTime(long l) {
		Date date = new Date(l);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return sdf.format(date);
	}
	public static String formatTimeToMS(long l) {
		Date date = new Date(l);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ", Locale.getDefault());
		return sdf.format(date);
	}
}
