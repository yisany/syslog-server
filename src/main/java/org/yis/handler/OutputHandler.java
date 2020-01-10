package org.yis.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.comm.Config;
import org.yis.domain.DoubleBufferQueue;
import org.yis.domain.Message;
import org.yis.domain.enums.OutModuleEnum;
import org.yis.export.Export;
import org.yis.export.ipml.FileExport;
import org.yis.export.ipml.KafkaExport;
import org.yis.util.BizException;

import java.util.HashMap;
import java.util.List;
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
                    handler.pushToOut();
                }
            }
        }
    }

    public static ConcurrentHashMap<OutModuleEnum, Export> getOutClass() {
        return handler.outClass;
    }

    /**
     * 输出
     *
     */
    public void pushToOut() {

        Config.executor.execute(() -> {
            DoubleBufferQueue outQueue = DoubleBufferQueue.getInstance();
            try {
                while (true) {
                    List<Message> rList = outQueue.getReadList();
                    while (rList.isEmpty()) {
                        // 设定何时转换read和write队列
                        if (outQueue.getWriteListSize() > 50) {
                            outQueue.swap();
                            rList = outQueue.getReadList();
                        } else {
                            Thread.sleep(1000);
                        }
                    }

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
            } catch (InterruptedException e) {
                logger.error("OutputHandler.pushToOut error, e={}", e);
                throw new BizException("线程休眠失败.");
            }
        });

    }

    /**
     * 输出内容到kafka
     */
    private void toKafka(Message msg) {
        if (!outClass.containsKey(OutModuleEnum.KFAKF)) {
            KafkaExport export = new KafkaExport();
            outClass.put(OutModuleEnum.KFAKF, export);
        }
        outClass.get(OutModuleEnum.KFAKF).send(() -> {
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
        if (!outClass.containsKey(OutModuleEnum.FILE)) {
            FileExport export = new FileExport();
            outClass.put(OutModuleEnum.FILE, export);
        }
        outClass.get(OutModuleEnum.FILE).send(() -> {
            Map<String, Object> event = new HashMap<>();
            event.put("message", msg.getMessage());
            event.put("local_ip", msg.getIp().getHostAddress());
            event.put("local_port", msg.getIp());
            event.put("timestamp", msg.getTimestamp());
            return event;
        });
    }

}
