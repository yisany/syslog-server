package org.yis.comm;

import org.yis.domain.enums.OutModuleEnum;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * March, or die.
 *
 * @Description: 配置文件
 * @Created by yisany on 2020/01/07
 */
public class Config {

    private Config() {}

    public static ThreadPoolExecutor executor;

    public static int UDP_PORT;
    public static int TCP_PORT;
    public static int TLS_PORT;

    public static OutModuleEnum out;

    // 文件输出路径
    public static String path;

    // kafka相关设置
    public static KafkaConfig kafka;




}
