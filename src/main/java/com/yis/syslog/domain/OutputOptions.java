package com.yis.syslog.domain;

import lombok.Data;

import java.util.Map;

/**
 * @author by yisany on 2020/05/08
 */
public interface OutputOptions {

    OutputOptions convert(Map<String, Object> conf, OutputOptions option);

    class StdoutOption implements OutputOptions {

        @Override
        public OutputOptions convert(Map<String, Object> conf, OutputOptions option) {
            StdoutOption stdout = (StdoutOption) option;
            return stdout;
        }
    }

    @Data
    class FileOption implements OutputOptions {
        private String path;

        @Override
        public OutputOptions convert(Map<String, Object> conf, OutputOptions option) {
            FileOption file = (FileOption) option;
            file.setPath(conf.get("path").toString());
            return file;
        }
    }

    @Data
    class KafkaOption implements OutputOptions {
        private String bootstrapServers;
        private String zkAddress;
        private String topics;
        private Map<String, String> producerSettings;

        @Override
        public OutputOptions convert(Map<String, Object> conf, OutputOptions option) {
            KafkaOption kafka = new KafkaOption();
            kafka.setBootstrapServers((String) conf.get("bootstrapServers"));
            kafka.setZkAddress((String) conf.get("zkAddress"));
            kafka.setTopics((String) conf.get("topics"));
            kafka.setProducerSettings((Map<String, String>) conf.get("producerSettings"));
            return kafka;
        }
    }

}
