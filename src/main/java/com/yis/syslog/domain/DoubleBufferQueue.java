package com.yis.syslog.domain;

import com.yis.syslog.util.BizException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * March, or die.
 *
 * @Description: 双缓存队列
 * @Created by yisany on 2020/01/08
 */
public class DoubleBufferQueue {

    private final static Logger logger = LogManager.getLogger(DoubleBufferQueue.class);

    private List<Message> rList = new LinkedList<>();
    private List<Message> wList = new LinkedList<>();

    private static volatile DoubleBufferQueue queue;

    private DoubleBufferQueue() {
    }

    public static DoubleBufferQueue getInstance() {
        if (queue == null) {
            synchronized (DoubleBufferQueue.class) {
                if (queue == null) {
                    queue = new DoubleBufferQueue();
                }
            }
        }
        return queue;
    }

    public void push(Message msg) {
        synchronized (wList) {
            wList.add(msg);
        }
    }

    public int getWriteListSize() {
        synchronized (wList) {
            return wList.size();
        }
    }

    public List<Message> getReadList() {
        return rList;
    }

    /**
     * 交换读写队列
     */
    private void swap() {
        synchronized (wList) {
            List<Message> tmp = rList;
            rList = wList;
            wList = tmp;

            wList.clear();
        }
    }

    /**
     * 查询是否需要交换队列
     * @param outQueue
     */
    public static List<Message> ready(DoubleBufferQueue outQueue) {
        try {
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
            return rList;
        } catch (InterruptedException e) {
            logger.error("DoubleBufferQueue ready error, e={}", e);
            throw new BizException("交换队列操作失败");
        }

    }


}
