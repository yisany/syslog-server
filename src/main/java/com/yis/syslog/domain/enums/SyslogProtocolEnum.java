package com.yis.syslog.domain.enums;

/**
 * @author by yisany on 2020/05/08
 */
public enum  SyslogProtocolEnum {

    RFC_5424, RFC_3164, UNKNOWN;

    public static SyslogProtocolEnum get(String proto) {
        if ("RFC_5424".equals(proto)) {
            return RFC_5424;
        } else if ("RFC_3164".equals(proto)) {
            return RFC_3164;
        } else {
            return UNKNOWN;
        }
    }

}
