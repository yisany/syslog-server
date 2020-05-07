package com.yis.syslog.util;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间处理工具类
 */
public class DateUtil {

    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    /**
     * 日期格式
     **/
    public interface DatePattern {
        String HHMMSS = "HHmmss";
        String HH_MM_SS = "HH:mm:ss";
        String YYYYMMDD = "yyyyMMdd";
        String YYYY_MM_DD = "yyyy-MM-dd";
        String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
        String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
        String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
        String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
        String UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String ES = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    }

    /**
     * 获取日期
     *
     * @return
     */
    public static String getDate() {
        Date date = new Date();
        DateFormat format = DateFormat.getDateInstance();
        return format.format(date);
    }

    /**
     * 获取当前的北京时间
     * @return
     */
    public static String getTimeNow() {
        LocalDateTime arrivalDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.YYYY_MM_DD_HH_MM_SS_SSS);
        return arrivalDate.format(formatter);
    }

    /**
     * 获取当前的北京时间
     * @param pattern 时间格式
     * @return
     */
    public static String getTimeNow(String pattern) {
        LocalDateTime arrivalDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return arrivalDate.format(formatter);
    }

    public static Long getTimestamp10(boolean before, int... offset) {
        return getTimestatmp13(before, offset) / 1000;
    }

    public static Long getTimestatmp13(boolean before, int... offset) {
        long time = System.currentTimeMillis();
        if (offset.length != 0) {
            int o = offset[0];
            if (before) {
                time -= o * 60 * 1000;
            } else {
                time += o * 60 * 1000;
            }
        }
        return time;
    }

    /**
     * 获取某一天的起始时间
     *
     * @param timeMills
     * @return
     */
    public static Date getStartOfDate(Long timeMills) {
        Calendar calendar = Calendar.getInstance();
        if (timeMills != null) {
            calendar.setTimeInMillis(timeMills);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取某一天的截止时间
     *
     * @param timeMills
     * @return
     */
    public static Date getEndOfDate(Long timeMills) {
        Calendar calendar = Calendar.getInstance();
        if (timeMills != null) {
            calendar.setTimeInMillis(timeMills);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * ck查询时间
     */
    public static String getCKTime(Long timestamp) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date(timestamp);
        return format.format(date);
    }

    /**
     * 获取过去第几天的年月日
     */
    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        return format.format(today);
    }

    public static String[] getPastDate(int... past) {
        String[] date = new String[past.length];
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        for (int i = 0; i < past.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past[i]);
            Date aim = calendar.getTime();
            date[i] = format.format(aim);
        }
        return date;
    }

    /**
     * 北京时间戳 -> UTC时间戳
     *
     * @param timestamp 13位时间戳
     * @return
     */
    public static Long CST2UTC(Long timestamp) {
        Date cstDate = new Date(timestamp);
        Calendar cal = Calendar.getInstance();
        cal.setTime(cstDate);
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return cal.getTimeInMillis();
    }

    /**
     * UTC时间戳 -> 北京时间戳
     *
     * @param timestamp 13位时间戳
     */
    public static Long UTC2CST(Long timestamp) {
        Date cstDate = new Date(timestamp);
        Calendar cal = Calendar.getInstance();
        cal.setTime(cstDate);
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, +(zoneOffset + dstOffset));
        return cal.getTimeInMillis();
    }

    /**
     * 13位时间戳 -> yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static String getUTC(Long timestamp) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = new Date(timestamp);
        return format.format(date);
    }

    /**
     * yyyy-MM-dd'T'HH:mm:ss.SSS'Z' -> 13位时间戳
     */
    public static Long getTimeStamp(String time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date date = format.parse(time);
            Calendar ca = Calendar.getInstance();
            ca.setTime(date);
            return ca.getTimeInMillis();
        } catch (ParseException e) {
            System.out.println("date format error: " + e.getMessage());
            return null;
        }
    }

    public static Long convertTime(String pattern, String timestamp) {
        FastDateFormat format = FastDateFormat.getInstance(pattern);
        try {
            Date date = format.parse(timestamp);
            return date.getTime();
        } catch (ParseException e) {
            logger.error("convertTime parse error, error:{}", e);
            return 0L;
        }
    }

}
