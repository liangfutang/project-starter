package com.zjut.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(name = "rocketMqCondition")
public class RocketMqAutoConfiguration {
}
