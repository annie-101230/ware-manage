package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

public interface ListService {

    void onSale(Long skuId);

    void createGoods();

    void createUser();

    void cancelSale(Long skuId);

    void hotScore(Long skuId);

    SearchResponseVo list(SearchParam searchParam);
}
