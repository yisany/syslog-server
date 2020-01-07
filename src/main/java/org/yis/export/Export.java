package org.yis.export;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
public interface Export {

    void init();

    void send(Caller caller);

}
