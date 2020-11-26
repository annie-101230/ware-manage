package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cacheAspect.GmallCache;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public List<BaseCategory1> getCategory1() {

        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {

        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category1_id",category1Id);

        List<BaseCategory2> baseCategory2s = baseCategory2Mapper.selectList(queryWrapper);
        return baseCategory2s;
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {

        QueryWrapper<BaseCategory3> QueryWrapper = new QueryWrapper<>();
        QueryWrapper.eq("category2_id",category2Id);

        List<BaseCategory3> category3s = baseCategory3Mapper.selectList(QueryWrapper);
        return category3s;
    }

    @GmallCache
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {

        QueryWrapper<BaseCategoryView> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);

        return baseCategoryViewMapper.selectOne(queryWrapper);
    }

    @Override
    public List<JSONObject> categoryList() {

        // 将分类数据查询出来
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);

        // 封装成多级json集合
        List<JSONObject> category1List = new ArrayList<>();

        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        for (Map.Entry<Long, List<BaseCategoryView>> category1group : category1Map.entrySet()) {

            // 1级分类的id和名称
            Long category1Id = category1group.getKey();
            String category1Name = category1group.getValue().get(0).getCategory1Name();
            // 封装一级分类
            JSONObject category1jsonObject = new JSONObject();
            category1jsonObject.put("categoryId", category1Id);
            category1jsonObject.put("categoryName", category1Name);

            // 封装二级分类
            List<JSONObject> category2List = new ArrayList<>();
            Map<Long, List<BaseCategoryView>> category2Map = category1group.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> category2group : category2Map.entrySet()) {
                JSONObject category2jsonObject = new JSONObject();
                Long category2Id = category2group.getKey();
                String category2Name = category2group.getValue().get(0).getCategory2Name();
                category2jsonObject.put("categoryId", category2Id);
                category2jsonObject.put("categoryName", category2Name);

                // 封装三级分类
                List<JSONObject> category3List = new ArrayList<>();
                Map<Long, List<BaseCategoryView>> category3Map = category2group.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                for (Map.Entry<Long, List<BaseCategoryView>> category3group : category3Map.entrySet()) {
                    JSONObject category3jsonObject = new JSONObject();
                    Long category3Id = category3group.getKey();
                    String category3Name = category3group.getValue().get(0).getCategory3Name();
                    category3jsonObject.put("categoryId", category3Id);
                    category3jsonObject.put("categoryName", category3Name);
                    category3List.add(category3jsonObject);
                }
                // 2级
                category2jsonObject.put("categoryChild", category3List);
                category2List.add(category2jsonObject);
            }
            // 1级
            category1jsonObject.put("categoryChild", category2List);
            category1List.add(category1jsonObject);
        }
        return category1List;
    }
}
