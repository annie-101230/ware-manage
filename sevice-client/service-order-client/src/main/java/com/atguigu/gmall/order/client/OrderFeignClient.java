package com.atguigu.gmall.order.client;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "service-order")
public interface OrderFeignClient {

    @RequestMapping("api/order/getDetailArrayList/{userId}")
    List<OrderDetail> getDetailArrayList(@PathVariable("userId") String userId);

    @RequestMapping("api/order/genTradeNo/{userId}")
    String genTradeNo(@PathVariable("userId") String userId);

    @RequestMapping("api/order/getOrderById/{orderId}")
    OrderInfo getOrderById(@PathVariable("orderId") Long orderId);
}
