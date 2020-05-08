package com.yis.syslog.filter.filters;

import com.alibaba.fastjson.JSON;
import com.github.palindromicity.syslog.SyslogParserBuilder;
import com.github.palindromicity.syslog.SyslogSpecification;

import java.util.Map;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/03/25
 */
public class SyslogMessageParser {

    public void process(Map<String, Object> event) {
        return;
    }

    public static void main(String[] args) {
        com.github.palindromicity.syslog.SyslogParser parser = new SyslogParserBuilder().build();
        Map<String, Object> map = parser.parseLine("<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8");
        System.out.println(JSON.toJSONString(map));


        com.github.palindromicity.syslog.SyslogParser parser3164 = new SyslogParserBuilder().forSpecification(SyslogSpecification.RFC_3164).build();
        Map<String, Object> xcva = parser3164.parseLine("<15>Jul 10 12:00:00 192.168.1.1 SyslogGen MESSAGE ");
        System.out.println(JSON.toJSONString(xcva));
    }

}
