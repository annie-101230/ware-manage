package com.atguigu.gmall.mq.service.impl;

import com.atguigu.gmall.mq.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitServiceImp implements RabbitService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);

    }

    @Override
    public void sendDeadMessage(String exchange, String routingKey, Object message, Long time) {

        // 发送消息时，通过设置消息，设置消息的ttl
        rabbitTemplate.convertAndSend(exchange, routingKey, message, messageSet -> {
                    messageSet.getMessageProperties().setExpiration(1 * 1000 * time + "");// 消息设置类，返回消息配置，设置ttl时间
                    return messageSet;
                }
        );
    }

    @Override
    public void sendDelayMessage(String exchange, String routingKey, Object message, long time) {

        // 发送消息时，通过设置消息，设置消息的ttl
        rabbitTemplate.convertAndSend(exchange, routingKey, message, messageSet -> {
                    messageSet.getMessageProperties().setDelay(1000*Integer.parseInt(time+""));// 消息设置类，返回消息配置，设置ttl时间
                    return messageSet;
                }
        );

    }

}
