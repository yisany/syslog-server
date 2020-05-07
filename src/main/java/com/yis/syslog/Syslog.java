package com.yis.syslog;

import com.yis.syslog.comm.ParseOption;
import com.yis.syslog.handler.BaseHandler;
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
        logger.info("=====    Syslog InputHandler Check    =====");
        ParseOption.initConfig(args);
        logger.info("=====  Syslog InputHandler Check over =====");
        BaseHandler handler = new BaseHandler();
        handler.start();
    }

}
