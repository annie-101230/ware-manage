package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.list.test.User;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public void onSale(Long skuId) {
        Goods goods = productFeignClient.getGoodsBySkuId(skuId);
        goodsRepository.save(goods);
    }

    @Override
    public void createUser() {
        elasticsearchRestTemplate.createIndex(User.class);
        elasticsearchRestTemplate.putMapping(User.class);
    }

    @Override
    public void createGoods() {
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
    }

    @Override
    public void cancelSale(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void hotScore(Long skuId) {

        // 查询缓存中的热度值
        // sku:16:hotScore
        Long hotScore = (Long) redisTemplate.opsForValue().increment("sku:" + skuId + ":hotScore", 1l);

        // 同步es
        // 使用缓存进行缓冲，稀释请求10此以上，对es进行更新
        if (hotScore % 10 == 0) {
            Optional<Goods> byId = goodsRepository.findById(skuId);
            Goods goods = byId.get();
            goods.setHotScore(hotScore);
            goodsRepository.save(goods);
        }

    }

    @Override
    public SearchResponseVo list(SearchParam searchParam) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();

        // 封装请求dsl命令
        SearchRequest searchRequest = new SearchRequest();
        searchRequest = getSearchDsl(searchParam);// dsl语句

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 解析返回结果
            searchResponseVo = parseSearchResponse(searchResponse);// 返回结果
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResponseVo;
    }

    /***
     * 封装dsl请求语句
     * @param searchParam
     * @return
     */
    private SearchRequest getSearchDsl(SearchParam searchParam) {
        Long category3Id = searchParam.getCategory3Id();
        String trademark = searchParam.getTrademark();
        String keyword = searchParam.getKeyword();
        String[] props = searchParam.getProps();

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        searchRequest.types("info");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(20);
        searchSourceBuilder.from(0);
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 三级分类
        if (null != category3Id && category3Id > 0) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("category3Id", category3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }


        // 关键字
        if (StringUtils.isNotBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
            // 高亮设置
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        // 属性s(nested)
        if (null != props && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");

                Long attrId = Long.parseLong(split[0]);
                String attrValueName = split[1];
                String attrName = split[2];

                BoolQueryBuilder boolQueryBuilderForNested = new BoolQueryBuilder();
                // 属性id
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("attrs.attrId", attrId);
                boolQueryBuilderForNested.filter(termQueryBuilder);
                // 属性值名称
                MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("attrs.attrValue", attrValueName);
                boolQueryBuilderForNested.must(matchQueryBuilder);
                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs",boolQueryBuilderForNested, ScoreMode.None);

                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }

        // 商标
        if (StringUtils.isNotBlank(trademark)) {
            // 取出商标参数中的商标id
            Long tmId = Long.parseLong(trademark.split(":")[0]);
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("tmId", tmId);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // 排序
        if(StringUtils.isNotBlank(searchParam.getOrder())){
            String order = searchParam.getOrder();
            String[] split = order.split(":");
            String type = split[0];// 1 2
            String sort = split[1];// asc desc

            String name = "hotScore";
            if(type.equals("2")){
                name = "price";
            }
            searchSourceBuilder.sort(name, sort.equals("asc")?SortOrder.ASC:SortOrder.DESC);
        }

        // 检索
        searchSourceBuilder.query(boolQueryBuilder);

        // 商标聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));


        // 属性聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        System.out.println(searchSourceBuilder.toString());// 打印dsl语句
        searchRequest.source(searchSourceBuilder);// 检索条件

        return searchRequest;
    }

    /***
     * 解析返回结果
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();
        if (hits.totalHits > 0) {
            // 解析命中结果
            SearchHit[] hitsResult = hits.getHits();
            List<Goods> goodsList = new ArrayList<>();
            for (SearchHit documentFields : hitsResult) {
                String sourceAsJSON = documentFields.getSourceAsString();
                Goods goods = JSON.parseObject(sourceAsJSON, Goods.class);
                // 判断是否设置了高亮
                Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
                if(null!=highlightFields&&highlightFields.size()>0){
                    HighlightField highlightField = highlightFields.get("title");
                    Text[] fragments = highlightField.getFragments();
                    Text titleTextHighlight = fragments[0];
                    goods.setTitle(titleTextHighlight.toString());
                }
                goodsList.add(goods);


            }
            searchResponseVo.setGoodsList(goodsList);

            // 解析商标聚合
            ParsedLongTerms TmIdParsedLongTerms = (ParsedLongTerms) searchResponse.getAggregations().get("tmIdAgg");

            List<SearchResponseTmVo> searchResponseTmVos = TmIdParsedLongTerms.getBuckets().stream().map(tmIdBucket -> {
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();

                // id
                Long tmIdKey = (Long) tmIdBucket.getKey();
                searchResponseTmVo.setTmId(tmIdKey);

                // name
                ParsedStringTerms TmNameParsedStringTerms = (ParsedStringTerms) tmIdBucket.getAggregations().get("tmNameAgg");
                List<String> tmNames = TmNameParsedStringTerms.getBuckets().stream().map(tmNameBucket -> {
                    return tmNameBucket.getKeyAsString();
                }).collect(Collectors.toList());
                searchResponseTmVo.setTmName(tmNames.get(0));
                // logo
                ParsedStringTerms tmLogoUrlParsedStringTerms = (ParsedStringTerms) tmIdBucket.getAggregations().get("tmLogoUrlAgg");
                String tmLogoUrl = tmLogoUrlParsedStringTerms.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

                return searchResponseTmVo;
            }).collect(Collectors.toList());


            searchResponseVo.setTrademarkList(searchResponseTmVos);

            // 解析属性聚合
            ParsedNested attrsAggparsedNested = (ParsedNested) searchResponse.getAggregations().get("attrsAgg");
            ParsedLongTerms attrIdParsedLongTerms = (ParsedLongTerms) attrsAggparsedNested.getAggregations().get("attrIdAgg");

            List<SearchResponseAttrVo> searchResponseAttrVos = attrIdParsedLongTerms.getBuckets().stream().map(attrIdBucket->{
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();

                // id
                Long attrIdKey = (Long) attrIdBucket.getKey();
                searchResponseAttrVo.setAttrId(attrIdKey);

                // attrName
                ParsedStringTerms attrNameParsedStringTerms = (ParsedStringTerms) attrIdBucket.getAggregations().get("attrNameAgg");
                String attrName = attrNameParsedStringTerms.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);

                // attrValue
                ParsedStringTerms attrValueParsedStringTerms = (ParsedStringTerms) attrIdBucket.getAggregations().get("attrValueAgg");
                List<String> attrValueList = attrValueParsedStringTerms.getBuckets().stream().map(attrValueBucket->{
                    return attrValueBucket.getKeyAsString();
                }).collect(Collectors.toList());
                searchResponseAttrVo.setAttrValueList(attrValueList);

                return searchResponseAttrVo;
            }).collect(Collectors.toList());

            searchResponseVo.setAttrsList(searchResponseAttrVos);




        }


        return searchResponseVo;
    }
}
