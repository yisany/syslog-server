package org.yis.entity.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author milu
 * @Description
 * @createTime 2019年11月25日 11:33:00
 */
public abstract class Queue {

    private Logger logger = LoggerFactory.getLogger(Queue.class);


    protected static volatile ArrayBlockingQueue<Map<String, Object>> queue;

    protected static final AtomicBoolean ATO = new AtomicBoolean(false);
    protected static volatile ReentrantLock lock = new ReentrantLock();

    public int getQueueSize() {
        return queue.size();
    }

    /**
     * 插入数据
     * @param event
     */
    public void put(Map<String, Object> event) {
        try {
            if (ATO.get()) {
                try {
                    lock.lockInterruptibly();
                    queue.put(event);
                } finally {
                    lock.unlock();
                }
            } else {
                queue.put(event);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("put message error={}", e);
        }
    }

    /**
     * 取出数据
     * @return
     */
    public Map<String, Object> take() {
        Map<String, Object> event;
        try {
            if (ATO.get()) {
                try {
                    lock.lockInterruptibly();
                    event = queue.take();
                } finally {
                    lock.unlock();
                }
            } else {
                event = queue.take();
            }
        } catch (InterruptedException e) {
            logger.error("take message error={}", e);
            event = null;
        }
        return event;
    }

}
