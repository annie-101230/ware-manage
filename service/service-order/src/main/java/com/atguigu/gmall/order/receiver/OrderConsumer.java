package com.atguigu.gmall.order.receiver;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OrderConsumer {

    @Autowired
    OrderService orderService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange=@Exchange(value = "exchange.direct.payment.pay",durable = "true",autoDelete = "true"),
            value = @Queue(value = "queue.payment.pay",autoDelete = "true"),
            key = {"payment.pay"}

    ))
    public void proccess(Channel channel,Message message,String jsonMap) throws IOException {
        Map<String,Object> map = new HashMap<>();
        map = JSON.parseObject(jsonMap, map.getClass());

        // 调用订单服务，更新订单状态
        orderService.updateOrder(map);

        // 消息消费的手动确认(为了保证事务)
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

    }




}
