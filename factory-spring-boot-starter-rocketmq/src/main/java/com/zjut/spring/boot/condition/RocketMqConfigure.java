package com.zjut.spring.boot.condition;

import com.zjut.spring.boot.autoconfigure.RocketMqAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tlf
 */
@Configuration
@AutoConfigureBefore(RocketMqAutoConfiguration.class)
public class RocketMqConfigure {

    private static final Logger logger = LoggerFactory.getLogger(RocketMqConfigure.class);

    @Bean
    public RocketMqCondition rocketMqCondition() {
        logger.info("#########  准备启动数据库模块  #########");
        return new RocketMqCondition();
    }
}
