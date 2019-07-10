package org.yis.core.syslog4j_syslog;

import org.productivity.java.syslog4j.server.SyslogServerConfigIF;

/**
 * Aim:  config配置
 * Date: 2018/11/23 9:48
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class ConfigBuilder {

    public static Builder newBuilder(Protocol protocol) {
        return new Builder(protocol);
    }

    public static class Builder {

        private SyslogServerConfigIF config;

        public Builder(Protocol protocol) {
            try {
                config = protocol.getConfig().newInstance();
            } catch (InstantiationException e) {
                System.out.println("ConfigBuilder config Instance failed " + config);
                //System.exit(-1);
            } catch (IllegalAccessException e) {
                System.out.println("You hava No Access " + config);
                //System.exit(-1);
            }
        }

        public Builder setPort(int port) {
            config.setPort(port);
            return this;
        }

        public SyslogServerConfigIF build() {
            return config;
        }

    }

}
