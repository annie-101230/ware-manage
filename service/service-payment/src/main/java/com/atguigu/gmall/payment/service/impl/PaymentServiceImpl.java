package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.mq.service.RabbitService;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {


    @Autowired
    AlipayClient alipayClient;// 公共参数

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    PaymentInfoMapper paymentInfoMapper;


    @Autowired
    RabbitService rabbitService;


    @Override
    public String alipay(Long orderId) {
        OrderInfo orderInfo = orderFeignClient.getOrderById(orderId);

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();// 请求参数
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());
        map.put("goods_id",orderInfo.getOrderDetailList().get(0).getSkuId());
        map.put("goods_name",orderInfo.getOrderDetailList().get(0).getSkuName());
        map.put("quantity",1);
        map.put("price",0.01);
        request.setBizContent(JSON.toJSONString(map));

        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        request.setReturnUrl(AlipayConfig.return_payment_url);

        AlipayTradePagePayResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        String form = response.getBody();
        // 保存支付请求信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setOrderId(orderId);
        paymentInfo.setPaymentType("在线支付");
        paymentInfoMapper.insert(paymentInfo);

        return form;
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",paymentInfo.getOutTradeNo());

        // 发送订单已经支付的消息队列，给订单系统
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("order_status",paymentInfo.getPaymentStatus());
        rabbitService.sendMessage("exchange.direct.payment.pay","payment.pay",JSON.toJSONString(map));

        paymentInfoMapper.update(paymentInfo,queryWrapper);

    }

    @Override
    public Map<String, Object> query(String out_trade_no) {

        Map<String, Object> map = new HashMap<>();

        AlipayTradeQueryRequest alipayTradeQueryRequest = new AlipayTradeQueryRequest();// 请求参数


        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("out_trade_no",out_trade_no);

        alipayTradeQueryRequest.setBizContent(JSON.toJSONString(paramMap));

        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(alipayTradeQueryRequest);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        boolean success = response.isSuccess();

        System.out.println(success);

        if(response.isSuccess()){
            System.out.println("调用成功");

            String tradeStatus = response.getTradeStatus();

            map.put("trade_status",tradeStatus);
            System.out.println(tradeStatus);
        } else {
            System.out.println("调用失败");
        }

        return map;
    }

    @Override
    public void sendPayCheckQueue(String outTradeNo,Integer count) {
        System.out.println("发送检查队列");
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("count",count);
        String jsonMap = JSON.toJSONString(map);
        rabbitService.sendDelayMessage("exchange.delay","routing.delay",jsonMap,20);

    }
}
