package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.AttrService;
import com.atguigu.gmall.product.service.CategoryService;
import com.atguigu.gmall.product.service.SkuService;
import com.atguigu.gmall.product.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    SkuService skuService;

    @Autowired
    AttrService attrService;

    @Autowired
    SpuService spuService;

    @Autowired
    CategoryService categoryService;

    @RequestMapping("categoryList")
    List<JSONObject> categoryList(){

        List<JSONObject> jsonObject = categoryService.categoryList();
        return jsonObject;
    }

    @RequestMapping("getGoodsBySkuId/{skuId}")
    Goods getGoodsBySkuId(@PathVariable("skuId") Long skuId){

        Goods goods = skuService.getGoodsById(skuId);
        return goods;
    }


    @RequestMapping("getSkuById/{skuId}/{ip}")
    SkuInfo getSkuById(@PathVariable("skuId") Long skuId,@PathVariable("ip") String ip){

        SkuInfo skuInfo = skuService.getSkuById(skuId);

        return skuInfo;
    }


    @RequestMapping("getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable Long skuId){

        BigDecimal price = skuService.getSkuPrice(skuId);

        return price;
    }

    @RequestMapping("getSpuSaleAttrs/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrs(@PathVariable("spuId") Long spuId , @PathVariable("skuId") Long skuId){

        return spuService.getSpuSaleAttrs(spuId,skuId);
    }

    @RequestMapping("getSkuImages/{skuId}")
    List<SkuImage> getSkuImages(@PathVariable Long skuId){

        return skuService.getSkuImages(skuId);
    }


    @RequestMapping("getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable Long category3Id){

        return categoryService.getCategoryView(category3Id);
    }

    @RequestMapping("getSaleAttrValuesBySpu/{spuId}")
    List<Map<String, Object>> getSaleAttrValuesBySpu(@PathVariable("spuId") Long spuId){

        return skuService.getSaleAttrValuesBySpu(spuId);
    }
}
