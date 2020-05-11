package com.yis.syslog;

import com.google.common.collect.Lists;
import com.yis.syslog.comm.ShutDownHook;
import com.yis.syslog.domain.qlist.InputQueueList;
import com.yis.syslog.domain.qlist.OutputQueueList;
import com.yis.syslog.filter.FilterFactory;
import com.yis.syslog.input.Input;
import com.yis.syslog.input.InputFactory;
import com.yis.syslog.output.OutputFactory;
import com.yis.syslog.output.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
public class AssemblyPipeline {

    private static final Logger logger = LogManager.getLogger(AssemblyPipeline.class);

    private AssemblyPipeline() {}

    private static AssemblyPipeline pipeline;

    private InputQueueList initInputQueueList;
    private OutputQueueList initOutputQueueList;

    private List<Input> allBaseInputs = Lists.newCopyOnWriteArrayList();
    private List<Output> allBaseOutputs = Lists.newCopyOnWriteArrayList();

    public static AssemblyPipeline getInstance() {
        if (!Optional.ofNullable(pipeline).isPresent()) {
            synchronized (AssemblyPipeline.class) {
                if (!Optional.ofNullable(pipeline).isPresent()) {
                    pipeline = new AssemblyPipeline();
                }
            }
        }
        return pipeline;
    }

    /**
     * 启动
     */
    public void start() {
        logger.info("Syslog Server starting...");

        // 初始化队列
        logger.info("initInputQueueList start ...");
        initInputQueueList = InputQueueList.getInputQueueListInstance(1, 10000);
        logger.info("initOutputQueueList start ...");
        initOutputQueueList = OutputQueueList.getOutPutQueueListInstance(1, 10000);

        InputFactory.initInputInstances(initInputQueueList, allBaseInputs);
        FilterFactory.initFilterInstances(initInputQueueList, initOutputQueueList);
        OutputFactory.initOutputInstances(initOutputQueueList, allBaseOutputs);

        // 添加关闭钩子
        addShutDownHook();
    }

    /**
     * 资源释放
     */
    private void addShutDownHook() {
        ShutDownHook hook = new ShutDownHook(initInputQueueList, initOutputQueueList,allBaseInputs, allBaseOutputs);
        hook.addShutDownHook();
    }



}
