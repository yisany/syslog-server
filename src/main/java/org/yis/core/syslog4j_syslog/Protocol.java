package org.yis.core.syslog4j_syslog;

import org.productivity.java.syslog4j.server.impl.net.AbstractNetSyslogServerConfig;

/**
 * 协议封装模块
 */
public enum Protocol {

    TCP("tcp", TCPSyslogServerConfig.class),UDP("udp", UDPSyslogServerConfig.class),TLS("tls", TLSSyslogServerConfig.class);

    private String name;
    private Class<? extends AbstractNetSyslogServerConfig> config;


    Protocol(String name, Class<? extends AbstractNetSyslogServerConfig> config) {
        this.name = name;
        this.config = config;
    }

    /**
     * 设定以哪种协议来接收信息
     * @param name 协议名字
     * @return
     */
    public static Protocol name(String name) {
        if (name.equals("tcp")){
            return TCP;
        }else if (name.equals("udp")){
            return UDP;
        }else {
            return TLS;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<? extends AbstractNetSyslogServerConfig> getConfig() {
        return config;
    }

    public void setConfig(Class<? extends AbstractNetSyslogServerConfig> config) {
        this.config = config;
    }
}
