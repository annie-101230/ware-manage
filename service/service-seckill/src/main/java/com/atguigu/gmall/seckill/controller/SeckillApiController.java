package com.atguigu.gmall.seckill.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.config.CacheHelper;
import com.atguigu.gmall.seckill.service.SeckillService;
import com.atguigu.gmall.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillApiController {

    @Autowired
    SeckillService seckillService;

    @RequestMapping("/auth/checkOrder/{skuId}")
    Result checkOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) {

        String userId = request.getHeader("userId");

        // 检查结果

        // 已下单
        if (1 == 0) {
            return Result.ok(ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }

        // 成功
        if (1 == 0) {
            return Result.ok(ResultCodeEnum.SECKILL_SUCCESS);
        }

        // 售罄
        if (1 == 1) {
            return Result.build("已售罄",ResultCodeEnum.SECKILL_FINISH);
        }

        // 排队

        return Result.ok(ResultCodeEnum.SECKILL_RUN);


    }

    /***
     * 抢单
     * @param skuId
     * @param request
     * @param skuIdStr
     * @return
     */
    @RequestMapping("/auth/seckillOrder/{skuId}")
    Result seckillOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request, String skuIdStr) {

        String userId = request.getHeader("userId");

        String seckillStatus = (String) CacheHelper.get(skuId + "");

        if (null != seckillStatus && seckillStatus.equals("1")) {
            seckillService.seckillOrder(userId, skuId);
        } else {
            return Result.fail();
        }

        return Result.ok();
    }


    @RequestMapping("auth/getSeckillSkuIdStr/{skuId}")
    Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId, HttpServletRequest request) {

        String userId = request.getHeader("userId");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String format = sdf.format(new Date());
        // 加密生成一个随机抢购码
        String seckillCode = MD5.encrypt(userId + format);

        return Result.ok(seckillCode);
    }


    @RequestMapping("getSeckillGoods/{skuId}")
    SeckillGoods getSeckillGoods(@PathVariable("skuId") Long skuId) {
        SeckillGoods seckillGood = seckillService.getSeckillGoods(skuId);

        return seckillGood;
    }

    @RequestMapping("seckillList")
    List<SeckillGoods> seckillList() {

        List<SeckillGoods> seckillGoods = seckillService.seckillList();

        return seckillGoods;
    }

    @RequestMapping("putGoods")
    public Result putGoods() {

        // 将数据库中的参与秒杀商品发布到服务器(redis)
        seckillService.putGoods();

        return Result.ok("发布商品");
    }


    @RequestMapping("publishGoods/{skuId}")
    public Result publishGoods(@PathVariable("skuId") Long skuId) {

        // 将商品的秒杀状态发布到服务器
        seckillService.publishGoods(skuId);
        return Result.ok("发布状态");
    }

    @RequestMapping("unPublishGoods/{skuId}")
    public Result unPublishGoods(@PathVariable("skuId") Long skuId) {

        // 将商品的秒杀状态发布到服务器
        seckillService.unPublishGoods(skuId);
        return Result.ok("取消发布");
    }


    @RequestMapping("testPublish/{skuId}")
    public Result testPublish(@PathVariable("skuId") Long skuId) {

        // 将商品的秒杀状态发布到服务器
        Object o = CacheHelper.get(skuId + "");
        return Result.ok(o);
    }


}
