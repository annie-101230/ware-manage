package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("admin/product")
public class CategoryApiController {

    @Autowired
    CategoryService categoryService;

    @RequestMapping("/getCategory1")
    public Result getCategory1() {

        //查询一级分类
        List<BaseCategory1> baseCategory1List = categoryService.getCategory1();

        return Result.ok(baseCategory1List);
    }

    @RequestMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable("category1Id") Long category1Id) {

        //查询二级分类
        List<BaseCategory2> baseCategory2List = categoryService.getCategory2(category1Id);

        return Result.ok(baseCategory2List);
    }

    @RequestMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable("category2Id") Long category2Id) {

        //查询三级分类
        List<BaseCategory3> baseCategory3List = categoryService.getCategory3(category2Id);

        return Result.ok(baseCategory3List);
    }
}
