package com.atguigu.gmall.payment.receiver;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.payment.service.PaymentService;
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
public class PaymentConsumer {

    @Autowired
    PaymentService paymentService;


    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange=@Exchange(value = "exchange.delay",durable = "true",autoDelete = "true"),
            value = @Queue(value = "queue.delay.1",autoDelete = "true"),
            key = {"routing.delay"}

    ))
    public void proccess(Channel channel,Message message,String jsonMap) throws IOException {

        System.out.println("消费检查队列，检查支付结果");

        Map<String,Object> map = new HashMap<>();
        Map mapJson = JSON.parseObject(jsonMap, map.getClass());

        String out_trade_no = (String)mapJson.get("out_trade_no");
        Integer count = (Integer)mapJson.get("count");

        if(count>=0){
            // 检查结果尚未支付完成，重新发送检查队列
            count --;
            paymentService.sendPayCheckQueue(out_trade_no,count);

            // 消息消费的手动确认(为了保证事务)
            // channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
        }else {
            System.out.println("达到上限，不在检查支付结果");
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

    }




}
