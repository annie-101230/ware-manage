package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.mq.service.RabbitService;
import com.atguigu.gmall.mq.stockReceiver.CacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqTestController {

    @Autowired
    RabbitService rabbitService;

    @RequestMapping("testSendDelayMq")
    public Result testSendDelayMq(){

        rabbitService.sendDelayMessage("exchange.delay","routing.delay","1", 15l);

        return Result.ok();
    }


    @RequestMapping("testSendDeadMq")
    public Result testSendDeadMq(){

        rabbitService.sendDeadMessage("exchange.dead","routing.dead.1","1", 10l);

        return Result.ok();
    }

    @RequestMapping("testSendMq")
    public Result testSendMq(){

        rabbitService.sendMessage("test.exchange","test.routingKey","1");

        return Result.ok();
    }

    @RequestMapping("testStockMap")
    public Result testStockMap(){

        Object o = CacheHelper.get("16");

        return Result.ok(o);
    }

}
