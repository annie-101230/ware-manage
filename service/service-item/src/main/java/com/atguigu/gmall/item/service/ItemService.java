package com.atguigu.gmall.item.service;

import java.math.BigDecimal;
import java.util.Map;

public interface ItemService {
    Map<String,Object> getItem(Long skuId,String ip);

}
