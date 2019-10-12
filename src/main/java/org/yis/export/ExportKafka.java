package org.yis.export;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.entity.MessageQueue;

import java.util.Properties;

/**
 * @author milu
 * @Description 输出到kafka
 * @createTime 2019年07月11日 10:52:00
 */
public class ExportKafka {

    private Logger logger = LogManager.getLogger(ExportKafka.class);

    private static Properties p;

    private static KafkaProducer<String, String> kafkaProducer;

    private String url;
    private String topic;

    public ExportKafka(String url, String topic) {
        this.url = url;
        this.topic = topic;
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, url);
        kafkaProducer = new KafkaProducer<>(p);
    }

    static {
        p = new Properties();
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    }

    public void write2Kafka() {
        logger.info("exportToKafka is working...");
        try {
            while (true) {
                String msg = MessageQueue.getInstance().take().toString();
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, msg);
                kafkaProducer.send(record);
                Thread.sleep(500);
            }
        } catch (Exception e) {
            logger.warn("ExportKafka.write2Kafka error, e={}", e);
        }
    }

}
