package com.hm.demo.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dumingwei on 2017/10/20.
 */
public class DateUtil {

    public static final int SECOND_MILL_SECOND = 1000;
    public static final int MINUTE_MILL_SECOND = 60 * 1000;
    public static final int HOUR_MILL_SECOND = 60 * 60 * 1000;
    public static final int DAY_MILL_SECOND = 24 * 60 * 60 * 1000;
    private static SimpleDateFormat ymdHmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    public static long timeDifference(String nowTime, String endTime) {
        long diff = 0;
        try {
            Date start = ymdHmsFormat.parse(nowTime);
            Date end = ymdHmsFormat.parse(endTime);
            diff = end.getTime() - start.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diff;
    }
}
