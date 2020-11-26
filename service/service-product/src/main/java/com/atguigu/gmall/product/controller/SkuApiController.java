package com.atguigu.gmall.product.controller;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@CrossOrigin
@RestController
@RequestMapping("admin/product")
public class SkuApiController {

    @Autowired
    SkuService skuService;

    //添加SKU
    @RequestMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {

        skuService.saveSkuInfo(skuInfo);

        return Result.ok();
    }


    //获取SKU分页列表
    @RequestMapping("list/{page}/{limit}")
    public Result list(@PathVariable("page")  Long page ,
                       @PathVariable("limit") Long limit) {

        IPage<SkuInfo> skuInfoIPage = new Page<>();
        skuInfoIPage.setCurrent(page);
        skuInfoIPage.setSize(limit);

        IPage<SkuInfo> skuInfoIPage1 = skuService.list(skuInfoIPage);

        return Result.ok(skuInfoIPage1);
    }

    //商品上架
    @RequestMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {

        skuService.onSale(skuId);

        return Result.ok();
    }

    //商品下架
    @RequestMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId) {

        skuService.cancelSale(skuId);

        return Result.ok();
    }



}
