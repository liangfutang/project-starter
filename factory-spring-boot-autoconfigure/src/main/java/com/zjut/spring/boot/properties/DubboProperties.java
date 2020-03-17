package com.zjut.spring.boot.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * dubbo.properties是放在引用的服务的下面，引入该dubbo模块需要的参数均配置在该文件下面
 */
@Data
@Component
@PropertySource(value = "classpath:properties/dubbo.properties", encoding = "UTF-8")
public class DubboProperties {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${dubbo.protocol.name}")
    private String protocolName;

    @Value("${dubbo.protocol.port}")
    private Integer protocolPort;

    @Value("${dubbo.scan.base-packages}")
    private String scanBasePackages;

    /**
     * Dubbo的线程池数量，默认的是200个
     */
    @Value("${dubbo.protocol.threads}")
    private Integer protocolThreads;

    /**
     * Dubbo的线程池类型，默认的是fixed,可以设置为cached
     */
    @Value("${dubbo.protocol.threadpool:fixed}")
    private String protocolThreadpool;

    /**
     * 增加队列数量(dubbo 默认是0，也就是请求不会入队列，会去请求别的服务，如果多个服务不建议修改此值，或者值设置的小一些)
     */
    @Value("${dubbo.protocol.queues}")
    private String protocolQueues;

    /**
     * 注册地址
     */
    @Value("${dubbo.registry.address}")
    private String registryAddress;

    @Value("${dubbo.registry.protocol}")
    private String registryProtocol;

    @Value("${dubbo.provider.timeout}")
    private Integer providerTimeout;

    @Value("${dubbo.provider.delay}")
    private Integer providerDelay;

    @Value("${dubbo.provider.retries}")
    private Integer providerRetries;

    @Value("${dubbo.provider.LoadBalance}")
    private String providerLoadBalance;

    @Value("${dubbo.consumer.check:false}")
    private Boolean consumerCheck;

    @Value("${dubbo.consumer.LoadBalance}")
    private String consumerLoadBalance;
}
