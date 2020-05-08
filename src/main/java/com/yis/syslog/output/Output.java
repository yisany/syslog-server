package com.yis.syslog.output;

import java.util.Map;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
public interface Output {

    /**
     * 初始化
     */
    void prepare();

    /**
     * 关闭
     */
    void release();

    /**
     * 处理
     * @param event
     */
    void process(Map<String, Object> event);

}
