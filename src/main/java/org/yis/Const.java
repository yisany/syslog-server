package org.yis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 公用变量
 */
public final class Const {
    // syslog协议端口
    public static final int SYSLOG_UDP_PORT=9897;
    public static final int SYSLOG_TCP_PORT=9898;
    public static final int SYSLOG_TLS_PORT=9899;

    // 实例
    private static class syslogInstance{
        private static final Pattern SYSLOG_REGEX = Pattern.compile("^\\S+\\s{1}\\d+\\s\\S+\\s{1,2}\\d+\\s\\d+:\\d+:\\d+\\s.*$");
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy MMM dd", Locale.ENGLISH);
    }

    public static Pattern getRegex(){
        return syslogInstance.SYSLOG_REGEX;
    }

    public static DateTimeFormatter getDateFormatter(){
        return syslogInstance.DATE_TIME_FORMATTER;
    }

}
