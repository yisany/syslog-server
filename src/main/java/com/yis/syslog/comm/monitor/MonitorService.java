package com.yis.syslog.comm.monitor;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;

/**
 * @author by yisany on 2020/05/13
 */
public class MonitorService {

    private final static double MB = 1024 * 1024 * 1.0;

    private final static double GB = 1024 * 1024 * 1024 * 1.0;

    public MonitorInfo getMonitorInfoBean() {
        // jvm
        double totalMemory = Runtime.getRuntime().totalMemory() / MB;
        double freeMemory = Runtime.getRuntime().freeMemory() / MB;
        double maxMemory = Runtime.getRuntime().maxMemory() / MB;
        // MonitorInfo
        MonitorInfo infoBean = new MonitorInfo();
        infoBean.setJvmFreeMemory(getIntValue(totalMemory));
        infoBean.setJvmFreeMemory(getIntValue(freeMemory));
        infoBean.setJvmMaxMemory(getIntValue(maxMemory));
        infoBean.setProcessors(Runtime.getRuntime().availableProcessors());
        return infoBean;
    }

    private int getIntValue(double d) {
        return new BigDecimal(d).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static void main(String[] args) {
        MonitorInfo info = new MonitorService().getMonitorInfoBean();
        System.out.println(JSON.toJSONString(info));
    }

}
