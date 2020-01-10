package org.yis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.comm.ParseOption;
import org.yis.handler.BaseHandler;

/**
 * Aim: 程序主入口
 * Author milu
 * Version: v3.0.1
 */
public class Syslog {

    private static Logger logger = LogManager.getLogger(Syslog.class);

    private static final String EXPORT = "export";
    private static final String PATTERN = "pattern";

    public static void main(String[] args){
        logger.info("=====    Syslog InputHandler Check    =====");
        ParseOption.initConfig(args);
        logger.info("=====  Syslog InputHandler Check over =====");
        BaseHandler handler = new BaseHandler();
        handler.start();
    }

}
