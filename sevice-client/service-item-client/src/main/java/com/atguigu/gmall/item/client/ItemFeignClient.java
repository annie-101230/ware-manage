package com.atguigu.gmall.item.client;

import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(value = "service-item")
public interface ItemFeignClient {

    @RequestMapping("api/item/getItem/{skuId}/{ip}")
    Map<String,Object> getItem(@PathVariable("skuId") Long skuId,@PathVariable("ip") String ip);
}
