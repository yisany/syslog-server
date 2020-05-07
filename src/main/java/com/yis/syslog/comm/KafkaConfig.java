package com.yis.syslog.comm;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
@Data
public class KafkaConfig {

    private String bootstrapServers;
    private String zkAddress;
    private String topics;
    private Map<String, String> producerSettings = new HashMap<>();

    public static KafkaConfig convert(Map input) {
        KafkaConfig config = new KafkaConfig();
        config.setBootstrapServers((String) input.get("bootstrapServers"));
        config.setZkAddress((String) input.get("zkAddress"));
        config.setTopics((String) input.get("topics"));
        config.setProducerSettings((Map<String, String>) input.get("producerSettings"));
        return config;
    }
}
