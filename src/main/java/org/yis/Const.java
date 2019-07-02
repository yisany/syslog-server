package org.yis;

import java.util.regex.Pattern;

/**
 * 公用变量
 */
public final class Const {

    // syslog协议端口
    public static final int SYSLOG_UDP_PORT=9897;
    public static final int SYSLOG_TCP_PORT=9898;
    public static final int SYSLOG_TLS_PORT=9899;

    // 正则
    private static class syslogRegexInstance{
        private static final Pattern SYSLOG_REGEX = Pattern.compile("^\\S+\\s{1}\\d+\\s\\S+\\s{1,2}\\d+\\s\\d+:\\d+:\\d+\\s.*$");
    }

    public static Pattern getRegex(){
        return syslogRegexInstance.SYSLOG_REGEX;
    }

}
