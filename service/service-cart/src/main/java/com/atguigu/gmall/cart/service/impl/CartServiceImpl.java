package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.cacheAspect.GmallCache;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.bouncycastle.cert.ocsp.jcajce.JcaRespID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    ProductFeignClient productFeignClient;

    @Override
    @Transactional
    public void addCart(CartInfo cartInfo) {

        // 判断数据库中是否有当前userId添加过的skuId
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",cartInfo.getUserId());
        queryWrapper.eq("sku_id",cartInfo.getSkuId());

        CartInfo cartInfoExist = cartInfoMapper.selectOne(queryWrapper);
        SkuInfo sku = productFeignClient.getSkuById(cartInfo.getSkuId(), "1");
        CartInfo cartInfoCache = new CartInfo();

        if(null!=cartInfoExist){
            // 修改购物车数量
            Integer skuNumAdd = cartInfo.getSkuNum();
            Integer skuNumExist = cartInfoExist.getSkuNum();

            cartInfoExist.setSkuNum((new BigDecimal(skuNumAdd).add(new BigDecimal(skuNumExist))).intValue());
            cartInfoExist.setCartPrice(sku.getPrice().multiply(new BigDecimal(cartInfoExist.getSkuNum())));
            cartInfoMapper.update(cartInfoExist,queryWrapper);

            cartInfoCache = cartInfoExist;

        }else {
            // 添加新购物车
            // cartInfo.setSkuPrice(sku.getPrice());
            cartInfo.setCartPrice(sku.getPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
            cartInfo.setIsChecked(1);
            cartInfo.setImgUrl(sku.getSkuDefaultImg());
            cartInfo.setSkuName(sku.getSkuName());
            cartInfo.setSkuNum(cartInfo.getSkuNum());
            cartInfoMapper.insert(cartInfo);
            cartInfoCache = cartInfo;
        }

        // 同步缓存
        redisTemplate.opsForHash().put("user:"+cartInfo.getUserId()+":cart",cartInfo.getSkuId()+"",cartInfoCache);

    }

    @GmallCache
    @Override
    public List<CartInfo> cartList(String userId) {

        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<CartInfo> cartInfos = cartInfoMapper.selectList(queryWrapper);
        for (CartInfo cartInfo : cartInfos) {
            Long skuId = cartInfo.getSkuId();
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            cartInfo.setSkuPrice(skuPrice);
        }
        return cartInfos;
    }

    @GmallCache
    @Override
    public List<CartInfo> getCartByUserId(String userId) {

        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<CartInfo> cartInfos = cartInfoMapper.selectList(queryWrapper);
        return cartInfos;
    }
}
