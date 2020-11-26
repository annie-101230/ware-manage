package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("admin/product")
public class AttrApiController {

    @Autowired
    AttrService attrService;

    @RequestMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable("attrId") Long attrId) {

        // 保存属性(值)信息
        List<BaseAttrValue> baseAttrValues = attrService.getAttrValueList(attrId);

        return Result.ok(baseAttrValues);
    }



    @RequestMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {

        // 保存属性(值)信息
        attrService.saveAttrInfo(baseAttrInfo);

        return Result.ok(null);
    }


    @RequestMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id, @PathVariable Long category2Id, @PathVariable Long category3Id) {
        List<BaseAttrInfo> baseAttrInfos =  attrService.attrInfoList(category3Id);

        return Result.ok(baseAttrInfos);
    }


}
