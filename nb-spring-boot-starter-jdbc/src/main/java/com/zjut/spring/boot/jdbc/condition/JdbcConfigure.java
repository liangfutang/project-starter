package com.zjut.spring.boot.jdbc.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: tlf
 */
@Configuration
public class JdbcConfigure {

    private static final Logger logger = LoggerFactory.getLogger(JdbcConfigure.class);

    @Bean
    public JdbcCondition jdbcCondition(){
        logger.info("#########  准备启动数据库模块  #########");
        return new JdbcCondition();
    }
}
