package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.client.SeckillFeignClient;
import com.atguigu.gmall.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class SeckillController {


    @Autowired
    SeckillFeignClient seckillFeignClient;

    @RequestMapping("seckill/queue.html")
    public String queue(Model model, String skuIdStr, HttpServletRequest request,Long skuId) {

        String userId = request.getHeader("userId");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String format = sdf.format(new Date());
        // 加密生成一个随机抢购码
        String seckillCode = MD5.encrypt(userId+format);

        // 对比抢购码
        if(skuIdStr.equals(seckillCode)){
            model.addAttribute("skuId",skuId);
            return "seckill/queue";
        }else {
            model.addAttribute("message","非法请求");
            return "seckill/fail";
        }


    }

    @RequestMapping("seckill/{skuId}.html")
    public String getSeckillGoods(@PathVariable("skuId") Long skuId, Model model) {

        SeckillGoods seckillGood =  seckillFeignClient.getSeckillGoods(skuId);
        model.addAttribute("item",seckillGood);
        return "seckill/item";
    }


    @RequestMapping("seckill.html")
    public String seckillList(Model model) {

        List<SeckillGoods> seckillGoods =  seckillFeignClient.seckillList();
        model.addAttribute("list",seckillGoods);
        return "seckill/index";
    }

}
