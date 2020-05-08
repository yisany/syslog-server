package com.yis.syslog.domain;

import lombok.Data;

import java.util.Map;

/**
 * @author by yisany on 2020/05/08
 */
@Data
public class InputOptions {

    private int udp;
    private int tcp;
    private int tls;

    public static InputOptions convert(Map<String, Integer> options) {
        InputOptions input = new InputOptions();
        input.setUdp(options.containsKey("udp") ? options.get("udp") : 8897);
        input.setTcp(options.containsKey("tcp") ? options.get("tcp") : 8898);
        input.setTls(options.containsKey("tls") ? options.get("tls") : 8899);
        return input;
    }

}
