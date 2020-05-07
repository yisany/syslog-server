package com.yis.syslog.util;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author milu
 * @Description 命令行解析工具
 * @createTime 2020年01月02日 11:48:00
 */
public class CliUtil {

    private static final Logger logger = LogManager.getLogger(CliUtil.class);

    private static Options ops;
    private static CommandLine commandLine;

    private static final String OPT_FORMAT = "%s-file";
    private static final String DESC_FORMAT = "specify %s config file";

    /**
     * 解析命令行
     * @param args 命令行
     * @param opts 参数名
     * @return
     */
    public static Map<String, String> parseCli(String[] args, String... opts) {
        Map<String, String> comms = new HashMap<>();
        try {
            ops = new Options();
            for (String opt : opts) {
                Option op = new Option(opt, String.format(OPT_FORMAT, opt), true, String.format(DESC_FORMAT, opt));
                op.setRequired(true);
                ops.addOption(op);
            }
            commandLine = new DefaultParser().parse(ops, args);
            for (String opt : opts) {
                if (StringUtils.isNotBlank(commandLine.getOptionValue(opt))) {
                    comms.put(opt, commandLine.getOptionValue(opt));
                } else {
                     comms.put(opt, "Param does not exist.");
                }
            }
            return comms;
        } catch (ParseException e) {
            logger.error("parseOption parse error:{}", e);
            throw new BizException("命令行解析失败, 缺少必要参数");
        }
    }

}
