package com.yis.syslog.sender;

import java.util.Map;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
public interface Sender {

    /**
     * 初始化
     */
    void init();

    /**
     * 发送消息
     * @param caller 消息处理逻辑
     */
    void send(Caller caller);

    /**
     * 关闭
     */
    void release();

    interface Caller {

        /**
         * 数据处理
         */
        Map<String, Object> convert();

    }

}