package com.vng.teg.logtool.common.util;

import com.vng.teg.logtool.common.Constants;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: sonnt4
 * Date: 11/13/13
 * Time: 3:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimestampUtil {
    private static final SimpleDateFormat dateHourFormat = new SimpleDateFormat(Constants.YYYYMMDDHH);

    public static long getTimestampInMillisecond(String text) throws ParseException {
        long timestamp = -1;
        if (StringUtils.isNotBlank(text)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.YYYYMMDD);
            SimpleDateFormat dateHourFormat = new SimpleDateFormat(Constants.YYYYMMDDHH);
            if (text.length() == 8) {
                timestamp = dateFormat.parse(text).getTime();
            } else if (text.length() == 10) {
                timestamp = dateHourFormat.parse(text).getTime();
            }
        }
        return timestamp;
    }

    public static long getTimestampInMillisecond(String text, String format) throws ParseException {
        SimpleDateFormat dFormat = new SimpleDateFormat(format);
        return dFormat.parse(text).getTime();
    }
    public static String getDate(Date date, String format, int offset) throws ParseException {
        String rs = null;
        SimpleDateFormat dFormat = new SimpleDateFormat(format);
        long timestamp = date.getTime();
        timestamp += offset * 24 * 60 * 60 * 1000;
        rs = dFormat.format(new Date(timestamp));
        return rs;
    }

    public static String getDate(String text, String format, int offset) throws ParseException {
        String rs = null;
        if (StringUtils.isNotBlank(text)) {
            SimpleDateFormat dFormat = new SimpleDateFormat(format);
            long timestamp = dFormat.parse(text).getTime();
            timestamp += offset * 24 * 60 * 60 * 1000;
            rs = dFormat.format(new Date(timestamp));
        }
        return rs;
    }

    public static String getDateByFormat(long milliseconds, DateFormat dateFormat) throws ParseException {
        return dateFormat.format(new Date(milliseconds));
    }

    public static List<String> getYYYYMMDDHHFormat(String startTime, String endTime) throws ParseException {
        SimpleDateFormat dateHourFormat = new SimpleDateFormat(Constants.YYYYMMDDHH);
        List<String> rs = new ArrayList<String>();
        if (StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return rs;
        }
        long start = getTimestampInMillisecond(startTime);
        long end = getTimestampInMillisecond(endTime);
        if (endTime.length() == 8) {
            end += 24 * 60 * 60 * 1000;
        } else if (endTime.length() == 10) {
            end += 60 * 60 * 1000;
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON));
        cal.setTimeInMillis(start);
        long timestamp = cal.getTimeInMillis();
        while (timestamp < end) {
            rs.add(getDateByFormat(timestamp, dateHourFormat));
            cal.add(Calendar.HOUR, 1);
            timestamp = cal.getTimeInMillis();
        }
        return rs;
    }

    public static List<String> getDateRangeByFormat(String startTime, String endTime, String format) throws ParseException {
        List<String> rs = new ArrayList<String>();
        if (StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return rs;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        long start = getTimestampInMillisecond(startTime);
        long end = getTimestampInMillisecond(endTime);
        if (endTime.length() == 8) {
            end += 24 * 60 * 60 * 1000;
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON));
        cal.setTimeInMillis(start);
        long timestamp = cal.getTimeInMillis();
        while (timestamp < end) {
            rs.add(dateFormat.format(cal.getTime()));
            cal.add(Calendar.DATE, 1);
            timestamp = cal.getTimeInMillis();
        }
        return rs;
    }
    public static List<String> getDateRangeByFormat(String date, int different, String format) throws ParseException {
        List<String> rs = new ArrayList<String>();
        if (StringUtils.isBlank(date)) {
            return rs;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        long start = getTimestampInMillisecond(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON));
        cal.setTimeInMillis(start);
        if(different < 0){
            cal.add(Calendar.DATE, different);
        }
        rs.add(dateFormat.format(cal.getTime()));

        for(int i = 0; i < Math.abs(different); i++){
            cal.add(Calendar.DATE, 1);
            rs.add(dateFormat.format(cal.getTime()));
        }

        return rs;
    }
    public static String getDateByFormat(String date, int different, String format) throws ParseException {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        long start = getTimestampInMillisecond(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON));
        cal.setTimeInMillis(start);
        cal.add(Calendar.DATE, different);
        return dateFormat.format(cal.getTime());
    }

    @Deprecated
    public static String getCurrentYYYYMMDDHHFormat() {
        TimeZone timeZone = TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON);
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        return dateHourFormat.format(cal.getTime());
    }

    public static String getCurrentDateByFormat(DateFormat dateFormat) {
        TimeZone timeZone = TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON);
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        return dateFormat.format(cal.getTime());
    }

    public static long toBeginDateTimestamp(long timestampInMillisecond) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON));
        cal.setTimeInMillis(timestampInMillisecond);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        return cal.getTimeInMillis();
    }
    public static long getBeginDateTimestampWithOffset(int offsetDate) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.DATE, offsetDate);
        return cal.getTimeInMillis();
    }
    public static long getBeginHourTimestampWithOffset(int offsetHour) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON));
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.HOUR_OF_DAY, offsetHour);
        return cal.getTimeInMillis();
    }

    public static long getTimestampFromReversedPerHour(long reversedTimestampPerHour) {
        return (Long.MAX_VALUE - reversedTimestampPerHour) * 1000 * 3600;
    }

    public static long getTimestampFromReversedPerDate(long reversedTimestampPerDate) {
        return toBeginDateTimestamp((Long.MAX_VALUE - reversedTimestampPerDate + 1) * 1000 * 3600 * 24);
    }

    public static long getReversedTimestampPerHour(long timestampInMillisecond) {
        return Long.MAX_VALUE - timestampInMillisecond / (1000 * 3600);
    }

    public static long getReversedTimestampPerDate(long timestampInMillisecond) {
        return Long.MAX_VALUE - toBeginDateTimestamp(timestampInMillisecond) / (1000 * 3600 * 24);
    }

    public static long getReversedTimestampPerSecond(long timestampInMillisecond) {
        return Long.MAX_VALUE - timestampInMillisecond / 1000;
    }

    public static String getTimeByMinuteSecondFormat(long millis) {
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public static String getTimeByHourMinuteSecondFormat(long millis) {
        return String.format("%d hour, %d min, %d sec",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public static String convertMysqlDateTime2ISO(String datetime, DateFormat mysqlFormat, DateFormat isoFormat) throws Exception {
        mysqlFormat.setTimeZone(TimeZone.getTimeZone(Constants.TIMEZONE_SAIGON));
        Date date = mysqlFormat.parse(datetime);
        String output = isoFormat.format(date);
        return output.substring(0, 22) + ":" + output.substring(22);
    }

    public static void main(String[] args) throws Exception {
        SimpleDateFormat datetimeFormat = new SimpleDateFormat(Constants.MYSQL_DATETIME_FORMAT);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        System.out.println(convertMysqlDateTime2ISO("2014-03-01 22:13:42", datetimeFormat, df));
        System.exit(0);
    }
}
