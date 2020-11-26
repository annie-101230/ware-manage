package com.atguigu.gmall.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback{

    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setReturnCallback(this);
        rabbitTemplate.setConfirmCallback(this);
    }

    /**
     * 如果消息在交换机上投递失败
     * @param message
     * @param errCode
     * @param errMessage
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int errCode, String errMessage, String exchange, String routingKey) {
        System.out.println("消息投递失败");
    }

    /***
     * 消息发送出去后(交换机)
     * @param correlationData
     * @param b
     * @param errMessage
     */
    @Override
    public void confirm(@Nullable CorrelationData correlationData, boolean b, @Nullable String errMessage) {
        System.out.println("消息是否成功发送到交换机");
    }
}
