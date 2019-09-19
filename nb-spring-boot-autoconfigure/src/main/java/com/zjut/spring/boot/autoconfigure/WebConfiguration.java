package com.zjut.spring.boot.autoconfigure;

import com.zjut.spring.boot.filter.PrintUrlFilter;
import com.zjut.spring.boot.filter.TraceIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * @author jsk
 * @Date 2019/2/27 9:35
 */
@Configuration
@ConditionalOnClass(ConfigurableWebServerFactory.class)
@AutoConfigureBefore(ServletWebServerFactoryAutoConfiguration.class)
public class WebConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return (factory) -> {
            factory.setPort(8080);
        };
    }

    /**
     * 打印所有进来的url的过滤器
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean printUrlFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new PrintUrlFilter());
        registration.addUrlPatterns("/*");
        registration.setName("printUrlFilter");
        // 越大越靠前
        registration.setOrder(1);
        return registration;
    }

    /**
     * 注册traceID
     * @return
     */
    @Bean
    public FilterRegistrationBean traceIdFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        // 过滤路径
        registration.addUrlPatterns("/*");
        // 过滤器名称
        registration.setName("traceIdFilter");
        // 过滤器顺序
        registration.setOrder(2);
        return registration;
    }
}
