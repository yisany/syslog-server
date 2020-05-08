package com.yis.syslog.output;

import com.google.common.base.Throwables;
import com.yis.syslog.OptionsProcessor;
import com.yis.syslog.domain.qlist.OutputQueueList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by yisany on 2020/05/08
 */
public class OutputFactory {

    public static void initOutputInstances(OutputQueueList outputQueueList, List<Sender> allBaseOutputs) {
        // 获取输出对象
        Map<String, Object> outputConfig = OptionsProcessor.getInstance().getOutputConfig();
        OutputThread.initOutputThread(outputConfig, outputQueueList, allBaseOutputs);
    }

    private static List<Sender> getBatchInstance(Map<String, Object> outputConfig) {
        // TODO 构建sender
        return new ArrayList<>();
    }

    private static class OutputThread implements Runnable {

        private static final Logger logger = LogManager.getLogger(OutputThread.class);

        private List<Sender> outputProcessors;
        private BlockingQueue<Map<String, Object>> outputQueue;

        private static ExecutorService outputExecutor;

        public OutputThread(List<Sender> outputProcessors, BlockingQueue<Map<String, Object>> outputQueue) {
            this.outputProcessors = outputProcessors;
            this.outputQueue = outputQueue;
        }

        @Override
        public void run() {
            Map<String, Object> event = null;
            try {
                Thread.sleep(2000);
                while (true) {
                    if (!priorityFail()) {
                        event = this.outputQueue.take();
                        if (event != null) {
                            for (Sender bo : outputProcessors) {
                                bo.process(event);
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.error("output event failed, event={}, e={}", event, Throwables.getStackTraceAsString(e));
            }
        }

        /**
         * 优先处理失败信息
         * @return true: 存在失败信息
         */
        private boolean priorityFail() {
            // TODO 失败信息获取
            return false;
        }

        public static void initOutputThread(Map<String, Object> outputConfig, OutputQueueList outputQueueList, List<Sender> allBaseOutputs) {
            if (outputExecutor == null) {
                outputExecutor = Executors.newFixedThreadPool(outputQueueList.getQueueList().size());
            }
            for (BlockingQueue<Map<String, Object>> queueList : outputQueueList.getQueueList()) {
                List<Sender> baseOutputs = getBatchInstance(outputConfig);
                allBaseOutputs.addAll(baseOutputs);
                outputExecutor.execute(new OutputThread(baseOutputs, queueList));
            }
        }
    }

}
