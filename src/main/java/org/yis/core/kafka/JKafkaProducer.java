package org.yis.core.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.util.ThreadPool;

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
        while(!this.queue.isEmpty()) {
            this.sendWithBlock(topic, key, (String)this.queue.poll());
        }

        this.sendWithBlock(topic, key, value);
    }

    public void sendWithBlock(String topic, String key, final String value) {
        if (value != null) {
            this.producer.send(new ProducerRecord(topic, key, value), new Callback() {
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    try {
                        if (exception != null) {
                            JKafkaProducer.this.queue.put(value);
                            JKafkaProducer.logger.error("send data failed, wait to retry, value={},error={}", value, exception.getMessage());
                            Thread.sleep(1000L);
                        }
                    } catch (InterruptedException var4) {
                        JKafkaProducer.logger.error("kafka send callback error", var4);
                    }
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

    public static void perf(final String topic, String propsInfo, int recordNum, int recordSize, int concurrent) {
        ThreadPool threadPool = new ThreadPool(concurrent, concurrent, "kafka-producer");
        Properties props = new Properties();
        String[] propsFields = propsInfo.split("&");
        String[] var8 = propsFields;
        int var9 = propsFields.length;


        for(int i = 0; i < var9; ++i) {
            String field = var8[i];
            String[] propsKV = field.split("=");
            props.put(propsKV[0], propsKV[1]);
        }

        final JKafkaProducer p = init(props);
        final StringBuffer strBuf = new StringBuffer();

        for(int i = 0; i < recordSize; ++i) {
            strBuf.append(i + "");
            if (strBuf.length() > recordSize) {
                break;
            }
        }

        final long num = (long)(recordNum / concurrent);
        final CountDownLatch cc = new CountDownLatch(concurrent);

        for(int i = 0; i < concurrent; ++i) {
            threadPool.getExecutor().execute(new Runnable() {
                public void run() {
                    try {
                        for(int j = 0; (long)j < num; ++j) {
                            p.sendWithRetry(topic, j + "", strBuf.toString());
                        }

                        cc.countDown();
                    } catch (Exception var2) {
                        var2.printStackTrace();
                    }

                }
            });
        }

        long start = System.currentTimeMillis();

        try {
            cc.await();
        } catch (InterruptedException var23) {
        }

        long end = System.currentTimeMillis();
        long totalBytes = (long)(strBuf.toString().length() * recordNum);
        long avgBytes = totalBytes * 1000L / (end - start);
        long avgNum = (long)(recordNum * 1000) / (end - start);
        System.out.println("waste time=" + (end - start));
        System.out.println("totalBytes=" + totalBytes);
        System.out.println("avgBytes=" + avgBytes);
        System.out.println("avgNum=" + avgNum);
        p.close();
        threadPool.shutdown();
    }
}
