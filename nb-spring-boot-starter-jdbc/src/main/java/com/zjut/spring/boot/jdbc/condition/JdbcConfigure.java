package com.zjut.spring.boot.jdbc.condition;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: tlf
 * @date: 2019/7/4 17:26
 */
@Configuration
public class JdbcConfigure {

    @Bean
    public JdbcCondition jdbcCondition(){
        return new JdbcCondition();
    }
}
