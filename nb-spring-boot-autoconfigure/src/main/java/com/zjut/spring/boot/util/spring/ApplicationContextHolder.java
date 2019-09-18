package com.zjut.spring.boot.util.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 获取Spring上下文
 */
public class ApplicationContextHolder implements ApplicationContextAware {
    // 上下文对象实例
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }
}
