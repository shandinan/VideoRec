package com.star.video.starrec.utils;

import java.util.Calendar;

/**
 * @author  shandinan
 * @description  时间相关工具类
 * @date  2020年3月19日
 */
public class DateHelper {
    /**
     * 获取系统时间 年月日时分秒
     *
     * @return
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR); // 获取年份
        int month = ca.get(Calendar.MONTH); // 获取月份
        int day = ca.get(Calendar.DATE); // 获取日
        int minute = ca.get(Calendar.MINUTE); // 分
        int hour = ca.get(Calendar.HOUR); // 小时
        int second = ca.get(Calendar.SECOND); // 秒
        String date = "" + year + (month + 1) + day + hour + minute + second;
      //  Log.d(TAG, "date:" + date);
        return date;
    }

}
