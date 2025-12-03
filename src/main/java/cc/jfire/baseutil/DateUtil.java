package cc.jfire.baseutil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil
{
    private static final DateTimeFormatter FORMATTER                  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ONLY_DATE_FORMATTER        = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER        = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter COMPACT_DATE_FORMATTER     = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter COMPACT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String dateToString(Date date)
    {
        if (date == null)
        {
            return null;
        }
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime.format(FORMATTER);
    }

    /**
     * 智能解析日期字符串，支持多种常见格式
     * 支持的格式：
     * - yyyy-MM-dd HH:mm:ss
     * - yyyy-MM-dd
     * - yyyyMMdd
     * - yyyyMMddHHmmss
     *
     * @param str 日期字符串
     * @return Date对象，如果字符串为空或无法解析则返回null
     */
    public static Date parse(String str)
    {
        if (str == null || str.trim().isEmpty())
        {
            return null;
        }
        str = str.trim();
        LocalDateTime localDateTime = null;
        try
        {
            // 尝试解析 yyyy-MM-dd HH:mm:ss
            if (str.length() == 19 && str.contains(" "))
            {
                localDateTime = LocalDateTime.parse(str, DATE_TIME_FORMATTER);
            }
            // 尝试解析 yyyy-MM-dd
            else if (str.length() == 10 && str.contains("-"))
            {
                localDateTime = ONLY_DATE_FORMATTER.parse(str, temporal -> {
                    return LocalDateTime.of(temporal.get(java.time.temporal.ChronoField.YEAR), temporal.get(java.time.temporal.ChronoField.MONTH_OF_YEAR), temporal.get(java.time.temporal.ChronoField.DAY_OF_MONTH), 0, 0, 0);
                });
            }
            // 尝试解析 yyyyMMddHHmmss
            else if (str.length() == 14)
            {
                localDateTime = LocalDateTime.parse(str, COMPACT_DATETIME_FORMATTER);
            }
            // 尝试解析 yyyyMMdd
            else if (str.length() == 8)
            {
                localDateTime = COMPACT_DATE_FORMATTER.parse(str, temporal -> {
                    return LocalDateTime.of(temporal.get(java.time.temporal.ChronoField.YEAR), temporal.get(java.time.temporal.ChronoField.MONTH_OF_YEAR), temporal.get(java.time.temporal.ChronoField.DAY_OF_MONTH), 0, 0, 0);
                });
            }
            if (localDateTime != null)
            {
                return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            }
        }
        catch (Exception e)
        {
            // 解析失败，返回null
        }
        return null;
    }

    /**
     * 将Date对象格式化为 yyyy-MM-dd HH:mm:ss 格式的字符串
     *
     * @param date Date对象
     * @return 格式化后的字符串，如果date为null则返回null
     */
    public static String formatDateTime(Date date)
    {
        return dateToString(date);
    }

    /**
     * 获取指定日期当天的开始时间（00:00:00）
     *
     * @param date 日期
     * @return 当天开始时间，如果date为null则返回null
     */
    public static Date beginOfDay(Date date)
    {
        if (date == null)
        {
            return null;
        }
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime beginOfDay = localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        return Date.from(beginOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取指定日期当天的结束时间（23:59:59）
     *
     * @param date 日期
     * @return 当天结束时间，如果date为null则返回null
     */
    public static Date endOfDay(Date date)
    {
        if (date == null)
        {
            return null;
        }
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endOfDay = localDateTime.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }
}
