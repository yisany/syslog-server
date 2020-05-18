package com.yis.syslog;

import com.yis.syslog.comm.BizException;
import com.yis.syslog.comm.monitor.MonitorInfo;
import com.yis.syslog.comm.monitor.MonitorService;
import com.yis.syslog.domain.InputOptions;
import com.yis.syslog.domain.enums.SyslogProtocolEnum;
import com.yis.syslog.util.YamlUtil;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
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

    private static MonitorInfo monitorInfo = new MonitorService().getMonitorInfoBean();

    private OptionsProcessor() {
    }

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
     *
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
     *
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
     *
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
     * 获取input队列数量
     *
     * @return
     */
    public int getInitInputWork() {
        String number = comms.getOptionValue("w");
        int works = StringUtils.isNotBlank(number) ? Integer.parseInt(number) : getInputBase();
        logger.warn("filter works:{}", String.valueOf(works));
        return works;
    }

    /**
     * 获取output队列数量
     *
     * @return
     */
    public int getInitOutputWork() {
        String number = comms.getOptionValue("o");
        int works = StringUtils.isNotBlank(number) ? Integer.parseInt(number) : getOutputBase();
        logger.warn("output works:{}", String.valueOf(works));
        return works;
    }

    private int getInputBase() {
        int process = monitorInfo.getProcessors();
        return process + process / 2;
    }

    private static int getOutputBase(){
        int process = monitorInfo.getProcessors();
        return process;
    }

    /**
     * 初始化
     *
     * @param args
     */
    public void initConfig(String[] args) {
        logger.info("Checking config...");
        comms = parseArgs(args);
        configs = YamlUtil.parseYaml(comms.getOptionValue("f"));
    }

    private CommandLine parseArgs(String[] args) {
        CommandLine cmdLine;
        Options options = new Options();
        options.addOption("dev", false, "dev mode");
        options.addOption("h", false, "usage help");
        options.addOption("help", false, "usage help");
        options.addOption("md", true, "monitor server address");
        options.addOption("f", true, "configuration file");
        options.addOption("w", true, "input worker number");
        options.addOption("o", true, "output worker number");
        options.addOption("c", true, "output queue size coefficient");
        options.addOption("i", true, "input queue size coefficient");
        try {
            CommandLineParser paraer = new DefaultParser();
            cmdLine = paraer.parse(options, args);
            if (cmdLine.hasOption("help") || cmdLine.hasOption("h")) {
                usage();
                System.exit(-1);
            }

            if (!cmdLine.hasOption("f")) {
                throw new ParseException("Required -f argument to specify config file");
            }

            return cmdLine;
        } catch (ParseException e) {
            logger.error("parseOption parse error:{}", e.getMessage());
            throw new BizException("命令行解析失败, 缺少必要参数");
        }
    }

    /**
     * print help information
     */
    private static void usage() {
        StringBuilder helpInfo = new StringBuilder();
        helpInfo.append("-h").append("\t\t\thelp command").append("\n")
                .append("-help").append("\t\t\thelp command").append("\n")
                .append("-md").append("\t\t\tmonitor server address, default localhost:19221").append("\n")
                .append("-f").append("\t\t\trequired config, indicate config file").append("\n")
                .append("-w").append("\t\t\tfilter worker numbers").append("\n")
                .append("-o").append("\t\t\toutput worker numbers").append("\n")
                .append("-c").append("\t\t\t output queue size coefficient").append("\n")
                .append("-i").append("\t\t\t input queue size coefficient").append("\n")
                .append("dev").append("\t\t\tdev").append("\n");
        System.out.println(helpInfo.toString());
    }

}
