package org.yis.domain;

import java.util.LinkedList;
import java.util.List;

/**
 * March, or die.
 *
 * @Description: 双缓存队列
 * @Created by yisany on 2020/01/08
 */
public class DoubleBufferQueue {

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
    public void swap() {
        synchronized (wList) {
            List<Message> tmp = rList;
            rList = wList;
            wList = tmp;

            wList.clear();
        }
    }


}
