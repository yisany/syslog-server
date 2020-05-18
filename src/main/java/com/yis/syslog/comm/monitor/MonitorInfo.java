package com.yis.syslog.comm.monitor;

import lombok.Data;

/**
 * @author by yisany on 2020/05/13
 */
@Data
public class MonitorInfo {

    /** jvm可使用内存. */
    private long jvmTotalMemory;

    /** jvm剩余内存. */
    private long jvmFreeMemory;

    /** jvm最大可使用内存. */
    private long jvmMaxMemory;

    /** 操作系统. */
    private String osName;

    /** 总的物理内存. */
    private long osTotalMemorySize;

    /** 剩余的物理内存. */
    private long osFreeMemorySize;

    /** 已使用的物理内存. */
    private long osUsedMemorySize;

    /** 核心数. */
    private int processors;

}
