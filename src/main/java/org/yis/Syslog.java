package org.yis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.core.netty_syslog.Server;
import org.yis.entity.Const;
import org.yis.export.Export;
import org.yis.util.PropsUtil;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Aim: 程序主入口
 * Author milu
 * Version: v1.0.0
 */
public class Syslog {

    private static Logger logger = LogManager.getLogger(Syslog.class);

    public static void main(String[] args){
        ExecutorService executor = Executors.newFixedThreadPool(5);
        logger.info("Syslog System Check...");
        logger.info("Checking config...");
        Map<String, Object> props = PropsUtil.getProps();
        int pattern = checkConfig(props);
        if (pattern != 0) {
            Runnable export = new ExportThread(pattern, props);
            executor.execute(export);
        }
        Runnable worker1 = new MonitorThread("udp", Const.SYSLOG_UDP_PORT);
        Runnable worker2 = new MonitorThread("tcp", Const.SYSLOG_TCP_PORT);
        Runnable worker3 = new MonitorThread("tls", Const.SYSLOG_TLS_PORT);
        executor.execute(worker1);
        executor.execute(worker2);
        executor.execute(worker3);

    }

    private static int checkConfig(Map<String, Object> props) {
        if (props.containsKey("export")) {
            if ("true".equals(props.get("export").toString())) {
                if (!props.containsKey("pattern")) {
                    return -1;
                }
                String pattern = (String) props.get("pattern");
                logger.info("Pattern={}", pattern);
                switch (pattern) {
                    case "file":
                        return 1;
                    case "kafka":
                        return 2;
                    case "es":
                        return 3;
                    default:
                        return -1;
                }
            }
        }
        return 0;
    }

    private static class MonitorThread implements Runnable {

        private String protocol;
        private int port;

        public MonitorThread(String protocol, int port) {
            this.protocol = protocol;
            this.port = port;
            if (port == 0 || protocol.isEmpty()) {
                logger.error("usage error: The parameter is error.");
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

    private static class ExportThread implements Runnable {

        private int key;
        private Map<String, Object> props;

        public ExportThread(int key, Map<String, Object> props) {
            this.key = key;
            this.props = props;
            if (key == 0 || key == -1) {
                logger.error("config error: Errors in parameter setting for export");
                System.exit(-1);
            }
        }

        @Override
        public void run() {
            Export export = new Export(key, props);
            export.listen();
        }
    }

}
