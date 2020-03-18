package com.zjut.spring.boot.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 可将相关配置放到apollo中
 */
@Data
@Component
//@ConfigurationProperties(prefix = "test")
@PropertySource(value = "classpath:properties/datasource.properties", encoding = "UTF-8", ignoreResourceNotFound = true)
public class DruidStatProperties {
    /**
     * Enable StatViewServlet.
     */
    @Value("${druid.stat-view-servlet.enabled}")
    private boolean enabled = true;
    @Value("${druid.stat-view-servlet.url-pattern}")
    private String urlPattern;
    @Value("${druid.stat-view-servlet.allow}")
    private String allow;
    @Value("${druid.stat-view-servlet.deny}")
    private String deny;
    @Value("${druid.stat-view-servlet.login-username}")
    private String loginUsername;
    @Value("${druid.stat-view-servlet.login-password}")
    private String loginPassword;
    @Value("${druid.stat-view-servlet.reset-enable}")
    private String resetEnable;
    //数据库类型
    @Value("${druid.filter.stat.db-type}")
    private String dbType;
    //是否记录慢sql
    @Value("${druid.filter.stat.log-slow-sql}")
    //慢SQL的时间定义
    private boolean logSlowSql;
    @Value("${druid.filter.stat.slow-sql-millis}")
    private long slowSqlMillis;
    //是否SQL合并配置
    @Value("${druid.stat.mergeSql}")
    private boolean mergeSql;
    @Value("${dataSource.logger.name}")
    private String dataSourceLoggerName;
    @Value("${connection.logger.name}")
    private String connectionLoggerName;
    @Value("${statement.logger.name}")
    private String statementLoggerName;
    @Value("${resultSet.logger.name}")
    private String resultSetLoggerName;
    @Value("${druid.log.stmt.executableSql}")
    private Boolean statementExecutableSqlLogEnable;
    //对被认为是攻击的SQL进行LOG.error输出
    @Value("${druid.wall.logViolation}")
    private Boolean logViolation;
    //对被认为是攻击的SQL抛出SQLExcepton
    @Value("${druid.wall.throwException}")
    private Boolean throwException;
    @Value("${druid.wall.multiStatementAllow}")
    private Boolean multiStatementAllow;
}
