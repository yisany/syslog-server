package org.yis.entity.queue;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author milu
 * @Description 失败消息队列
 * @createTime 2019年08月29日 10:13:00
 */
public class FailedQueueInstance extends Queue {

    private static volatile FailedQueueInstance instance;

    private FailedQueueInstance(){}

    public static FailedQueueInstance getInstance() {
        if (instance == null) {
            synchronized (QueueInstance.class) {
                if (instance == null) {
                    instance = new FailedQueueInstance();
                    queue = new ArrayBlockingQueue(4096);
                }
            }
        }
        return instance;
    }


}
