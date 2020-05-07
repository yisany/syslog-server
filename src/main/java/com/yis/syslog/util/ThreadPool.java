package com.yis.syslog.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

public class ThreadPool {
    private static final Logger logger = LogManager.getLogger(ThreadPool.class);
    private int defaultPoolSize = 5000;
    private int defaultThreadNum = 1;
    private String defaultPoolName = "pool";
    private ScheduledThreadPoolExecutor scheduler;
    private Object synedObject = new Object();
    private Map<String, BlockedExecutor> executors = new ConcurrentHashMap();

    public ThreadPool() {
        this.scheduler = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("scheduler").build());
    }

    public ThreadPool(int defaultPoolSize, int defaultThreadNum, String defaultPoolName) {
        this.defaultPoolName = defaultPoolName;
        this.defaultPoolSize = defaultPoolSize;
        this.defaultThreadNum = defaultThreadNum;
        this.scheduler = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("scheduler-" + defaultPoolName).build());

    }

    public ScheduledFuture<?> scheduleWithDelay(Runnable command, long delaytime) {
        return this.scheduler.scheduleWithFixedDelay(command, delaytime, delaytime, TimeUnit.MILLISECONDS);
    }

    public ThreadPool.BlockedExecutor getExecutor() {
        String key = this.defaultPoolName + this.defaultPoolSize + this.defaultThreadNum;
        if (!this.executors.containsKey(key)) {
            synchronized(this.synedObject) {
                if (!this.executors.containsKey(key)) {
                    this.executors.put(key, new ThreadPool.BlockedExecutor(this.defaultPoolName, this.defaultPoolSize, this.defaultThreadNum));
                }
            }
        }

        return (ThreadPool.BlockedExecutor)this.executors.get(key);
    }

    public ThreadPool.BlockedExecutor getExecutor(String poolName, int poolSize, int threadNum) {
        String key = poolName + poolSize + threadNum;
        if (!this.executors.containsKey(key)) {
            synchronized(this.synedObject) {
                if (!this.executors.containsKey(key)) {
                    this.executors.put(key, new ThreadPool.BlockedExecutor(poolName, poolSize, threadNum));
                }
            }
        }

        return (ThreadPool.BlockedExecutor)this.executors.get(key);
    }

    public void shutdown() {
        Iterator var1 = this.executors.entrySet().iterator();

        while(var1.hasNext()) {
            Map.Entry<String, BlockedExecutor> e = (Map.Entry)var1.next();
            ((ThreadPool.BlockedExecutor)e.getValue()).shutdown();
        }

        this.scheduler.shutdown();
    }

//    public static void main(String[] args) {
//        ThreadPool pool = new ThreadPool(1, 1, "haode");
//        pool.getExecutor().execute(new Runnable() {
//            public void run() {
//                int var1 = 0;
//
//                while(var1++ < 100) {
//                    System.out.println("dddd");
//
//                    try {
//                        Thread.sleep(10000L);
//                    } catch (InterruptedException var3) {
//                        var3.printStackTrace();
//                    }
//                }
//
//            }
//        });
//    }

    public interface RunnableHandler {
        void process();
    }

    public class BlockedExecutor extends ThreadPoolExecutor {
        private final Semaphore semp;

        public BlockedExecutor(String poolName, int corePoolSize, int threadNum) {
            super(corePoolSize, corePoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), new ThreadFactoryBuilder().setNameFormat(poolName).build());
            this.semp = new Semaphore(corePoolSize);
        }

        public void exec(final ThreadPool.RunnableHandler command) {
            try {
                this.semp.acquire();
                super.execute(new Runnable() {
                    public void run() {
                        try {
                            command.process();
                        } catch (Exception var5) {
                        } finally {
                            BlockedExecutor.this.semp.release();
                        }

                    }
                });
            } catch (InterruptedException var3) {
                ThreadPool.this.logger.warn("msg={}", var3.getMessage());
            }

        }
    }
}