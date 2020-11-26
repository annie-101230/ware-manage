package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("api/payment")
public class PaymentApiController {

    @Autowired
    PaymentService paymentService;

    @Autowired
    OrderFeignClient orderFeignClient;


    @RequestMapping("alipay/query/{out_trade_no}")
    public Result query(@PathVariable("out_trade_no") String out_trade_no){

        Map<String,Object> map = paymentService.query(out_trade_no);

        return Result.ok(map);
    }

    @RequestMapping("alipay/callback/return")
    public String alipay(HttpServletRequest request){

        String out_trade_no = request.getParameter("out_trade_no");
        String trade_no = request.getParameter("trade_no");
        String callback_content = request.getQueryString();

        // 根据支付宝回调结果更新支付服务
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.toString());
        paymentInfo.setTradeNo(trade_no);
        paymentInfo.setCallbackContent(callback_content);
        paymentInfo.setCallbackTime(new Date());
        paymentService.updatePayment(paymentInfo);


        return "<form action=\"http://payment.gmall.com/paySuccess\">\n" +
                "</form>\n" +
                "<script>\n" +
                "\tdocument.forms[0].submit();\n" +
                "</script>";
    }


    @RequestMapping("alipay/submit/{orderId}")
    public String alipay(@PathVariable("orderId") Long orderId){

        // 根据支付宝的api封装支付请求

        String form = paymentService.alipay(orderId);

        // 开启定时任务
        OrderInfo orderById = orderFeignClient.getOrderById(orderId);
        paymentService.sendPayCheckQueue(orderById.getOutTradeNo(),5);

        return form;
    }
}
