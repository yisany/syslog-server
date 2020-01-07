package org.yis.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.comm.Config;
import org.yis.comm.OutModuleEnum;
import org.yis.export.Export;
import org.yis.export.ipml.FileExport;
import org.yis.export.ipml.KafkaExport;
import org.yis.util.DateUtil;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author milu
 * @Description 导出
 * @createTime 2019年07月10日 17:50:00
 */
public class OutputHandler {

    private static final Logger logger = LogManager.getLogger(OutputHandler.class);

    private ConcurrentHashMap<OutModuleEnum, Export> outClass;

    private static volatile OutputHandler handler;

    private OutputHandler() {
        this.outClass = new ConcurrentHashMap<>();
    }

    public static void init() {
        if (handler == null) {
            synchronized (OutputHandler.class) {
                if (handler == null) {
                    handler = new OutputHandler();
                }
            }
        }
    }

    /**
     * 输出
     *
     * @param message
     */
    public static void pushToOut(String message, InetAddress ip, int port) {
        String localIp = ip.getHostAddress();
        switch (Config.out) {
            case ALL:
                handler.toKafka(message, localIp, port);
                handler.toFile(message, localIp, port);
                break;
            case FILE:
                handler.toFile(message, localIp, port);
                break;
            case KFAKF:
                handler.toKafka(message, localIp, port);
                break;
            case NONE:
                logger.warn("no output module is set, message={}", message);
                break;
        }
    }

    /**
     * 输出内容到kafka
     */
    private void toKafka(String message, String localIp, int port) {
        if (!outClass.containsKey(OutModuleEnum.KFAKF)) {
            KafkaExport export = new KafkaExport();
            outClass.put(OutModuleEnum.KFAKF, export);
        }
        outClass.get(OutModuleEnum.KFAKF).send(() -> {
            Map<String, Object> event = new HashMap<>();
            event.put("message", message);
            event.put("local_ip", localIp);
            event.put("local_port", port);
            event.put("timestamp", DateUtil.getUTC(System.currentTimeMillis()));
            return event;
        });
    }

    /**
     * 输出内容到本地文本
     */
    private void toFile(String message, String localIp, int port) {
        if (!outClass.containsKey(OutModuleEnum.FILE)) {
            FileExport export = new FileExport();
            outClass.put(OutModuleEnum.FILE, export);
        }
        outClass.get(OutModuleEnum.FILE).send(() -> {
            Map<String, Object> event = new HashMap<>();
            event.put("message", message);
            event.put("local_ip", localIp);
            event.put("local_port", port);
            event.put("timestamp", DateUtil.getUTC(System.currentTimeMillis()));
            return event;
        });
    }

}
