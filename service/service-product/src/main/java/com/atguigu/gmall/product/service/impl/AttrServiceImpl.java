package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.AttrService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> attrInfoList(Long category3Id) {

        QueryWrapper<BaseAttrInfo> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("category_id", category3Id);
        queryWrapper.eq("category_level", 3);

        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectList(queryWrapper);

        for (BaseAttrInfo baseAttrInfo : baseAttrInfos) {
            QueryWrapper<BaseAttrValue> queryWrapperValue = new QueryWrapper<>();

            queryWrapperValue.eq("attr_id", baseAttrInfo.getId());
            List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(queryWrapperValue);
            baseAttrInfo.setAttrValueList(baseAttrValues);
        }

        return baseAttrInfos;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        if (StringUtils.isEmpty(baseAttrInfo.getId())) {
            // 保存
            baseAttrInfoMapper.insert(baseAttrInfo);
            Long attrId = baseAttrInfo.getId();

        } else {
            baseAttrInfoMapper.updateById(baseAttrInfo);

            QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMapper.delete(queryWrapper);
        }
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(baseAttrValue);
        }

    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> queryWrapperValue = new QueryWrapper<>();

        queryWrapperValue.eq("attr_id", attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(queryWrapperValue);

        return baseAttrValues;
    }
}
