package com.yis.syslog.sender.impl.kafka;

import com.alibaba.fastjson.JSON;
import com.yis.syslog.comm.Config;
import com.yis.syslog.sender.Sender;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.yis.syslog.sender.impl.core.JKafkaProducer;
import com.yis.syslog.util.BizException;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * @author milu
 * @Description kafka producer
 * @createTime 2019年12月12日 13:48:00
 */
public class KafkaSender implements Sender {

    private static final Logger logger = LogManager.getLogger(KafkaSender.class);

    private static JKafkaProducer producer;

    private Properties props;
    private String bootstrapServers;
    private Map<String, String> properties;
    private String topic;

    public KafkaSender() {
        init();
    }

    @Override
    public void init() {
        this.bootstrapServers = Config.kafka.getBootstrapServers();
        this.topic = Config.kafka.getTopics();
        this.properties = Config.kafka.getProducerSettings();

        prepare();
    }

    private void prepare() {
        try {

            if (props == null) {
                props = new Properties();
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                props.putAll(properties);
            }
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            producer = JKafkaProducer.init(props);
        } catch (Exception e) {
            logger.error("kafka producer init error", e);
            throw new BizException("kafka producer init error");
        }
    }

    @Override
    public void release() {
        producer.close();
        logger.info("kafka producer release.");
    }

    /**
     * 生产消息 -> kafka
     * @param caller
     */
    @Override
    public void send(Caller caller) {
        Map<String, Object> event = caller.convert();
        logger.debug("push to kafka, event={}", event);
        producer.sendWithRetry(topic, UUID.randomUUID().toString(), JSON.toJSONString(event));
    }

}
