package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    OrderService orderService;

    @RequestMapping("api/order/getOrderById/{orderId}")
    OrderInfo getOrderById(@PathVariable("orderId") Long orderId){

        OrderInfo orderInfo = orderService.getOrderById(orderId);
        return orderInfo;
    }

    @RequestMapping("auth/submitOrder")
    Result submitOrder(String tradeNo, HttpServletRequest request,@RequestBody OrderInfo orderInfo){

        String userId = request.getHeader("userId");
        // 校验订单的重复提交
        Boolean b = orderService.checkTradeNo(tradeNo,userId);

        if(b){
            // 提交订单
            orderInfo.setUserId(Long.parseLong(userId));
            orderInfo = orderService.submitOrder(orderInfo);
        }else {
            return Result.fail();
        }
        return Result.ok(orderInfo.getId());
    }

    @RequestMapping("genTradeNo/{userId}")
    String genTradeNo(@PathVariable("userId") String userId){

        return orderService.genTradeNo(userId);
    }

    @RequestMapping("getDetailArrayList/{userId}")
    List<OrderDetail> getDetailArrayList(@PathVariable("userId") String userId){

        List<OrderDetail> orderDetails = orderService.getDetailArrayList(userId);
        return orderDetails;
    }
}
