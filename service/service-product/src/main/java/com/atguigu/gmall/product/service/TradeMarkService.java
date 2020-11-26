package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface TradeMarkService {

    List<BaseTrademark> getTrademarkList();

    IPage<BaseTrademark> baseTrademark(IPage<BaseTrademark> baseTrademarkPage);

    void save(BaseTrademark baseTrademark);

    void remove(Long tmId);
}
