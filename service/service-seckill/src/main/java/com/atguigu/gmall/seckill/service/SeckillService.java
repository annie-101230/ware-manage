package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillService {
    void putGoods();

    void publishGoods(Long skuId);

    void unPublishGoods(Long skuId);

    List<SeckillGoods> seckillList();

    SeckillGoods getSeckillGoods(Long skuId);

    void seckillOrder(String userId,Long skuId);
}
