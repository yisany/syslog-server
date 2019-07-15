package org.yis.export;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yis.entity.MessageQueue;

import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author milu
 * @Description 输出到kafka
 * @createTime 2019年07月11日 10:52:00
 */
public class ExportKafka {

    private static Logger logger = LoggerFactory.getLogger(ExportKafka.class);

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
        System.out.println("exportToKafka is working...");
        System.out.println(JSON.toJSONString(p));
        try {
            while (true) {
                String msg = JSON.toJSONString(MessageQueue.getInstance().take());
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, msg);
                kafkaProducer.send(record);
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}