package com.zjut.spring.boot.beanconfig;

import com.zjut.common.utils.DevParamUtil;
import com.zjut.mq.client.core.ConsumerHandler;
import com.zjut.mq.client.core.MqConsumerConfigBean;
import com.zjut.mq.client.rocketmq.RocketMqPushConsumerClient;
import com.zjut.spring.boot.annotation.ZjutConsumer;
import com.zjut.spring.boot.enums.SubscribeType;
import com.zjut.spring.boot.properties.RocketMqProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 初始化配置消费者
 */
public class RocketMqClientBean implements ApplicationContextAware {
    private final static Logger logger = LoggerFactory.getLogger(RocketMqClientBean.class);

    private ApplicationContext applicationContext;
    @Autowired
    private RocketMqProperties rocketMqProperties;
    @Autowired(required = false)
    private Map<String, ConsumerHandler> handlerMap;

    void init() {
        // 设置开关(基本用在开发阶段)
        if (DevParamUtil.isCloseRocketMqTopic()) {
            logger.info("未监听线上的rocketMQ消息...");
            return;
        }
        // 设置群组(基本用在开发阶段)
        String groupId = rocketMqProperties.getConsumerGroup();
        if (StringUtils.isNotEmpty(DevParamUtil.getRocketmqConsumerGroupid())) {
            groupId = DevParamUtil.getRocketmqConsumerGroupid();
            logger.info("rocketMQ使用自定义的groupId:" + groupId);
        }
        if (!CollectionUtils.isEmpty(handlerMap)) {
            // 预先设置的需要订阅的topic，如果没有预先设定则无法定于
            String consumerTopicString = rocketMqProperties.getConsumerTopic();
            String[] consumerTopicList = consumerTopicString.split(",");

            // 循环处理topic对应的handler1.处理handlerMap中的topic、按topic分类合并所有tag
            for (String consumerTopic : consumerTopicList) {
                // 1.组装mq配置信息
                MqConsumerConfigBean mqConsumerConfig = new MqConsumerConfigBean();
                mqConsumerConfig.setGroupName(consumerTopic + "_" + groupId);
                mqConsumerConfig.setMqNamesrvAddr(rocketMqProperties.getRocketmqAddr());
                mqConsumerConfig.setTopic(consumerTopic);
                // spring 5.x
//                RocketMqPushConsumerClient rocketMqPushConsumerClient = new RocketMqPushConsumerClient(mqConsumerConfig);
//                BeanDefinitionBuilder b = BeanDefinitionBuilder
//                        .genericBeanDefinition(RocketMqPushConsumerClient.class, () -> rocketMqPushConsumerClient);

                // 2.根据topic创建指定名称的consumer实例(4.x的spring)
                BeanDefinitionBuilder b = BeanDefinitionBuilder
                        .genericBeanDefinition(RocketMqPushConsumerClient.class)
                        .addConstructorArgValue(mqConsumerConfig);
                BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
                beanDefinitionRegistry.registerBeanDefinition(consumerTopic + "RocketMqPushConsumerClient", b.getBeanDefinition());

                // 3.合并所有该topic对应handler中所有tag
                StringBuilder tagList = new StringBuilder();
                Map newHandlerMap = new HashMap<>(8);
                for (String topicWithTag : handlerMap.keySet()) {
                    if (topicWithTag.startsWith(consumerTopic + "_")) {
                        String tag = topicWithTag.replace(consumerTopic + "_", "");
                        tagList.append(tag).append("||");
                        String[] tags = tag.split("\\|\\|");
                        for (String key : tags) {
                            newHandlerMap.put(key, handlerMap.get(topicWithTag));
                        }
                    }
                }
                if (tagList.lastIndexOf("||") != -1) {
                    tagList.delete(tagList.lastIndexOf("||"), tagList.lastIndexOf("||") + 2);
                }

                // 4.根据条件指定订阅类型
                Set<SubscribeType> subscribeType = new HashSet<>();
                if (StringUtils.isNotBlank(tagList)) {
                    // 4.1 判断topic订阅类型
                    for (Object key : newHandlerMap.keySet()) {
                        ZjutConsumer annotation = newHandlerMap.get(key).getClass().getAnnotation(ZjutConsumer.class);
                        if (Objects.isNull(annotation)) {
                            subscribeType.add(SubscribeType.common);
                        } else {
                            SubscribeType st = annotation.subscribeType();
                            switch (st) {
                                case common:
                                    subscribeType.add(SubscribeType.common);
                                    break;
                                case oneWay:
                                    subscribeType.add(SubscribeType.oneWay);
                                    break;
                                case orderly:
                                    subscribeType.add(SubscribeType.orderly);
                                    break;
                                default:
                                    subscribeType.add(SubscribeType.common);
                                    break;
                            }
                        }
                    }
                    if (subscribeType.size() != 1) {
                        logger.info("一个topic指定了多种订阅类型,请检查topic:" + consumerTopic);
                        throw new RuntimeException("一个topic指定了多种订阅类型,请检查topic:" + consumerTopic);
                    }

                    // 4.2 根据订阅类型,使用对应的订阅方法
                    RocketMqPushConsumerClient rocketMqPushConsumerClient = applicationContext.getBean(consumerTopic + "RocketMqPushConsumerClient",
                            RocketMqPushConsumerClient.class);
                    switch (subscribeType.iterator().next()) {
                        case common:
                            rocketMqPushConsumerClient.subscribe(tagList.toString(), newHandlerMap);
                            break;
                        case oneWay:
                            rocketMqPushConsumerClient.subscribeOneWay(tagList.toString(), newHandlerMap);
                            break;
                        case orderly:
                            rocketMqPushConsumerClient.subscribeOrderly(tagList.toString(), newHandlerMap);
                            break;
                        default:
                            logger.info("topic订阅类型出现异常,请检查topic:" + consumerTopic);
                            throw new RuntimeException("topic订阅类型出现异常,请检查topic:" + consumerTopic);
                    }
                } else {
                    logger.info("topic没有处理的handler,请检查topic:" + consumerTopic);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
