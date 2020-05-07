package com.yis.syslog.comm;

import com.yis.syslog.domain.enums.OutModuleEnum;
import com.yis.syslog.sender.OutputHandler;
import com.yis.syslog.sender.Sender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * March, or die.
 *
 * @Description: 资源释放
 * @Created by yisany on 2020/01/08
 */
public class ShutDownHook {

    private static final Logger logger = LogManager.getLogger(ShutDownHook.class);

    public ShutDownHook() {

    }

    /**
     * 初始化
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void init() {
        Thread shut = new Thread(new ShutDownHookThread());
        shut.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(shut);
        logger.info("addShutDownHook success...");
    }

    class ShutDownHookThread implements Runnable {

        @Override
        public void run() {
            // 关闭output模块
            ConcurrentHashMap<OutModuleEnum, Sender> outClass = OutputHandler.getOutClass();
            for (Map.Entry<OutModuleEnum, Sender> entry : outClass.entrySet()) {
                entry.getValue().release();
                logger.info("Out Module:{}, Service:{} is shutdown.", entry.getKey(), entry.getValue().getClass().getName());
            }
            // 关闭线程池
            Config.executor.shutdown();
            logger.info("ThreadPool is shutdown.");
        }
    }

}
