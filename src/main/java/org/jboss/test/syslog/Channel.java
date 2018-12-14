package org.jboss.test.syslog;

/**
 * Aim: 静态提供BaseInput入口
 * Date: 2018/11/22 22:17
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class Channel {

    private volatile static Server server;

    public static Server getServer() {
        if (server != null){
            return server;
        }else {
            throw new NullPointerException("syslog server is null");
        }
    }

    public static void setServer(Server server) {
        Channel.server = server;
    }
}
