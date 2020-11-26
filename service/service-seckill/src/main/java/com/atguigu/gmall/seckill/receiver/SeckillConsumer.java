package com.atguigu.gmall.seckill.receiver;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.mq.constant.MqConst;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SeckillConsumer {

    @Autowired
    RedisTemplate redisTemplate;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange=@Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER,durable = "true",autoDelete = "true"),
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER,autoDelete = "true"),
            key = {MqConst.ROUTING_SECKILL_USER}

    ))
    public void proccess(Channel channel, Message message, UserRecode userRecode) throws IOException {

        Long skuId = userRecode.getSkuId();
        String userId = userRecode.getUserId();

        // 消费抢单任务
        Object o = redisTemplate.opsForList().rightPop(RedisConst.SECKILL_STOCK_PREFIX + skuId);

        if(null!=o){
            // 抢单成功，生成预订单
            OrderRecode orderRecode = new OrderRecode();
            orderRecode.setNum(1);
            orderRecode.setUserId(userId);
            SeckillGoods seckillGood = (SeckillGoods)redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId+"");
            orderRecode.setSeckillGoods(seckillGood);
            redisTemplate.opsForHash().put(RedisConst.SECKILL_ORDERS,userId,orderRecode);
        }else{
            // 抢单失败，售罄
            redisTemplate.convertAndSend("seckillpush",skuId+":0");
        }

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag,false);
    }


}
