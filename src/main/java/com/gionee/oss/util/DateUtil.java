package com.gionee.oss.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by yeqy on 2017/5/27.
 */
public class DateUtil {

    public static String GMTCurremtTimeStamp() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // 设置时区为GMT
        return sdf.format(cd.getTime());
    }

    public static Date getGMTFromString(String gmtTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.parse(gmtTime);
    }

    public static String getDateGMTString(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // 设置时区为GMT
        return sdf.format(date.getTime());
    }

    public static String getCurrentYearMonth() {
        return new SimpleDateFormat("yyyyMM").format(new Date());
    }

    public static String getDateYearMonth(Date date) {
        return new SimpleDateFormat("yyyyMM").format(date);
    }

    public static String getDateYearMonth(Date date, Integer month) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.MONTH, month);
        return new SimpleDateFormat("yyyyMM").format(cd.getTime());
    }

}
