package com.yis.syslog.domain.qlist;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * @author by yisany on 2020/05/08
 */
public abstract class QueueList {

    protected List<BlockingQueue<Map<String, Object>>> queueList = Lists.newArrayList();

    /**
     * 置入数据
     * @param message
     */
    public abstract void put(Map<String, Object> message);

    /**
     * 寻找最空闲队列
     */
    public abstract void startElectionIdleQueue();

    /**
     * 关闭队列
     */
    public abstract void ququeRelease();

    /**
     * 判断队列集合是否为空
     * @return
     */
    public boolean allQueueEmpty() {
        boolean result = true;
        for (BlockingQueue<Map<String, Object>> queue : queueList) {
            result = result && queue.isEmpty();
        }
        return result;
    }

    /**
     * 获取队列集合的总长度
     * @return
     */
    public int allQueueSize() {
        int size = 0;
        for (BlockingQueue<Map<String, Object>> queue : queueList) {
            size += queue.size();
        }
        return size;
    }

    public List<BlockingQueue<Map<String, Object>>> getQueueList() {
        return queueList;
    }

    protected int electionIndex(int size) {
        int id = 0;
        int sz = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            int ssz = queueList.get(i).size();
            if (ssz < sz) {
                sz = ssz;
                id = i;
            }
        }
        return id;
    }
}
