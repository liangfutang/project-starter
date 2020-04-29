package com.zjut.spring.boot.autoconfigure;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zjut.spring.boot.interceptor.FactoryPaginationInterceptor;
import com.zjut.spring.boot.properties.DataSourceProperties;
import com.zjut.spring.boot.properties.DruidStatProperties;
import com.zjut.spring.boot.properties.DruidWebStatProperties;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnClass(EmbeddedDatabaseType.class)
@Import({DataSourceProperties.class,
        DruidStatProperties.class,
        DruidWebStatProperties.class})
@AutoConfigureBefore(org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class)
public class DataSourceAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceAutoConfiguration.class);

    @Autowired
    private DataSourceProperties dataSourceProperties;
    @Autowired
    private DruidStatProperties druidStatProperties;
    @Autowired
    private DruidWebStatProperties druidWebStatProperties;

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        dataSource.setDriverClassName(dataSourceProperties.getDriverClass());
        dataSource.setMaxActive(dataSourceProperties.getMaxActive());
        dataSource.setInitialSize(dataSourceProperties.getInitialSize());
        dataSource.setMaxWait(dataSourceProperties.getMaxWait());
        dataSource.setMinIdle(dataSourceProperties.getMinIdle());
        dataSource.setTimeBetweenEvictionRunsMillis(dataSourceProperties.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(dataSourceProperties.getMinEvictableIdleTimeMillis());
        dataSource.setValidationQuery(dataSourceProperties.getValidationQuery());
        dataSource.setTestWhileIdle(dataSourceProperties.getTestWhileIdle());
        dataSource.setTestOnBorrow(dataSourceProperties.getTestOnBorrow());
        dataSource.setTestOnReturn(dataSourceProperties.getTestOnReturn());
        dataSource.setPoolPreparedStatements(dataSourceProperties.getPoolPreparedStatements());
        dataSource.setMaxOpenPreparedStatements(dataSourceProperties.getMaxOpenPreparedStatements());
        dataSource.setUrl(dataSourceProperties.getUrl());
        dataSource.setPassword(dataSourceProperties.getPassword());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setTimeBetweenLogStatsMillis(dataSourceProperties.getTimeBetweenLogStatsMillis());

        List<Filter> filters = new ArrayList<>();
        StatFilter statFilter = new StatFilter();
        statFilter.setDbType(druidStatProperties.getDbType());
        statFilter.setLogSlowSql(druidStatProperties.isLogSlowSql());
        statFilter.setSlowSqlMillis(druidStatProperties.getSlowSqlMillis());
        statFilter.setMergeSql(druidStatProperties.isMergeSql());
        Slf4jLogFilter slf4jLogFilter = new Slf4jLogFilter();
        slf4jLogFilter.setConnectionLoggerName(druidStatProperties.getConnectionLoggerName());
        slf4jLogFilter.setDataSourceLoggerName(druidStatProperties.getDataSourceLoggerName());
        slf4jLogFilter.setResultSetLoggerName(druidStatProperties.getResultSetLoggerName());
        slf4jLogFilter.setStatementLoggerName(druidStatProperties.getStatementLoggerName());
        slf4jLogFilter.setStatementLogEnabled(druidStatProperties.getStatementExecutableSqlLogEnable());
        WallFilter wallFilter = new WallFilter();
        wallFilter.setDbType(druidStatProperties.getDbType());
        wallFilter.setLogViolation(druidStatProperties.getLogViolation());
        wallFilter.setThrowException(druidStatProperties.getThrowException());
        //wallFilter.getConfig().setMultiStatementAllow(true);
        filters.add(statFilter);
        filters.add(slf4jLogFilter);
        filters.add(wallFilter);
        dataSource.setProxyFilters(filters);

        try {
            dataSource.setFilters(dataSourceProperties.getFilters());
            dataSource.init();
            //设置批量更新
            wallFilter.getConfig().setMultiStatementAllow(druidStatProperties.getMultiStatementAllow());
        } catch (SQLException e) {
            logger.error("数据库设置批量更新异常", e);
        }
        return dataSource;
    }

    @Bean(name = "transactionManager")
    @ConditionalOnMissingBean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public FactoryPaginationInterceptor getFactoryPaginationInterceptor() {
        return new FactoryPaginationInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "mybatis.mapperLocations")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource)
            throws Exception {
        final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(dataSourceProperties.getMapperLocations()));
        sessionFactory.setPlugins(new Interceptor[]{
                // 自定义的插件，检查查询的SQL，如果没有limit则加上，并且限制单次查询的最大数量
                this.getFactoryPaginationInterceptor()
        });
        return sessionFactory.getObject();
    }

    /**
     * MapperScannerConfigurer 在apollo之前初始化无法配置(使用apollo的时候需要注意,1.在接口mapper上加注解@Mapper。2.在启动类上加@MapperScan)
     * @return
     */
