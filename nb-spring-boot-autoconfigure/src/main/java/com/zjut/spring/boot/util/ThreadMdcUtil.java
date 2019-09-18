package com.zjut.spring.boot.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zjut.common.constants.CommonConstants;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * 添加traceID后使用多线程，如果不手动添加traceID会导致该traceID没有或不对，因为存储在ThreadLocal中的traceID是在该多线程初始化的时候加载一次
 * 这里将线程池封装了下，每次会更新里面的traceID
 */
public class ThreadMdcUtil {

    public static void setTraceIdIfAbsent() {
        if (MDC.get(CommonConstants.TRACE_ID) == null) {
            MDC.put(CommonConstants.TRACE_ID, getTraceId());
        }
    }

    public static void setTraceId() {
        MDC.put(CommonConstants.TRACE_ID, getTraceId());
    }

    public static void setTraceId(String traceId) {
        MDC.put(CommonConstants.TRACE_ID, traceId);
    }

    public static <T> Callable<T> wrap(final Callable<T> callable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }

    public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }

    public static class ThreadPoolExecutorWrap extends ThreadPoolExecutor {
        public ThreadPoolExecutorWrap(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }
        public ThreadPoolExecutorWrap(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }
        public ThreadPoolExecutorWrap(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        }
        public ThreadPoolExecutorWrap(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        @Override
        public void execute(Runnable task) {
            super.execute(ThreadMdcUtil.wrap(task, MDC.getCopyOfContextMap()));
        }

        @Override
        public Future<?> submit(Runnable task) {
            return super.submit(ThreadMdcUtil.wrap(task, MDC.getCopyOfContextMap()));
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return super.submit(ThreadMdcUtil.wrap(task, MDC.getCopyOfContextMap()), result);
        }
    }


    private static String getTraceId() {
        return UUID.randomUUID().toString();
    }



    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadMdcUtil.ThreadPoolExecutorWrap(30, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(30), new ThreadFactoryBuilder().setNameFormat("测试线程号-%d").build());
    }
}
