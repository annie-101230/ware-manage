package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.TradeMarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("admin/product/baseTrademark")
public class TrademarkApiController {

    @Autowired
    TradeMarkService tradeMarkService;

    @RequestMapping("/getTrademarkList")
    public Result getTrademarkList() {

        List<BaseTrademark> baseTrademarks =  tradeMarkService.getTrademarkList();

        return Result.ok(baseTrademarks);
    }

    @RequestMapping("{page}/{limit}")
    public Result baseTrademark(@PathVariable("page")  Long  page   ,
                                @PathVariable("limit") Long limit ) {

        IPage<BaseTrademark> baseTrademarkPage = new Page<>();
        baseTrademarkPage.setSize(limit);
        baseTrademarkPage.setPages(page);

        IPage<BaseTrademark> baseTrademark = tradeMarkService.baseTrademark(baseTrademarkPage);
        return Result.ok(baseTrademark);
    }

    @RequestMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark) {

        tradeMarkService.save(baseTrademark);

        return Result.ok();
    }

    @RequestMapping("remove/{tmId}")
    public Result remove(@PathVariable("tmId") Long tmId) {

        tradeMarkService.remove(tmId);

        return Result.ok();
    }


}
