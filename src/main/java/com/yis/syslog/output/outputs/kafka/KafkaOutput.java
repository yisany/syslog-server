package com.yis.syslog.output.outputs.kafka;

import com.alibaba.fastjson.JSON;
import com.yis.syslog.comm.BizException;
import com.yis.syslog.domain.OutputOptions;
import com.yis.syslog.output.Output;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Properties;

/**
 * @author milu
 * @Description kafka producer
 * @createTime 2019年12月12日 13:48:00
 */
public class KafkaOutput implements Output {

    private static final Logger logger = LogManager.getLogger(KafkaOutput.class);

    private static JKafkaProducer producer;

    private Properties props;
    private String bootstrapServers;
    private Map<String, String> properties;
    private String topic;

    public KafkaOutput(OutputOptions.KafkaOption option) {
        this.bootstrapServers = option.getBootstrapServers();
        this.topic = option.getTopics();
        this.properties = option.getProducerSettings();

        prepare();
    }

    @Override
    public void prepare() {
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

    @Override
    public void process(Map<String, Object> event) {
        producer.sendWithRetry(topic, "", JSON.toJSONString(event));
    }

}