//    @Bean
//    @ConditionalOnMissingBean
    public MapperScannerConfigurer mapperScannerConfigurer(){
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        mapperScannerConfigurer.setBasePackage(dataSourceProperties.getBasePackage());
        return mapperScannerConfigurer;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServletRegistrationBean servletRegistrationBean() {
        if (druidStatProperties.isEnabled()) {
            ServletRegistrationBean registrationBean = new ServletRegistrationBean();
            registrationBean.setServlet(new StatViewServlet());
            registrationBean.addUrlMappings(druidStatProperties.getUrlPattern() != null ? druidStatProperties.getUrlPattern() : "/druid/*");
            if (druidStatProperties.getAllow() != null) {
                registrationBean.addInitParameter("allow", druidStatProperties.getAllow());
            }
            if (druidStatProperties.getDeny() != null) {
                registrationBean.addInitParameter("deny", druidStatProperties.getDeny());
            }
            if (druidStatProperties.getLoginUsername() != null) {
                registrationBean.addInitParameter("loginUsername", druidStatProperties.getLoginUsername());
            }
            if (druidStatProperties.getLoginPassword() != null) {
                registrationBean.addInitParameter("loginPassword", druidStatProperties.getLoginPassword());
            }
            if (druidStatProperties.getResetEnable() != null) {
                registrationBean.addInitParameter("resetEnable", druidStatProperties.getResetEnable());
            }
            return registrationBean;
        }
        return null;
    }


    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean filterRegistrationBean() {
        if (druidWebStatProperties.isEnabled()) {
            FilterRegistrationBean registrationBean = new FilterRegistrationBean();
            WebStatFilter filter = new WebStatFilter();
            registrationBean.setFilter(filter);
            registrationBean.addUrlPatterns(druidWebStatProperties.getUrlPattern() != null ? druidWebStatProperties.getUrlPattern() : "/*");
            registrationBean.addInitParameter("exclusions", druidWebStatProperties.getExclusions() != null ? druidWebStatProperties.getExclusions() : "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
            if (druidWebStatProperties.getSessionStatEnable() != null) {
                registrationBean.addInitParameter("sessionStatEnable", druidWebStatProperties.getSessionStatEnable());
            }
            if (druidWebStatProperties.getSessionStatMaxCount() != null) {
                registrationBean.addInitParameter("sessionStatMaxCount", druidWebStatProperties.getSessionStatMaxCount());
            }
            if (druidWebStatProperties.getPrincipalSessionName() != null) {
                registrationBean.addInitParameter("principalSessionName", druidWebStatProperties.getPrincipalSessionName());
            }
            if (druidWebStatProperties.getPrincipalCookieName() != null) {
                registrationBean.addInitParameter("principalCookieName", druidWebStatProperties.getPrincipalCookieName());
            }
            if (druidWebStatProperties.getProfileEnable() != null) {
                registrationBean.addInitParameter("profileEnable", druidWebStatProperties.getProfileEnable());
            }
            return registrationBean;
        } else {
            return null;
        }
    }
}
