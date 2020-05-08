package com.yis.syslog.input;

/**
 * @author by yisany on 2020/05/08
 */
public interface Input {

    void prepare();

    void emit();

    void release();

}
