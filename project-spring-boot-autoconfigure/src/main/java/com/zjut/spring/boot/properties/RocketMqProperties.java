package com.zjut.spring.boot.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

//@RefreshScope
@Data
@Component
@PropertySource(value = "classpath:properties/rocketmq.properties", encoding = "UTF-8", ignoreResourceNotFound = true)
public class RocketMqProperties {
    @Value("${rocketmq.addr}")
    private String rocketmqAddr;

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Value("${rocketmq.producer.topic}")
    private String producerTopic;

    @Value("${rocketmq.consumer.group}")
    private String consumerGroup;

    @Value("${rocketmq.consumer.topic}")
    private String consumerTopic;
}