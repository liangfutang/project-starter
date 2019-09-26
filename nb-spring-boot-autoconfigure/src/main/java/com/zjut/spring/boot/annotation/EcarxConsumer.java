package com.zjut.spring.boot.annotation;

import com.zjut.spring.boot.enums.SubscribeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消费者的handler上添加测注释可以选择订阅消费的类型
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EcarxConsumer {
    SubscribeType subscribeType();
}
