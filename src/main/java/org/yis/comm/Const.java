package org.yis.comm;

/**
 * 公用变量
 */
public final class Const {

    // 单次写入缓冲区大小: 40k
    public static final Integer BUFFER_SIZE = 1024 * 40;
    // 是否追加写入文件
    public static final boolean FILE_APPEND = true;
    // 系统换行符
    public static final String LINE_BREAK = System.getProperty("line.separator");

//    public static final Pattern SYSLOG_REGEX = Pattern.compile("^\\S+\\s{1}\\d+\\s\\S+\\s{1,2}\\d+\\s\\d+:\\d+:\\d+\\s.*$");
//    public static final Pattern SYSLOG_MAIN_BODY = Pattern.compile("\\d*\\s[A-Z][a-z]{2}\\s\\d*\\s\\d{1,2}:\\d{1,2}:\\d{1,2}.*");
//    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy MM dd", Locale.ENGLISH);
//
//    public static Map<String, Integer> MONTH = new ConcurrentHashMap(){{
//        put("Jan", 1); put("Feb", 2); put("Mar", 3); put("Apr", 4);
//        put("May", 5); put("Jun", 6); put("Jul", 7); put("Aug", 8);
//        put("Sep", 9); put("Oct", 10); put("Nov", 11); put("Dec", 12);
//    }};

}
