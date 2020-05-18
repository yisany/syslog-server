package com.yis.syslog.output;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.yis.syslog.OptionsProcessor;
import com.yis.syslog.comm.InstanceFactory;
import com.yis.syslog.domain.OutputOptions;
import com.yis.syslog.domain.qlist.OutputQueueList;
import com.yis.syslog.output.outputs.file.FileOutput;
import com.yis.syslog.output.outputs.kafka.KafkaOutput;
import com.yis.syslog.output.outputs.stdout.StdoutOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by yisany on 2020/05/08
 */
public class OutputFactory extends InstanceFactory {

    private static final Logger logger = LogManager.getLogger(OutputFactory.class);

    private final static String PLUGINTYPE = "output";

    private static Map<String,Class<?>> outputsClassLoader = Maps.newConcurrentMap();

    public static void initOutputInstances(OutputQueueList outputQueueList, List<Output> allBaseOutputs) {
        // 获取输出对象
        Map<String, Object> outputConfig = OptionsProcessor.getInstance().getOutputConfig();
        if (outputConfig != null) {
            OutputThread.initOutputThread(outputConfig, outputQueueList, allBaseOutputs);
        }
    }

    private static Output getInstance(Map<String, Object> outputConfig, Class<?> outputClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> ctor = outputClass.getConstructor();

        Output baseOutput = (Output) ctor.newInstance();
        configInstance(baseOutput,outputConfig);//设置非static field
        baseOutput.prepare();

        return baseOutput;
    }

    private static List<Output> getBatchInstance(Map<String, Object> outputConfig) {
        List<Output> outputs = new ArrayList<>();

        int index = 0;
        try {
            for (Map.Entry<String, Object> outputEntry : outputConfig.entrySet()) {
                String outputType = outputEntry.getKey();
                Map oConfig = (Map) outputEntry.getValue();
                String className = getClassName(outputType, PLUGINTYPE);
                String key = String.format("%s%d", className, index++);
                Class<?> oClass = outputsClassLoader.get(key);
                if (oClass == null) {
                    oClass = getPluginClass(className);
                    outputsClassLoader.put(key, oClass);
                }
                if (oConfig == null) {
                    oConfig = Maps.newLinkedHashMap();
                }
                outputs.add(getInstance(oConfig, oClass));
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            logger.error("output factory getBatchInstance error, e={}", Throwables.getStackTraceAsString(e));
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
                        if (event != null && !event.isEmpty()) {
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
