package org.yis;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;

/**
 * @author milu
 * @Description Test
 * @createTime 2019年07月17日 17:13:00
 */
public class Test {

    public static void main(String[] args) {
        testShaDiao();
    }

    private static void testShaDiao() {
        char c1 = 48; // 0
        char c2 = 50; // 2
        String[] names = new String[] {"张天祎", "叶素康", "钱奕辰", "张帆", "陈彬男",
                "莫瑞", "赵强", "李伟栋", "刘鑫", "笪磊"};
        System.out.println("随机输出一个沙雕名字：" + names[getNumber()]);
    }

    private static int getNumber() {
        int num = (int)(1 + Math.random());
        return num < (byte)00000010 && num > (byte)00000000 ? num : getNumber();
    }

    private static void testKafka() {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "106.15.206.219:9092");//kafka地址，多个地址用逗号分割
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(p);

        try {
            while (true) {
                String msg = "Hello," + new Random().nextInt(100);
                ProducerRecord<String, String> record = new ProducerRecord<String, String>("test", msg);
                kafkaProducer.send(record);
                System.out.println("消息发送成功:" + msg);
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            kafkaProducer.close();
        }
    }


}
