package com.atguigu.gmall.all.controller;

import com.alibaba.nacos.client.utils.StringUtils;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    CartFeignClient cartFeignClient;

    @RequestMapping("trade.html")
    public String trade(HttpServletRequest request, Model model){

        String userId = request.getHeader("userId");

        List<UserAddress> userAddressList  =   userFeignClient.getUserAddressList(userId);
        List<OrderDetail> detailArrayList  =   orderFeignClient.getDetailArrayList(userId);

        model.addAttribute("detailArrayList",detailArrayList);
        model.addAttribute("userAddressList",userAddressList);
        model.addAttribute("totalAmount",getTotalAmount(detailArrayList));
        String tradeNo = orderFeignClient.genTradeNo(userId);
        model.addAttribute("tradeNo",tradeNo);



        return "order/trade";
    }

    private Object getTotalAmount(List<OrderDetail> detailArrayList) {
        BigDecimal totalAmout = new BigDecimal("0");
        for (OrderDetail orderDetail : detailArrayList) {
            BigDecimal orderPrice = orderDetail.getOrderPrice();
            totalAmout = totalAmout.add(orderPrice);
        }
        return totalAmout;
    }

    @RequestMapping("addCart.html")
    public String addCart(HttpServletRequest request, CartInfo cartInfo){

        String userId = request.getHeader("userId");

        if(StringUtils.isBlank(userId)){
            userId = request.getHeader("userTempId");;
        }

        cartInfo.setUserId(userId);
        cartFeignClient.addCart(cartInfo);

        return "redirect:http://cart.gmall.com/cart/success.html";
    }
}
