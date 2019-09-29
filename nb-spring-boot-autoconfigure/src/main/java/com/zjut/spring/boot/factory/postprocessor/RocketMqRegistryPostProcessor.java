package com.zjut.spring.boot.factory.postprocessor;

import com.zjut.mq.client.core.MqConfigBean;
import com.zjut.mq.client.rocketmq.RocketMqProducerClient;
import com.zjut.mq.client.rocketmq.TransactionProducerClient;
import com.zjut.spring.boot.autoconfigure.DataSourceAutoConfiguration;
import com.zjut.spring.boot.properties.RocketMqProperties;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Rocketmq的生产者
 */
public class RocketMqRegistryPostProcessor implements BeanFactoryPostProcessor {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceAutoConfiguration.class);

    @Autowired
    private RocketMqProperties rocketMqProperties;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        // 1.注册提供者客户端
        String producerTopicString = rocketMqProperties.getProducerTopic();
        String[] producerTopicList = producerTopicString.split(",");

        DefaultTransactionListener defaultTransactionCheckListener = new DefaultTransactionListener();
        for (String producerTopic : producerTopicList) {
            MqConfigBean mqConfig = new MqConfigBean();
            mqConfig.setGroupName(rocketMqProperties.getProducerGroup());
            mqConfig.setMqNamesrvAddr(rocketMqProperties.getRocketmqAddr());
            mqConfig.setTopic(producerTopic);
            // 1.1 普通提供者
            BeanDefinitionBuilder b = BeanDefinitionBuilder
                    .genericBeanDefinition(RocketMqProducerClient.class)
                    .addConstructorArgValue(mqConfig);
            ((DefaultListableBeanFactory) configurableListableBeanFactory).registerBeanDefinition(producerTopic + "RocketMqProducerClient", b.getBeanDefinition());
            // 1.2 事务提供者
            TransactionListener transactionCheckListener = null;
            String transactionListenerBeanName = producerTopic + "TransactionListener";
            if (configurableListableBeanFactory.containsBean(transactionListenerBeanName)) {
                transactionCheckListener = configurableListableBeanFactory.getBean(transactionListenerBeanName, TransactionListener.class);
            } else {
                logger.info("topic:" + producerTopic + ",未提供事务回查的listener!");
                transactionCheckListener = defaultTransactionCheckListener;
            }

            b = BeanDefinitionBuilder
                    .genericBeanDefinition(TransactionProducerClient.class)
                    .addConstructorArgValue(mqConfig)
                    .addConstructorArgValue(transactionCheckListener);
            ((DefaultListableBeanFactory) configurableListableBeanFactory).registerBeanDefinition(producerTopic + "TransactionProducerClient", b.getBeanDefinition());
        }
    }

    class DefaultTransactionListener implements TransactionListener {
        @Override
        public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            return LocalTransactionState.COMMIT_MESSAGE;
        }

        @Override
        public LocalTransactionState checkLocalTransaction(MessageExt msg) {
            return LocalTransactionState.COMMIT_MESSAGE;
        }
    }
}
