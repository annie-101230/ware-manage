package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.item.client.ItemFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Autowired
    ItemFeignClient itemFeignClient;

    public static void main(String[] args) {

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("元素"+i);
        }

        String join = StringUtils.join(list, "|");

        System.out.println(join);

    }

    @RequestMapping("{skuId}.html")
    public String index(@PathVariable("skuId") Long skuId, Model model, HttpServletRequest request){
        // String remoteAddr = request.getRemoteAddr();
        //System.out.println(remoteAddr+"同学访问的商品详情");

        // 调用item的接口
        Map<String,Object> map = new HashMap<>();
        map = itemFeignClient.getItem(skuId,"1");
        model.addAllAttributes(map);
        return "item/index";
    }

}
