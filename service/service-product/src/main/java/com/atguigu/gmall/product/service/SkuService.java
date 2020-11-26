package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SkuService {

    void saveSkuInfo(SkuInfo skuInfo);

    IPage<SkuInfo> list(IPage<SkuInfo> skuInfoIPage);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    SkuInfo getSkuById(Long skuId);

    BigDecimal getSkuPrice(Long skuId);

    List<SkuImage> getSkuImages(Long skuId);

    List<Map<String, Object>> getSaleAttrValuesBySpu(Long spuId);

    Goods getGoodsById(Long skuId);
}

