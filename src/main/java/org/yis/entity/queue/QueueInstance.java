package org.yis.entity.queue;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author milu
 * @Description 队列
 * @createTime 2019年08月27日 16:24:00
 */
public class QueueInstance extends Queue{

    private static volatile QueueInstance instance;

    private QueueInstance(){}

    public static QueueInstance getInstance() {
        if (instance == null) {
            synchronized (QueueInstance.class) {
                if (instance == null) {
                    instance = new QueueInstance();
                    queue = new ArrayBlockingQueue(4096);
                }
            }
        }
        return instance;
    }

}
