package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import sun.swing.StringUIClientPropertyKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {

    @Autowired
    ListFeignClient listFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @RequestMapping("index.html")
    public String index(Model model) {

        List<JSONObject> categoryList = new ArrayList<>();
        categoryList = productFeignClient.categoryList();
        model.addAttribute("list",categoryList);
        return "index/index";
    }

    @RequestMapping({"list.html","search.html"})
    public String list(Model model,SearchParam searchParam){

        SearchResponseVo searchResponseVo = listFeignClient.list(searchParam);
        model.addAttribute("goodsList",searchResponseVo.getGoodsList());
        model.addAttribute("attrsList",searchResponseVo.getAttrsList());
        model.addAttribute("trademarkList",searchResponseVo.getTrademarkList());
        model.addAttribute("urlParam",getUrlParam(searchParam));

        if(null!=searchParam.getProps()&&searchParam.getProps().length>0){
            List<SearchAttr> searchAttrs = new ArrayList<>();
            for (String prop : searchParam.getProps()) {
                SearchAttr searchAttr = new SearchAttr();
                String[] split = prop.split(":");

                Long attrId = Long.parseLong(split[0]);
                String attrValueName = split[1];
                String attrName = split[2];
                searchAttr.setAttrId(attrId);
                searchAttr.setAttrName(attrName);
                searchAttr.setAttrValue(attrValueName);
                searchAttrs.add(searchAttr);
            }
            model.addAttribute("propsParamList",searchAttrs);
        }

        if(StringUtils.isNotBlank(searchParam.getTrademark())){
            model.addAttribute("trademarkParam",searchParam.getTrademark().split(":")[1]);
        }

        if(StringUtils.isNotBlank(searchParam.getOrder())){
            String order = searchParam.getOrder();
            String[] split = order.split(":");
            String type = split[0];// 1 2
            String sort = split[1];// asc desc
            Map<String,String> orderMap = new HashMap<>();
            orderMap.put("type",type);
            orderMap.put("sort",sort);
            model.addAttribute("orderMap",orderMap);
        }
        return "list/index";
    }

    private String getUrlParam(SearchParam searchParam) {
        StringBuffer urlParam = new StringBuffer("list.html?");

        Long category3Id = searchParam.getCategory3Id();
        String trademark = searchParam.getTrademark();
        String keyword = searchParam.getKeyword();
        String[] props = searchParam.getProps();


        if(null!=category3Id&&category3Id>0){
            urlParam.append("category3Id="+category3Id);
        }

        if(StringUtils.isNotBlank(keyword)){
            urlParam.append("keyword="+keyword);
        }

        if(StringUtils.isNotBlank(trademark)){
            urlParam.append("&trademark="+trademark);
        }

        if(null!=props&&props.length>0){
            for (String prop : props) {
                urlParam.append("&props="+prop);
            }
        }

        return urlParam.toString();
    }

}
