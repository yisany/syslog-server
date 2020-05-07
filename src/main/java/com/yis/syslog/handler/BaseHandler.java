package com.yis.syslog.handler;

import com.yis.syslog.comm.Config;
import com.yis.syslog.domain.enums.ProtocolEnum;
import com.yis.syslog.reader.InputHandler;
import com.yis.syslog.sender.OutputHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.yis.syslog.comm.ShutDownHook;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
public class BaseHandler {

    private static final Logger logger = LogManager.getLogger(BaseHandler.class);

    public BaseHandler() {}

    /**
     * 启动
     */
    public void start() {
        logger.info("Syslog InputHandler starting...");
        // 输出端初始化
        OutputHandler.init();
        // 接收syslog日志线程初始化
        Config.executor.execute(new MonitorThread(ProtocolEnum.UDP, Config.UDP_PORT));
        Config.executor.execute(new MonitorThread(ProtocolEnum.TCP, Config.TCP_PORT));
        Config.executor.execute(new MonitorThread(ProtocolEnum.TLS, Config.TLS_PORT));

        // 添加关闭钩子
        addShutDownHook();
    }

    /**
     * 资源释放
     */
    private void addShutDownHook() {
        ShutDownHook hook = new ShutDownHook();
        hook.init();
    }

    private static class MonitorThread implements Runnable {

        private ProtocolEnum protocol;
        private int port;

        public MonitorThread(ProtocolEnum protocol, int port) {
            this.protocol = protocol;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                InputHandler server = new InputHandler(port, protocol);
                server.listen();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}