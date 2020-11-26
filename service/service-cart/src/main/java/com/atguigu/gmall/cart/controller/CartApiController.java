package com.atguigu.gmall.cart.controller;


import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    CartService cartService;

    @RequestMapping("getCartByUserId/{userId}")
    List<CartInfo> getCartByUserId(@PathVariable("userId") String userId) {

        List<CartInfo> cartInfos = cartService.getCartByUserId(userId);
        return cartInfos;
    }

    @RequestMapping("cartList")
    Result cartList(HttpServletRequest request){
        String userId = request.getHeader("userId");

        if(StringUtils.isBlank(userId)){
            userId = request.getHeader("userTempId");
        }
        List<CartInfo> cartInfos = new ArrayList<>();
        cartInfos = cartService.cartList(userId);
        return Result.ok(cartInfos);
    }

    @RequestMapping("addCart")
    void addCart(@RequestBody CartInfo cartInfo){

        cartService.addCart(cartInfo);

    }
}
