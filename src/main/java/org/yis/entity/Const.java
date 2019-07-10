package org.yis.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 公用变量
 */
public final class Const {

    public static final int MESSAGE_LIMIT = 10000;

    // syslog协议端口
    public static final int SYSLOG_UDP_PORT = 9897;
    public static final int SYSLOG_TCP_PORT = 9898;
    public static final int SYSLOG_TLS_PORT = 9899;

    private static final Pattern SYSLOG_REGEX = Pattern.compile("^\\S+\\s{1}\\d+\\s\\S+\\s{1,2}\\d+\\s\\d+:\\d+:\\d+\\s.*$");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy MMM dd", Locale.ENGLISH);


    public static Pattern getRegex(){
        return SYSLOG_REGEX;
    }

    public static DateTimeFormatter getDateFormatter(){
        return DATE_TIME_FORMATTER;
    }

}
