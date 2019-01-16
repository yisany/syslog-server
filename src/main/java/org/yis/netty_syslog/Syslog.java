package org.yis.netty_syslog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Aim: 程序主入口
 * Date: 18-12-18 17:23
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class Syslog {

    public static final int SYSLOG_PORT = 9898;

    public static final int SYSLOG_UDP_PORT=9897;
    public static final int SYSLOG_TCP_PORT=9898;
    public static final int SYSLOG_TLS_PORT=9899;

    public static final String SYSLOG_PROTOCOL = "udp";

//    public static void main(String[] args) {
//        int port = SYSLOG_PORT;
//        String protocol = SYSLOG_PROTOCOL;
//        System.out.println("protocol: " + protocol);
//        if (args != null && args.length > 0){
//            protocol = args[0];
//        }
//        try {
//            Server server = new Server(port, protocol);
//            server.listen();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args){
        ExecutorService executor = Executors.newFixedThreadPool(5);
        System.out.println("Syslog_Monitor is running");
        Runnable worker1 = new WorkerRunn("udp", SYSLOG_UDP_PORT);
        Runnable worker2 = new WorkerRunn("tcp", SYSLOG_TCP_PORT);
        Runnable worker3 = new WorkerRunn("tls", SYSLOG_TLS_PORT);
        executor.execute(worker1);
        executor.execute(worker2);
        executor.execute(worker3);

    }

    private static class WorkerRunn implements Runnable {

        private String protocol;
        private int port;

        public WorkerRunn(String protocol, int port) {
            this.protocol = protocol;
            this.port = port;
            if (port == 0 || protocol.isEmpty()) {
                System.out.println("usage error: The parameter is error.");
                System.exit(-1);
            }
        }

        @Override
        public void run() {
            try {
                Server server = new Server(port, protocol);
                server.listen();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
