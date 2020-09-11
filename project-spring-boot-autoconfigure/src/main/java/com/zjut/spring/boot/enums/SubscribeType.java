package com.zjut.spring.boot.enums;

/**
 * 订阅的类型
 */
public enum SubscribeType {
    /**
     * 普通订阅类型
     */
    common,
    /**
     * 订阅有序消息
     */
    orderly,
    /**
     * 订阅日志类消息，格式比较自由，返回字符串
     */
    oneWay,
    /**
     * 广播模式消费
     */
    broadcasting;
}
