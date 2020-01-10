package org.yis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/08
 */
@Data
@AllArgsConstructor
public class Message {

    private InetAddress ip;
    private int port;
    private String message;
    private String timestamp;

}
