package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    void updatePayment(PaymentInfo paymentInfo);

    String alipay(Long orderId);

    Map<String, Object> query(String out_trade_no);

    void sendPayCheckQueue(String outTradeNo, Integer count);
}
