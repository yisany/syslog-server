package com.yis.syslog.util;

import com.yis.syslog.comm.BizException;
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
                Option op = new Option(opt, String.format(OPT_FORMAT, opt), "-c".equals(opt) ? true : false, String.format(DESC_FORMAT, opt));
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
            logger.error("parseOption parse error:{}", e.getMessage());
            throw new BizException("命令行解析失败, 缺少必要参数");
        }
    }

    public static CommandLine parseArgs(String[] args) {
        CommandLine cmdLine;
        Options options = new Options();
        options.addOption("dev", false, "dev mode");
        options.addOption("h", false, "usage help");
        options.addOption("help", false, "usage help");
        options.addOption("md", true, "monitor server address");
        options.addOption("f", true, "configuration file");
        options.addOption("w", true, "filter worker number");
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
