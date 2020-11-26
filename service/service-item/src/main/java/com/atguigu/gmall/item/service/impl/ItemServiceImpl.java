package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    ListFeignClient listFeignClient;

    @Override
    public Map<String, Object> getItem(Long skuId, String ip) {

        // 记录该商品在搜索中的热度值
        listFeignClient.hotScore(skuId);

        return getItemByThread(skuId, ip);
    }

    private Map<String, Object> getItemSignl(Long skuId, String ip) {
        long startCurrentTimeMillis = System.currentTimeMillis();

        System.out.println("开始时间：" + startCurrentTimeMillis);


        Map<String, Object> map = new HashMap<>();

        SkuInfo skuInfo = productFeignClient.getSkuById(skuId, ip);


        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

        List<Map<String, Object>> valueMaps = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());


        BigDecimal price = productFeignClient.getSkuPrice(skuId);// 直接查数据库

        List<SpuSaleAttr> spuSaleAttrs = productFeignClient.getSpuSaleAttrs(skuInfo.getSpuId(), skuInfo.getId());

        List<SkuImage> skuImages = productFeignClient.getSkuImages(skuId);


        skuInfo.setSkuImageList(skuImages);
        map.put("categoryView", baseCategoryView);
        map.put("skuInfo", skuInfo);
        map.put("price", price);
        map.put("spuSaleAttrList", spuSaleAttrs);

        // 将mybatis的list模式的map 转化成json格式的map
        //[{"valueIds":"20|23","sku_id":16},{"valueIds":"20|25","sku_id":17}]   mybatis中的数据
        //[{"20|23":16},{"20|25":17}]  页面上的数据

        Map<String, Object> valueMap = new HashMap<>();
        for (Map<String, Object> vmap : valueMaps) {
            valueMap.put(vmap.get("valueIds") + "", vmap.get("sku_id"));
        }
        map.put("valuesSkuJson", JSON.toJSONString(valueMap));


        long endCurrentTimeMillis = System.currentTimeMillis();

        System.out.println("结束时间：" + endCurrentTimeMillis);

        System.out.println("总耗时：" + (endCurrentTimeMillis - startCurrentTimeMillis));

        return map;
    }


    private Map<String, Object> getItemByThread(Long skuId, String ip) {
        long startCurrentTimeMillis = System.currentTimeMillis();

        System.out.println("开始时间："+startCurrentTimeMillis);


        Map<String, Object> map =  new HashMap<>();

        CompletableFuture<SkuInfo> CompletableFutureSku = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = productFeignClient.getSkuById(skuId,ip);
                List<SkuImage> skuImages = productFeignClient.getSkuImages(skuId);
                skuInfo.setSkuImageList(skuImages);
                map.put("skuInfo",skuInfo);

                return skuInfo;
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> completableFutureCategory = CompletableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                map.put("categoryView", baseCategoryView);
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> completableFuturevalueMaps = CompletableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<Map<String,Object>> valueMaps = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());

                // 将mybatis的list模式的map 转化成json格式的map
                //[{"valueIds":"20|23","sku_id":16},{"valueIds":"20|25","sku_id":17}]   mybatis中的数据
                //[{"20|23":16},{"20|25":17}]  页面上的数据

                Map<String,Object> valueMap = new HashMap<>();
                for (Map<String, Object> vmap : valueMaps) {
                    valueMap.put(vmap.get("valueIds")+"",vmap.get("sku_id"));
                }
                map.put("valuesSkuJson", JSON.toJSONString(valueMap));
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> completableFutureSaleAttr = CompletableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<SpuSaleAttr> spuSaleAttrs = productFeignClient.getSpuSaleAttrs(skuInfo.getSpuId(),skuInfo.getId());
                map.put("spuSaleAttrList",spuSaleAttrs);            }
        }, threadPoolExecutor);


        CompletableFuture<Void> completableFuturePrice = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                BigDecimal price = productFeignClient.getSkuPrice(skuId);// 直接查数据库
                map.put("price", price);

            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(CompletableFutureSku,completableFutureCategory,completableFuturePrice,completableFutureSaleAttr,completableFuturevalueMaps).join();

        long endCurrentTimeMillis = System.currentTimeMillis();

        System.out.println("结束时间："+endCurrentTimeMillis);

        System.out.println("总耗时："+(endCurrentTimeMillis-startCurrentTimeMillis));
        return map;
    }

}
