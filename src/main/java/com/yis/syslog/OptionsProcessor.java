package com.yis.syslog;

import com.yis.syslog.domain.InputOptions;
import com.yis.syslog.domain.enums.SyslogProtocolEnum;
import com.yis.syslog.util.CliUtil;
import com.yis.syslog.util.YamlUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
public class OptionsProcessor {

    private static final Logger logger = LogManager.getLogger(OptionsProcessor.class);

    private OptionsProcessor() { }

    private static OptionsProcessor processor;

    private CommandLine comms;
    private Map<String, Object> configs = new ConcurrentHashMap<>();

    public static OptionsProcessor getInstance() {
        if (!Optional.ofNullable(processor).isPresent()) {
            synchronized (OptionsProcessor.class) {
                if (!Optional.ofNullable(processor).isPresent()) {
                    processor = new OptionsProcessor();
                }
            }
        }
        return processor;
    }

    /**
     * 获取输入配置
     * @return
     */
    public InputOptions getInputConfig() {
        if (configs.containsKey("input")) {
            Map<String, Object> inputs = (Map<String, Object>) configs.get("input");
            if (inputs.containsKey("port")) {
                Map<String, Integer> ports = (Map<String, Integer>) inputs.get("port");
                return InputOptions.convert(ports);
            }
        }
        return InputOptions.convert(new HashMap<>());
    }

    /**
     * 获取解析配置
     * @return
     */
    public SyslogProtocolEnum getFilterConfig() {
        if (configs.containsKey("filter")) {
            Map<String, Object> filters = (Map<String, Object>) configs.get("filter");
            if (filters.containsKey("protocol")) {
                String proto = filters.get("protocol").toString();
                return SyslogProtocolEnum.get(proto);
            }
        }
        return SyslogProtocolEnum.UNKNOWN;
    }

    /**
     * 获取输出配置
     * @return
     */
    public Map<String, Object> getOutputConfig() {
        if (configs.containsKey("output")) {
            return (Map<String, Object>) configs.get("output");
        } else {
            logger.error("config can not find output module...");
            return null;
        }
    }

    /**
     * 初始化
     * @param args
     */
    public void initConfig(String[] args) {
        logger.info("Checking config...");
        comms = CliUtil.parseArgs(args);
        configs = YamlUtil.parseYaml(comms.getOptionValue("f"));
    }

}
