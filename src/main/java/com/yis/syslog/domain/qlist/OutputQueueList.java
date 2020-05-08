package com.yis.syslog.domain.qlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author by yisany on 2020/05/08
 */
public class OutputQueueList extends QueueList {

    private static Logger logger = LogManager.getLogger(OutputQueueList.class);

    private static ExecutorService executor = Executors.newFixedThreadPool(1);

    private final AtomicInteger pIndex = new AtomicInteger(0);

    private static int SLEEP = 1;//queue选取的间隔时间

    private static OutputQueueList outPutQueueList;

    public static OutputQueueList getOutPutQueueListInstance(int queueNumber, int queueSize) {
        if (outPutQueueList != null) {
            return outPutQueueList;
        }
        outPutQueueList = new OutputQueueList();
        for (int i = 0; i < queueNumber; i++) {
            outPutQueueList.queueList.add(new ArrayBlockingQueue<>(queueSize));
        }
        outPutQueueList.startElectionIdleQueue();
        return outPutQueueList;
    }

    @Override
    public void put(Map<String, Object> message) {
        try {
            queueList.get(pIndex.get()).put(message);
        } catch (InterruptedException e) {
            logger.error("put output queue message error:{}",e.getCause());
        }
    }

    @Override
    public void startElectionIdleQueue() {
        executor.submit(new ElectionIdleQueue());
    }

    @Override
    public void ququeRelease() {
        boolean empty =allQueueEmpty();
        while(!empty){
            empty =allQueueEmpty();
        }
        logger.warn("out queue size=="+allQueueSize());
        logger.warn("outputQueueRelease success ...");
    }

    class ElectionIdleQueue implements Runnable {

        @Override
        public void run() {
            int size = queueList.size();
            while (true) {
                try {
                    if (size > 0) {
                        int id = electionIndex(size);
                        pIndex.getAndSet(id);
                    }
                    Thread.sleep(SLEEP);
                } catch (InterruptedException e) {
                    logger.error("input electionIdleQueue is error:{}",e.getCause());
                }
            }
        }
    }
}
