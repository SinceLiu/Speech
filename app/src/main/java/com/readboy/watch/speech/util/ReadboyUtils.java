package com.readboy.watch.speech.util;

import android.content.Context;
import android.net.ParseException;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by oubin on 2018/4/10.
 */

public final class ReadboyUtils {

    private ReadboyUtils(){

    }


    /**
     * 判断data时间是否在上课禁用时间段内。
     *
     * @param data 上课禁用时间戳
     *             Settings.Global.getString(context.getContentResolver(), "class_disabled");
     */
    public static boolean isTimeEnable(Context context, String data) {
        long time = System.currentTimeMillis();
        SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINESE);
        boolean isEnable = false;
        boolean isWeekEnable = false;
        boolean isTimeEnable = false;
        boolean isSingleTime = false;
        try {
            Date date = new Date(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            long startSetTime = Settings.Global.getLong(context.getContentResolver(), "class_disable_time", 0);
            Date startSetData = new Date(startSetTime);
            boolean isSameDay = isSameDay(date, startSetData);
            int week = (date.getDay() + 6) % 7;
            week = 1 << (6 - week);
            JSONObject jsonObject = new JSONObject(data);
            isEnable = jsonObject.optBoolean("enabled", false);
            String repeatStr = jsonObject.optString("repeat", "0000000");
            int repeatWeek = Integer.parseInt(repeatStr, 2);
            isSingleTime = isSameDay && (repeatWeek == 0);
            isWeekEnable = (week & repeatWeek) != 0;
            JSONArray jsonArray = jsonObject.optJSONArray("time");
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonSun = jsonArray.getJSONObject(i);
                String startTime = jsonSun.optString("start", "00:00");
                String endTime = jsonSun.optString("end", "00:00");
                String nowTime = mDateFormat.format(date);
                Date date1 = mDateFormat.parse(startTime.trim());
                Date date2 = mDateFormat.parse(endTime.trim());
                Date dateNow = mDateFormat.parse(nowTime.trim());
                if (dateNow.getTime() >= date1.getTime() && dateNow.getTime() < date2.getTime()) {
                    isTimeEnable = true;
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isEnable && (isWeekEnable || isSingleTime) && isTimeEnable;
    }

    private static boolean isSameDay(Date day1, Date day2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        String ds1 = sdf.format(day1);
        String ds2 = sdf.format(day2);
        return ds1.equals(ds2);
    }

}
