package com.atguigu.gmall.seckill.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.mq.service.RabbitService;
import com.atguigu.gmall.seckill.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitService rabbitService;

    @Override
    public void putGoods() {
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(null);

        for (SeckillGoods seckillGood : seckillGoods) {
            Integer num = seckillGood.getNum();

            for (int i = 0; i < num; i++) {
                // 库存
                redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX+seckillGood.getSkuId(),seckillGood.getSkuId());
            }
            redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS,seckillGood.getSkuId()+"",seckillGood);
            redisTemplate.convertAndSend("seckillpush",seckillGood.getSkuId()+":1");
        }


    }

    @Override
    public void publishGoods(Long skuId) {

        redisTemplate.convertAndSend("seckillpush",skuId+":1");

    }

    @Override
    public void unPublishGoods(Long skuId) {

        redisTemplate.convertAndSend("seckillpush",skuId+":0");

    }

    @Override
    public List<SeckillGoods> seckillList() {

        // 查询秒杀库的秒杀列表
        List<SeckillGoods> values = (List<SeckillGoods>)redisTemplate.opsForHash().values(RedisConst.SECKILL_GOODS);

        return values;
    }

    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        SeckillGoods seckillGood = (SeckillGoods)redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId+"");

        return seckillGood;
    }

    @Override
    public void seckillOrder(String userId,Long skuId) {

        // 用户分布式锁
        Boolean ifCan = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, "1", 1l, TimeUnit.MINUTES);

        if(ifCan){
            // 发送抢单的消息队列，缓冲抢购的压力
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER,MqConst.ROUTING_SECKILL_USER,userRecode);
        }

    }


}
