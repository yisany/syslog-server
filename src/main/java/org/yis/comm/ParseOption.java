package org.yis.comm;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.util.CliUtil;
import org.yis.util.YamlUtil;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
public class ParseOption {

    private static final Logger logger = LogManager.getLogger(ParseOption.class);

    private ParseOption() { }

    /**
     * 初始化
     * @param args
     */
    public static void initConfig(String[] args) {
        logger.info("Checking config...");
        ParseOption option = new ParseOption();
        // 初始化线程池
        Config.executor = option.initFixedThreadPool(5);
        // 获取配置文件地址
        Map<String, String> comms = CliUtil.parseCli(args, "c");
        // 初始化配置
        Map<String, Object> yamlMap = YamlUtil.parseYaml(comms.get("c"));
        option.parseConfigMap(yamlMap);
    }

    /**
     * 初始化线程池
     * @param nThreads
     * @return
     */
    private ThreadPoolExecutor initFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    /**
     * 解析配置文件
     * @param yamlMap
     */
    private void parseConfigMap(Map<String, Object> yamlMap) {
        Map<String, Integer> ports = (Map<String, Integer>) yamlMap.get("port");
        Map<String, Object> export = (Map<String, Object>) yamlMap.get("export");

        // 端口设置
        Config.UDP_PORT = ports.get("udp");
        Config.TCP_PORT = ports.get("tcp");
        Config.TLS_PORT = ports.get("tls");
        logger.info("Syslog config udp port={}", Config.UDP_PORT);
        logger.info("Syslog config tcp port={}", Config.TCP_PORT);
        logger.info("Syslog config tls port={}", Config.TLS_PORT);

        // 输出端设置
        if (export.containsKey("file") && export.containsKey("kafka")) {
            Config.out = OutModuleEnum.ALL;
            Config.path = String.valueOf(export.get("file"));
            KafkaConfig kafkaConfig = KafkaConfig.convert((Map) export.get("kafka"));
            Config.kafka = kafkaConfig;
        } else if (export.containsKey("file") && !export.containsKey("kafka")) {
            Config.out = OutModuleEnum.FILE;
            Config.path = String.valueOf(export.get("file"));
        } else if (!export.containsKey("file") && export.containsKey("kafka")) {
            Config.out = OutModuleEnum.KFAKF;
            KafkaConfig kafkaConfig = KafkaConfig.convert((Map) export.get("kafka"));
            Config.kafka = kafkaConfig;
        } else if (!export.containsKey("file") && !export.containsKey("kafka")) {
            Config.out = OutModuleEnum.NONE;
        }
        logger.info("Syslog config out module={}", Config.out);
        logger.info("Syslog config file path={}", Config.path);
        logger.info("Syslog config kafka conf={}", Config.kafka);
    }

}
