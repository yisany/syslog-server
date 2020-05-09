package com.yis.syslog.output.impl.kafka;

import com.yis.syslog.util.ThreadPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class JKafkaProducer {
    private static final Logger logger = LogManager.getLogger(JKafkaProducer.class);
    private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(100);
    private KafkaProducer<String, String> producer;

    public JKafkaProducer(Properties props) {
        this.producer = new KafkaProducer<>(props);
    }

    public static JKafkaProducer init(Properties p) {
        Properties props = new Properties();
        Thread.currentThread().setContextClassLoader(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "86400000");
        props.put(ProducerConfig.RETRIES_CONFIG, "1000000");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
        if (p != null) {
            props.putAll(p);
        }

        return new JKafkaProducer(props);
    }

    public static JKafkaProducer init(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return init(props);
    }

    public void sendWithRetry(String topic, String key, String value) {
        while (!this.queue.isEmpty()) {
            this.sendWithBlock(topic, key, this.queue.poll());
        }

        this.sendWithBlock(topic, key, value);
    }

    public void sendWithBlock(String topic, String key, final String value) {
        if (value != null) {
            ProducerRecord pr = StringUtils.isBlank(key) ? new ProducerRecord(topic, value) : new ProducerRecord(topic, key, value);
            this.producer.send(pr, (metadata, exception) -> {
                try {
                    if (exception != null) {
                        JKafkaProducer.this.queue.put(value);
                        logger.error("send data failed, wait to retry, value={},error={}", value, exception.getMessage());
                        Thread.sleep(1000L);
                    }
                } catch (InterruptedException var4) {
                    logger.error("kafka send callback error", var4);
                }
            });
        }
    }

    public void close() {
        this.producer.close();
    }

    public void flush() {
        this.producer.flush();
    }

}
