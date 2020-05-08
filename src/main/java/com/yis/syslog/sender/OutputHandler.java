package com.yis.syslog.sender;

import com.yis.syslog.comm.Config;
import com.yis.syslog.domain.DoubleBufferQueue;
import com.yis.syslog.domain.Message;
import com.yis.syslog.domain.enums.OutModuleEnum;
import com.yis.syslog.sender.impl.file.FileSender;
import com.yis.syslog.sender.impl.kafka.KafkaSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.yis.syslog.domain.enums.OutModuleEnum.*;

/**
 * @author milu
 * @Description 导出
 * @createTime 2019年07月10日 17:50:00
 */
public class OutputHandler {

    private static final Logger logger = LogManager.getLogger(OutputHandler.class);

    private ConcurrentHashMap<OutModuleEnum, Sender> outClass;

    private static volatile OutputHandler handler;

    private OutputHandler() {
        this.outClass = new ConcurrentHashMap<>();
    }

    public static void init() {
        if (handler == null) {
            synchronized (OutputHandler.class) {
                if (handler == null) {
                    handler = new OutputHandler();
                    handler.pushToOut();
                }
            }
        }
    }

    public static ConcurrentHashMap<OutModuleEnum, Sender> getOutClass() {
        return handler.outClass;
    }

    /**
     * 输出
     *
     */
    public void pushToOut() {

        Config.executor.execute(() -> {
            while (true) {
                List<Message> rList = DoubleBufferQueue.ready(DoubleBufferQueue.getInstance());
                for (Message msg : rList) {
                    switch (Config.out) {
                        case ALL:
                            handler.toKafka(msg);
                            handler.toFile(msg);
                            break;
                        case FILE:
                            handler.toFile(msg);
                            break;
                        case KFAKF:
                            handler.toKafka(msg);
                            break;
                        case NONE:
                            logger.warn("no output module is set, message={}", msg.getMessage());
                            break;
                    }
                }
            }
        });

    }

    /**
     * 输出内容到kafka
     */
    private void toKafka(Message msg) {
        if (!outClass.containsKey(KFAKF)) {
            KafkaSender export = new KafkaSender();
            outClass.put(KFAKF, export);
        }
        outClass.get(KFAKF).send(() -> {
            Map<String, Object> event = new HashMap<>();
            event.put("message", msg.getMessage());
            event.put("local_ip", msg.getIp().getHostAddress());
            event.put("local_port", msg.getIp());
            event.put("timestamp", msg.getTimestamp());
            return event;
        });
    }

    /**
     * 输出内容到本地文本
     */
    private void toFile(Message msg) {
        if (!outClass.containsKey(FILE)) {
            FileSender export = new FileSender();
            outClass.put(FILE, export);
        }
        outClass.get(FILE).send(() -> {
            Map<String, Object> event = new HashMap<>();
            event.put("message", msg.getMessage());
            event.put("local_ip", msg.getIp().getHostAddress());
            event.put("local_port", msg.getIp());
            event.put("timestamp", msg.getTimestamp());
            return event;
        });
    }

}
