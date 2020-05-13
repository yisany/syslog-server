package com.yis.syslog.output.outputs.stdout;

import com.alibaba.fastjson.JSON;
import com.yis.syslog.output.Output;

import java.util.Map;

/**
 * @author by yisany on 2020/05/08
 */
public class StdoutOutput implements Output {

    public StdoutOutput() {
    }

    @Override
    public void prepare() {

    }

    @Override
    public void release() {

    }

    @Override
    public void process(Map<String, Object> event) {
        System.out.println(JSON.toJSONString(event));
    }
}
