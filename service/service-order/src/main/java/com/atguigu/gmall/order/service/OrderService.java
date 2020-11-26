package com.atguigu.gmall.order.service;


import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;

import java.util.List;
import java.util.Map;

public interface OrderService {

    List<OrderDetail> getDetailArrayList(String userId);

    String genTradeNo(String userId);

    Boolean checkTradeNo(String tradeNo, String userId);

    OrderInfo submitOrder(OrderInfo orderInfo);

    OrderInfo getOrderById(Long orderId);

    void updateOrder(Map<String, Object> map);
}
