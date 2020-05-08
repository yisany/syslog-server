package com.yis.syslog.input;

import com.yis.syslog.OptionsProcessor;
import com.yis.syslog.domain.InputOptions;
import com.yis.syslog.domain.enums.ProtocolEnum;
import com.yis.syslog.domain.qlist.InputQueueList;
import com.yis.syslog.input.inputs.SyslogInput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by yisany on 2020/05/08
 */
public class InputFactory {

    public static void initInputInstances(InputQueueList initInputQueueList) {
        SyslogInput.setInputQueueList(initInputQueueList);
        InputOptions ic = OptionsProcessor.getInstance().getInputConfig();

        InputThread.initInputThread(new HashMap(){{
            put(ProtocolEnum.UDP, ic.getUdp());
            put(ProtocolEnum.TCP, ic.getTcp());
            put(ProtocolEnum.TLS, ic.getTls());
        }});
    }

    private static class InputThread implements Runnable {

        private static final Logger logger = LogManager.getLogger(InputThread.class);

        private ProtocolEnum protocol;
        private int port;

        public InputThread(ProtocolEnum protocol, int port) {
            this.protocol = protocol;
            this.port = port;
        }

        private static ExecutorService inputExecutor;

        @Override
        public void run() {
            try {
                SyslogInput server = new SyslogInput(port, protocol);
                server.listen();
            } catch (Exception e) {
                logger.error("input start error, module=syslog, protocol={}, port={}", protocol.toString(), port);
            }
        }

        public static void initInputThread(Map<ProtocolEnum, Integer> inputs) {
            if (inputExecutor == null) {
                inputExecutor = Executors.newFixedThreadPool(inputs.size());
            }
            Iterator<Map.Entry<ProtocolEnum, Integer>> iterator = inputs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ProtocolEnum, Integer> next = iterator.next();
                inputExecutor.submit(new InputThread(next.getKey(), next.getValue()));
            }
        }
    }

}
