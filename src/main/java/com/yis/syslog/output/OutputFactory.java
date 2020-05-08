package com.yis.syslog.output;

import com.google.common.base.Throwables;
import com.yis.syslog.OptionsProcessor;
import com.yis.syslog.domain.OutputOptions;
import com.yis.syslog.domain.qlist.OutputQueueList;
import com.yis.syslog.output.impl.file.FileOutput;
import com.yis.syslog.output.impl.kafka.KafkaOutput;
import com.yis.syslog.output.impl.stdout.StdoutOutput;
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

    private static final Logger logger = LogManager.getLogger(OutputFactory.class);

    public static void initOutputInstances(OutputQueueList outputQueueList, List<Output> allBaseOutputs) {
        // 获取输出对象
        Map<String, Object> outputConfig = OptionsProcessor.getInstance().getOutputConfig();
        if (outputConfig != null) {
            OutputThread.initOutputThread(outputConfig, outputQueueList, allBaseOutputs);
        }
    }

    private static List<Output> getBatchInstance(Map<String, Object> outputConfig) {
        List<Output> outputs = new ArrayList<>();
        for (Map.Entry<String, Object> entry : outputConfig.entrySet()) {
            String module = entry.getKey();
            Map<String, Object> conf = (Map<String, Object>) entry.getValue();
            switch (module) {
                case "stdout":
                    logger.info("output module:[stdout] is working");
                    StdoutOutput stdoutSender = new StdoutOutput();
                    outputs.add(stdoutSender);
                    break;
                case "file":
                    logger.info("output module:[file] is working");
                    OutputOptions.FileOption file = new OutputOptions.FileOption();
                    file.convert(conf, file);
                    FileOutput fileSender = new FileOutput(file);
                    outputs.add(fileSender);
                    break;
                case "kafka":
                    logger.info("output module:[kafka] is working");
                    OutputOptions.KafkaOption kafka = new OutputOptions.KafkaOption();
                    kafka.convert(conf, kafka);
                    KafkaOutput kafkaSender = new KafkaOutput(kafka);
                    outputs.add(kafkaSender);
                    break;
                default:
                    break;
            }
        }
        return outputs;
    }

    private static class OutputThread implements Runnable {

        private static final Logger logger = LogManager.getLogger(OutputThread.class);

        private List<Output> outputProcessors;
        private BlockingQueue<Map<String, Object>> outputQueue;

        private static ExecutorService outputExecutor;

        public OutputThread(List<Output> outputProcessors, BlockingQueue<Map<String, Object>> outputQueue) {
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
                            for (Output bo : outputProcessors) {
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
         *
         * @return true: 存在失败信息
         */
        private boolean priorityFail() {
            // TODO 失败信息获取
            return false;
        }

        public static void initOutputThread(Map<String, Object> outputConfig, OutputQueueList outputQueueList, List<Output> allBaseOutputs) {
            if (outputExecutor == null) {
                outputExecutor = Executors.newFixedThreadPool(outputQueueList.getQueueList().size());
            }
            for (BlockingQueue<Map<String, Object>> queueList : outputQueueList.getQueueList()) {
                List<Output> baseOutputs = getBatchInstance(outputConfig);
                allBaseOutputs.addAll(baseOutputs);
                outputExecutor.execute(new OutputThread(baseOutputs, queueList));
            }
        }
    }

}
