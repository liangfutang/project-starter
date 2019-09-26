package com.zjut.spring.boot.autoconfigure;

import com.zjut.spring.boot.beanconfig.RocketMqClientBean;
import com.zjut.spring.boot.properties.RocketMqProperties;
import org.apache.rocketmq.client.log.ClientLogger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnBean(name = "rocketMqCondition")
@Import({RocketMqProperties.class})
public class RocketMqAutoConfiguration {
    static {
        System.setProperty(ClientLogger.CLIENT_LOG_FILESIZE, "104857600");
        System.setProperty(ClientLogger.CLIENT_LOG_LEVEL, "error");
    }

    @Bean(initMethod = "init")
    public RocketMqClientBean getRocketMqClientBean() {
        return new RocketMqClientBean();
    }

}
