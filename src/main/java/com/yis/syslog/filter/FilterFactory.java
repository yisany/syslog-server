package com.yis.syslog.filter;

import com.google.common.base.Throwables;
import com.yis.syslog.OptionsProcessor;
import com.yis.syslog.domain.qlist.InputQueueList;
import com.yis.syslog.domain.qlist.OutputQueueList;
import com.yis.syslog.filter.filters.SyslogMessageParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by yisany on 2020/05/08
 */
public class FilterFactory{

    public static void initFilterInstances(InputQueueList inputQueueList, OutputQueueList outputQueueList) {
        FilterThread.setOutputQueueList(outputQueueList);
        FilterThread.initFilterThread(inputQueueList);
    }

    private static class FilterThread implements Runnable {

        private static Logger logger = LogManager.getLogger(FilterThread.class);

        private SyslogMessageParser parser;
        private BlockingQueue<Map<String, Object>> inputQueue;

        private static ExecutorService filterExecutor;
        private static OutputQueueList outputQueueList;

        public FilterThread(SyslogMessageParser parser, BlockingQueue<Map<String, Object>> inputQueue) {
            this.parser = parser;
            this.inputQueue = inputQueue;
        }

        public static void setOutputQueueList(OutputQueueList outputQueueList) {
            FilterThread.outputQueueList = outputQueueList;
        }

        @Override
        public void run() {
            A: while (true) {
                Map<String, Object> event = null;
                try {
                    event = this.inputQueue.take();
                    if (parser != null) {
                        if (event == null || event.size() == 0) {
                            continue A;
                        }
                        parser.process(event);
                    }
                    if (event != null) {
                        outputQueueList.put(event);
                    }
                } catch (InterruptedException e) {
                    logger.error("filter event failed, event={}, e={}", event, Throwables.getStackTraceAsString(e));
                }
            }
        }

        public static void initFilterThread(InputQueueList inputQueueList) {
            if (filterExecutor == null) {
                filterExecutor = Executors.newFixedThreadPool(inputQueueList.getQueueList().size());
            }
            for (BlockingQueue<Map<String, Object>> queueList : inputQueueList.getQueueList()) {
                SyslogMessageParser parser = new SyslogMessageParser(OptionsProcessor.getInstance().getFilterConfig());
                filterExecutor.execute(new FilterThread(parser, queueList));
            }
        }
    }
}
