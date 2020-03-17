package com.zjut.spring.boot.autoconfigure;

import com.zjut.common.utils.DevParamUtil;
import com.zjut.spring.boot.properties.DubboProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnBean(name = "dubboCondition")
@Import({DubboProperties.class})
@Slf4j
public class DubboAutoConfiguration {

    @Autowired
    private DubboProperties dubboProperties;

    /**
     * 注册的服务名
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass("org.apache.dubbo.spring.boot.autoconfigure.DubboAutoConfiguration")
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(dubboProperties.getApplicationName());
        return applicationConfig;
    }

    /**
     * dubbo协议配置
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName(dubboProperties.getProtocolName());
        protocolConfig.setPort(dubboProperties.getProtocolPort());
        if (dubboProperties.getProtocolThreads() != null) {
            protocolConfig.setThreads(dubboProperties.getProtocolThreads());
        }
        protocolConfig.setThreadpool(dubboProperties.getProtocolThreadpool());
        return protocolConfig;
    }

    /**
     * 注册配置
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public RegistryConfig registryConfig() {
        RegistryConfig registry = new RegistryConfig();
        registry.setProtocol(dubboProperties.getRegistryProtocol());
        registry.setAddress(dubboProperties.getRegistryAddress());
        registry.setSimplified(true);
        return registry;
    }

    /**
     * dubbo后台监听配置
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public MonitorConfig monitorConfig() {
        MonitorConfig monitorConfig = new MonitorConfig();
        monitorConfig.setProtocol(dubboProperties.getRegistryProtocol());
        monitorConfig.setAddress(dubboProperties.getRegistryAddress());
        return monitorConfig;
    }

    /**
     * 生产者配置
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ProviderConfig providerConfig() {
        ProviderConfig providerConfig = new ProviderConfig();
        if (dubboProperties.getProviderTimeout() != null) {
            providerConfig.setTimeout(dubboProperties.getProviderTimeout());
        }
        if (dubboProperties.getProviderDelay() != null) {
            providerConfig.setDelay(dubboProperties.getProviderDelay());
        }
        // 若含有dubbo分组参数，则给服务分组,分组后实现组内消费
        if (StringUtils.isNotEmpty(DevParamUtil.getDubboGroupName())) {
            providerConfig.setGroup(DevParamUtil.getDubboGroupName());
        }
        if (StringUtils.isNotBlank(dubboProperties.getProviderLoadBalance()) && !"default".equals(dubboProperties.getProviderLoadBalance())) {
            try {
                providerConfig.setLoadbalance(dubboProperties.getProviderLoadBalance());
            } catch (IllegalStateException e) {
                log.error("当前配置的loadBalance策略:{},暂不支持，请排查", dubboProperties.getProviderLoadBalance());
            }
        }
        return providerConfig;
    }


    /**
     * 消费者配置
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ConsumerConfig consumerConfig() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setCheck(dubboProperties.getConsumerCheck());
        // 若含有dubbo分组参数，则给服务分组
        if (StringUtils.isNotEmpty(DevParamUtil.getDubboGroupName())) {
            consumerConfig.setGroup(DevParamUtil.getDubboGroupName());
        }
        if (StringUtils.isNotBlank(dubboProperties.getConsumerLoadBalance()) && !"default".equals(dubboProperties.getConsumerLoadBalance())) {
            try {
                consumerConfig.setLoadbalance(dubboProperties.getConsumerLoadBalance());
            } catch (IllegalStateException e) {
                log.error("当前配置的loadBalance策略:{},不支持，请排查", dubboProperties.getConsumerLoadBalance());
            }
        }
        return consumerConfig;
    }

}
