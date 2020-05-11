package com.yis.syslog.input;

import com.yis.syslog.OptionsProcessor;
import com.yis.syslog.Syslog;
import com.yis.syslog.domain.InputOptions;
import com.yis.syslog.domain.enums.ProtocolEnum;
import com.yis.syslog.domain.qlist.InputQueueList;
import com.yis.syslog.input.inputs.SyslogInput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author by yisany on 2020/05/08
 */
public class InputFactory {

    public static void initInputInstances(InputQueueList initInputQueueList, List<Input> allBaseInputs) {
        SyslogInput.setInputQueueList(initInputQueueList);
        InputOptions ic = OptionsProcessor.getInstance().getInputConfig();

        InputThread.initInputThread(new HashMap(){{
            put(ProtocolEnum.UDP, ic.getUdp());
            put(ProtocolEnum.TCP, ic.getTcp());
            put(ProtocolEnum.TLS, ic.getTls());
        }}, allBaseInputs);
    }

    private static class InputThread implements Runnable {

        private static final Logger logger = LogManager.getLogger(InputThread.class);

        private SyslogInput input;

        public InputThread(SyslogInput input) {
            this.input = input;
        }

        private static ExecutorService inputExecutor;

        @Override
        public void run() {
            try {
                input.emit();
            } catch (Exception e) {
                logger.error("input start error, module=syslog, protocol={}, port={}", input.getProtocol(), input.getPort());
            }
        }

        public static void initInputThread(Map<ProtocolEnum, Integer> inputs, List<Input> allBaseInputs) {
            if (inputExecutor == null) {
                inputExecutor = Executors.newFixedThreadPool(inputs.size());
            }
            Iterator<Map.Entry<ProtocolEnum, Integer>> iterator = inputs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ProtocolEnum, Integer> next = iterator.next();
                SyslogInput input = new SyslogInput(next.getValue(), next.getKey());
                allBaseInputs.add(input);
                inputExecutor.submit(new InputThread(input));
            }
        }
    }

}
