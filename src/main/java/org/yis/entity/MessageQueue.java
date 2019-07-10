package org.yis.entity;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author milu
 * @Description 消息队列
 * @createTime 2019年07月10日 14:28:00
 */
public class MessageQueue extends LinkedBlockingQueue {

    private static volatile LinkedBlockingQueue<Object> queue;

    private MessageQueue() {
    }

    public static LinkedBlockingQueue getInstance() {
        if (queue == null) {
            synchronized (MessageQueue.class) {
                if (queue == null) {
                    queue = new LinkedBlockingQueue<Object>(Const.MESSAGE_LIMIT);
                }
            }
        }
        return queue;
    }
}