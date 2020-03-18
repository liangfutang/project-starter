package com.zjut.spring.boot.properties;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
//@ConfigurationProperties(prefix = "test")
@PropertySource(value = "classpath:properties/datasource.properties", encoding = "UTF-8", ignoreResourceNotFound = true)
public class DataSourceProperties {
    //  连接数据库的url
    @Value("${spring.datasource.url}")
    private String url;
    //  连接数据库的用户名
    @Value("${spring.datasource.username}")
    private String username;
    //  连接数据库的密码
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClass;
    @Value("${spring.datasource.filters}")
    private String filters;
    @Value("${spring.datasource.maxActive}")
    private int maxActive;
    @Value("${spring.datasource.initialSize}")
    private int initialSize;
    @Value("${spring.datasource.maxWait}")
    private long maxWait;
    @Value("${spring.datasource.minIdle}")
    private int minIdle;
    @Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
    private long timeBetweenEvictionRunsMillis;
    @Value("${spring.datasource.minEvictableIdleTimeMillis}")
    private long minEvictableIdleTimeMillis;
    @Value("${spring.datasource.validationQuery}")
    private String validationQuery;
    @Value("${spring.datasource.testWhileIdle}")
    private Boolean testWhileIdle;
    @Value("${spring.datasource.testOnBorrow}")
    private Boolean testOnBorrow;
    @Value("${spring.datasource.testOnReturn}")
    private Boolean testOnReturn;
    @Value("${spring.datasource.poolPreparedStatements}")
    private Boolean poolPreparedStatements;
    @Value("${spring.datasource.maxOpenPreparedStatements}")
    private int maxOpenPreparedStatements;
    @Value("${spring.datasource.timeBetweenLogStatsMillis:300000}")
    private long timeBetweenLogStatsMillis;

    /**
     * 配置文件路径
     */
    @Value("${mybatis.mapperLocations:classpath:mapper/*.xml}")
    private String mapperLocations;

    /**
     * mapper接口包路径
     */
    @Value("${mybatis.basePackage}")
    private String basePackage;
}
