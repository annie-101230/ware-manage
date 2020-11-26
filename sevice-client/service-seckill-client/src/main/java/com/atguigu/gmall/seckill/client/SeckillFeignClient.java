package com.atguigu.gmall.seckill.client;

import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "service-activity")
public interface SeckillFeignClient {

    @RequestMapping("api/activity/seckill/seckillList")
    List<SeckillGoods> seckillList();

    @RequestMapping("api/activity/seckill/getSeckillGoods/{skuId}")
    SeckillGoods getSeckillGoods(@PathVariable("skuId") Long skuId);
}
