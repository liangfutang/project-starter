package com.zjut.spring.boot.jdbc.condition;

import com.zjut.spring.boot.autoconfigure.DubboAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(DubboAutoConfiguration.class)
@Slf4j
public class DubboConfig {

    @Bean
    public DubboCondition dubboCondition() {
        log.info("#########  准备启动dubbo模块  #########");
        return new DubboCondition();
    }
}
