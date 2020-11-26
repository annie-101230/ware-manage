package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.list.test.User;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    ListService listService;

    @RequestMapping("list")
    SearchResponseVo list(@RequestBody SearchParam searchParam){

        SearchResponseVo searchResponseVo = listService.list(searchParam);
        return searchResponseVo;
    }

    @RequestMapping("hotScore/{skuId}")
    public Result hotScore(@PathVariable("skuId") Long skuId) {

        listService.hotScore(skuId);
        return Result.ok();
    }

    @RequestMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){
        //根据skuId查询goods方法
        listService.onSale(skuId);
        return Result.ok();
    }

    @RequestMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){

        listService.cancelSale(skuId);
        return Result.ok();
    }

    @RequestMapping("createGoods")
    public Result createGoods() {

        listService.createGoods();
        return Result.ok();
    }


    @RequestMapping("createUser")
    public Result createUser(){

        listService.createUser();
        return Result.ok();
    }


}
