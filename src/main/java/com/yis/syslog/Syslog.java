package com.yis.syslog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Aim: 程序主入口
 * Author milu
 * Version: v3.0.1
 */
public class Syslog {

    private static Logger logger = LogManager.getLogger(Syslog.class);

    public static void main(String[] args){
        logger.info("=====    Syslog SyslogInput Check    =====");
        OptionsProcessor.getInstance().initConfig(args);
        logger.info("=====  Syslog SyslogInput Check over =====");
        AssemblyPipeline.getInstance().start();
    }

}
