package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.cacheAspect.GmallCache;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SpuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public IPage<SpuInfo> spuList(IPage<SpuInfo> spuInfoIPage , Long category3Id) {

        QueryWrapper<SpuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);

        IPage<SpuInfo> selectPage = spuInfoMapper.selectPage(spuInfoIPage, queryWrapper);

        return selectPage;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {

        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        // 保存主表
        spuInfoMapper.insert(spuInfo);

        // 得到主键
        Long spuId = spuInfo.getId();

        // 保存图片表
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuId);
            spuImageMapper.insert(spuImage);
        }

        // 保存销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuId);
            spuSaleAttrMapper.insert(spuSaleAttr);
            // 保存销售属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuId);// spuId+saleAttrId联合外键
                spuSaleAttrValue.setBaseSaleAttrId(spuSaleAttr.getBaseSaleAttrId());// spuId+saleAttrId联合外键
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }
        }
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {

        QueryWrapper<SpuSaleAttr> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);

        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectList(queryWrapper);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {

            QueryWrapper<SpuSaleAttrValue> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("spu_id",spuId);
            queryWrapper1.eq("base_sale_attr_id",spuSaleAttr.getBaseSaleAttrId());

            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(queryWrapper1);

            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);

        }


        return spuSaleAttrs;
    }

    @GmallCache
    @Override
    public List<SpuImage> spuImageList(String spuId) {

        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);

        List<SpuImage> spuImages = spuImageMapper.selectList(queryWrapper);

        return spuImages;
    }


    @GmallCache
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrs(Long spu_id,Long sku_id) {

        /*
         * 实现一个 三表查询
         * spu_sale_attr : SPU销售属性
         * spu_sale_attr_value : SPU销售属性值
         * sku_sale_attr_value : SKU销售属性值
         * */

        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(spu_id,sku_id);

        return spuSaleAttrs;
    }
}
