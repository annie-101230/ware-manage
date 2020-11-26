package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.cacheAspect.GmallCache;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ListFeignClient listFeignClient;

    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

       skuInfoMapper.insert(skuInfo);

        Long skuId = skuInfo.getId();

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {

                skuImage.setSkuId(skuId);
                skuImageMapper.insert(skuImage);
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {

            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {

            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }


    }

    @Override
    public IPage<SkuInfo> list(IPage<SkuInfo> skuInfoIPage) {

        return skuInfoMapper.selectPage(skuInfoIPage,null);
    }

    @Override
    public void onSale(Long skuId) {

        //上架业务
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);

        //同步搜索引擎
        listFeignClient.onSale(skuId);

    }

    @Override
    public void cancelSale(Long skuId) {
        //上架业务
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        //同步搜索引擎
        listFeignClient.cancelSale(skuId);
    }

    @Override
    public SkuInfo getSkuById(Long skuId) {

        SkuInfo skuInfo = new SkuInfo();
        String lock = UUID.randomUUID().toString();

        //先访问缓存
        skuInfo = (SkuInfo)redisTemplate.opsForValue().get("sku:" + skuId + ":info");

        if (null == skuInfo) {

            Boolean ifDb = redisTemplate.opsForValue().setIfAbsent("sku:" + skuId + ":lock", lock,3, TimeUnit.SECONDS);
            if (ifDb ) {
                //访问DB
                skuInfo = getSkuInfoForMDB(skuId);

                if (null != skuInfo) {
                    //同步缓存
                    redisTemplate.opsForValue().set("sku:" + skuId + ":info", skuInfo);

                    //解锁
                    String delLock = (String)redisTemplate.opsForValue().get("sku:" + skuId + ":lock");
                    if (delLock.equals(lock)) {

                        redisTemplate.delete("sku:" + skuId + ":lock");
                    }
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    // 设置lua脚本返回的数据类型
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    // 设置lua脚本返回类型为Long
                    redisScript.setResultType(Long.class);
                    redisScript.setScriptText(script);
                    redisTemplate.execute(redisScript, Arrays.asList("sku:" + skuId + ":lock"),lock);

                    System.out.println("奥利给 --- !!!");
                }
            } else {
                //自旋
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);
            }
        }
        return skuInfo;
    }

    private SkuInfo getSkuInfoForMDB(Long skuId) {
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",skuId);

        return skuInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {

        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",skuId);

        SkuInfo skuInfo = skuInfoMapper.selectOne(queryWrapper);

        return skuInfo.getPrice();
    }

    @Override
    public List<SkuImage> getSkuImages(Long skuId) {

        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);

        return skuImageMapper.selectList(queryWrapper);
    }

    @GmallCache
    @Override
    public List<Map<String, Object>> getSaleAttrValuesBySpu(Long spuId) {

        List<Map<String, Object>> maps = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);

        return maps;
    }

    @Override
    public Goods getGoodsById(Long skuId) {

        Goods goods = new Goods();

        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        BaseTrademark baseTrademark = baseTrademarkMapper.selectById(skuInfo.getTmId());
        QueryWrapper<BaseCategoryView> baseCategoryViewMapperQueryWrapper = new QueryWrapper<>();
        baseCategoryViewMapperQueryWrapper.eq("category3_id",skuInfo.getCategory3Id());
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectOne(baseCategoryViewMapperQueryWrapper);

        goods.setId(skuId);
        goods.setTitle(skuInfo.getSkuName());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        goods.setTmId(baseTrademark.getId());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setCreateTime(new Date());
        goods.setCategory3Id(baseCategoryView.getCategory3Id());
        goods.setCategory3Name(baseCategoryView.getCategory3Name());
        goods.setCategory2Id(baseCategoryView.getCategory2Id());
        goods.setCategory2Name(baseCategoryView.getCategory2Name());
        goods.setCategory1Id(baseCategoryView.getCategory1Id());
        goods.setCategory1Name(baseCategoryView.getCategory1Name());

        List<SearchAttr> searchAttrs = new ArrayList<>();
        searchAttrs = baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
        goods.setAttrs(searchAttrs);
        return goods;
    }
}
