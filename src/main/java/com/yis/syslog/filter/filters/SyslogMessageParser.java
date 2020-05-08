package com.yis.syslog.filter.filters;

import com.alibaba.fastjson.JSON;
import com.github.palindromicity.syslog.SyslogParser;
import com.github.palindromicity.syslog.SyslogParserBuilder;
import com.github.palindromicity.syslog.SyslogSpecification;
import com.yis.syslog.domain.enums.SyslogProtocolEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/03/25
 */
public class SyslogMessageParser {

    private static final Logger logger = LogManager.getLogger(SyslogMessageParser.class);

    private SyslogProtocolEnum proto;

    private SyslogParser parser5424;
    private SyslogParser parser3164;

    public SyslogMessageParser(SyslogProtocolEnum proto) {
        this.proto = proto;
    }

    public void process(Map<String, Object> event) {
        if (!event.containsKey("message")) {
            logger.info("no message found, event={}", event);
            return;
        }
        if (parser5424 == null || parser3164 == null) {
            parser3164 = new SyslogParserBuilder().forSpecification(SyslogSpecification.RFC_3164).build();
            parser5424 = new SyslogParserBuilder().build();
        }
        Map<String, Object> parseMap = new HashMap<>();
        String message = event.get("message").toString();
        // 只有当协议为rfc_3164/rfc5424时, 才需要解析字段
        if (SyslogProtocolEnum.RFC_3164.equals(proto)) {
            parseMap = parser3164.parseLine(message);
        } else if (SyslogProtocolEnum.RFC_5424.equals(proto)) {
            parseMap = parser5424.parseLine(message);
        }
        event.putAll(parseMap);
    }

}
