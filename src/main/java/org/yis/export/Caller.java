package org.yis.export;

import java.util.Map;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
public interface Caller {

    /**
     * 数据处理
     */
    Map<String, Object> convert();

}
