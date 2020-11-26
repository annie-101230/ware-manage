package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.TradeMarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeMarkServiceImpl implements TradeMarkService {

    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;



    @Override
    public List<BaseTrademark> getTrademarkList() {

        return baseTrademarkMapper.selectList(null);
    }

    @Override
    public IPage<BaseTrademark> baseTrademark(IPage<BaseTrademark> baseTrademarkPage) {

        return  baseTrademarkMapper.selectPage(baseTrademarkPage,null);
    }

    @Override
    public void save(BaseTrademark baseTrademark) {


        Long id = baseTrademark.getId();
        baseTrademark.setId(id);

        String tmName = baseTrademark.getTmName();
        baseTrademark.setTmName(tmName);

        String logoUrl = baseTrademark.getLogoUrl();
        baseTrademark.setLogoUrl(logoUrl);

        baseTrademarkMapper.insert(baseTrademark);
    }

    @Override
    public void remove(Long tmId) {

//        QueryWrapper<BaseTrademark> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("id",tmId);

        int rows = baseTrademarkMapper.deleteById(tmId);

        if (rows > 0) {

        }

    }


}
