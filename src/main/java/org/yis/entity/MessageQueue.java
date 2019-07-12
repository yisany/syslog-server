package org.yis.entity;

import com.alibaba.fastjson.JSON;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author milu
 * @Description 消息队列
 * @createTime 2019年07月10日 14:28:00
 */
public class MessageQueue extends LinkedBlockingQueue {

    private static volatile LinkedBlockingQueue<Message> queue;

    private MessageQueue() {
    }

    @Override
    public void put(Object o) throws InterruptedException {
        System.out.println("queue插入一条消息：" + JSON.toJSONString(o));
        super.put(o);
    }

    public static LinkedBlockingQueue getInstance() {
        if (queue == null) {
            synchronized (MessageQueue.class) {
                if (queue == null) {
                    queue = new LinkedBlockingQueue<Message>(Const.MESSAGE_LIMIT);
                }
            }
        }
        return queue;
    }
}
