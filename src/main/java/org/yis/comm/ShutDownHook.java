package org.yis.comm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.export.Export;
import org.yis.handler.OutputHandler;

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
            // 关闭input线程
            Config.executor.shutdown();
            logger.info("In Module is shutdown.");
            // 关闭output模块
            ConcurrentHashMap<OutModuleEnum, Export> outClass = OutputHandler.getOutClass();
            for (Map.Entry<OutModuleEnum, Export> entry : outClass.entrySet()) {
                entry.getValue().release();
                logger.info("Out Module:{}, Service:{} is shutdown.", entry.getKey(), entry.getValue().getClass().getName());
            }
        }
    }

}
