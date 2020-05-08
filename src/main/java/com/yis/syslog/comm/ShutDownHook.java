package com.yis.syslog.comm;

import com.yis.syslog.domain.qlist.InputQueueList;
import com.yis.syslog.domain.qlist.OutputQueueList;
import com.yis.syslog.input.Input;
import com.yis.syslog.output.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * March, or die.
 *
 * @Description: 资源释放
 * @Created by yisany on 2020/01/08
 */
public class ShutDownHook {

    private static final Logger logger = LogManager.getLogger(ShutDownHook.class);

    private InputQueueList initInputQueueList;
    private OutputQueueList initOutputQueueList;
    private List<Output> allBaseOutputs;
    private List<Input> allBaseInputs;

    public ShutDownHook(InputQueueList initInputQueueList, OutputQueueList initOutputQueueList, List<Input> allBaseInputs, List<Output> allBaseOutputs) {
        this.initInputQueueList = initInputQueueList;
        this.initOutputQueueList = initOutputQueueList;
        this.allBaseInputs = allBaseInputs;
        this.allBaseOutputs = allBaseOutputs;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addShutDownHook() {
        Thread shut = new Thread(new ShutDownHookThread());
        shut.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(shut);
        logger.info("addShutDownHook success...");
    }

    class ShutDownHookThread implements Runnable {

        @Override
        public void run() {
            inputRelease();
            if (initInputQueueList != null) {
                initInputQueueList.ququeRelease();
            }
            if (initOutputQueueList != null) {
                initOutputQueueList.ququeRelease();
            }
            outPutRelease();
        }

        private void inputRelease() {
            try {
                if (allBaseOutputs != null) {
                    for (Input input : allBaseInputs) {
                        input.release();
                    }
                }
                logger.warn("inputRelease success...");
            } catch (Exception e) {
                logger.error("inputRelease error:{}", e.getMessage());
            }
        }

        private void outPutRelease() {
            try {
                if (allBaseOutputs != null) {
                    for (Output outPut : allBaseOutputs) {
                        outPut.release();
                    }
                }
                logger.warn("outPutRelease success...");
            } catch (Exception e) {
                logger.error("outPutRelease error:{}", e.getMessage());
            }
        }
    }

}
