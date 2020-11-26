package com.atguigu.gmall.test;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Test {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 查询商品各方面信息，最终组合成需要的页面商品数据格式
        Map<String, Object> map =  new HashMap<>();


        // 先查询商品的基本信息
        CompletableFuture<SkuInfo> completableFutureSku = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                System.out.println("查询商品sku信息");

                SkuInfo skuInfo = new SkuInfo();

                skuInfo.setSpuId(12l);
                map.put("skuInfo",skuInfo);
                return skuInfo;
            }
        });

        // 根据商品基本信息的spuId查询商品的销售属性信息
        CompletableFuture<Void> completableFutureSaleAttr = completableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                Long spuId = skuInfo.getSpuId();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("spuId为：" + spuId);

                // 根据spuId查询商品销售属性

                List<SpuSaleAttr> spuSaleAttrs = new ArrayList<>();

                map.put("spuSaleAttrList", spuSaleAttrs);
            }
        });

        CompletableFuture.allOf(completableFutureSku,completableFutureSaleAttr).join();


        System.out.println(JSON.toJSONString(map));


    }

    private static void a() {
        CompletableFuture<String> completableFutureX = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int i = 1/0;
                System.out.println("买西红柿");
                return "西红柿";
            }
        });


        CompletableFuture<String> completableFutureY = completableFutureX.thenApply(new Function<String, String>() {
            @Override
            public String apply(String s) {
                System.out.println(s);
                System.out.println("买鸡蛋");
                return "鸡蛋";
            }
        });

        CompletableFuture<String> exception = completableFutureY.exceptionally(new Function<Throwable, String>() {
            @Override
            public String apply(Throwable throwable) {
                System.out.println("买大葱");
                return "大葱";
            }
        });

        CompletableFuture<String> when = exception.whenComplete(new BiConsumer<String, Throwable>() {
            @Override
            public void accept(String s, Throwable throwable) {
                System.out.println(s);
            }
        });

        CompletableFuture.allOf(completableFutureX,completableFutureY,exception,when).join();

        System.out.println("炒菜，原料为：");
    }


}
