package org.yis.netty_syslog;

/**
 * Aim: 程序主入口
 * Date: 18-12-18 17:23
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class Syslog {

    public static final int SYSLOG_PORT = 9898;

    public static final String SYSLOG_PROTOCOL = "tcp";

    public static void main(String[] args) {
        int port = SYSLOG_PORT;
        String protocol = SYSLOG_PROTOCOL;
        System.out.println(protocol);
        if (args != null && args.length > 0){
            protocol = args[0];
        }

        try {
            Server server = new Server(port, protocol);
            server.listen();
        } catch (Exception e){
            e.printStackTrace();
        }


    }

}
