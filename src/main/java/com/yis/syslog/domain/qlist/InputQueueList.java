package com.yis.syslog.domain.qlist;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author by yisany on 2020/05/08
 */
public class InputQueueList extends QueueList {

    private static final Logger logger = LogManager.getLogger(InputQueueList.class);

    private static ExecutorService executor = Executors.newFixedThreadPool(1);

    private final AtomicInteger pIndex = new AtomicInteger(0);

    private static int SLEEP = 1; // 选择最空队列的间隔时间

    private static InputQueueList inputQueueList;

    private static int releaseSleep = 1000;

    protected AtomicBoolean ato = new AtomicBoolean(false);

    protected ReentrantLock lock = new ReentrantLock();

    public static InputQueueList getInputQueueListInstance(int queueNumber, int queueSize) {
        if (inputQueueList != null) {
            return inputQueueList;
        }
        inputQueueList = new InputQueueList();
        for (int i = 0; i < queueNumber; i++) {
            inputQueueList.queueList.add(new ArrayBlockingQueue<>(queueSize));
        }
        inputQueueList.startElectionIdleQueue();
        return inputQueueList;
    }

    @Override
    public void put(Map<String, Object> message) {
        try {
            if (ato.get()) {
                try {
                    lock.lockInterruptibly();
                    queueList.get(pIndex.get()).put(message);
                } finally {
                    lock.unlock();
                }
            } else {
                queueList.get(pIndex.get()).put(message);
            }
        } catch (InterruptedException e) {
            logger.error("put message error, e={}", Throwables.getStackTraceAsString(e));
        } finally {
            if (ato.get()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void startElectionIdleQueue() {
        executor.submit(new ElectionIdleQueue());
    }

    @Override
    public void ququeRelease() {
        try {
            lock.lockInterruptibly();
            ato.getAndSet(true);
            Thread.sleep(releaseSleep);
            boolean empty = allQueueEmpty();
            while (!empty) {
                empty = allQueueEmpty();
            }
            logger.warn("queue size=="+allQueueSize());
            logger.warn("inputQueueRelease success ...");
        } catch (InterruptedException e) {
            logger.error("inputQueueRelease error:{}",e.getMessage());
        } finally {
            lock.unlock();
        }
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
