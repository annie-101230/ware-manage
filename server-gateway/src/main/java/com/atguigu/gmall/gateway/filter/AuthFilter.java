package com.atguigu.gmall.gateway.filter;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthFilter implements GlobalFilter{

    AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${authUrls.url}")
    String authUrls;

    @Autowired
    UserFeignClient userFeignClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().toString();

        // 放行静态资源
        if(uri.indexOf("passport")!=-1||uri.indexOf(".js")!=-1||uri.indexOf(".css")!=-1||uri.indexOf(".jpg")!=-1||uri.indexOf(".png")!=-1||uri.indexOf(".json")!=-1||uri.indexOf(".ico")!=-1){
            return chain.filter(exchange);
        }

        // 黑名单
        boolean pathMatch = antPathMatcher.match("**/admin/**",uri);
        if(pathMatch){
            return out(response,ResultCodeEnum.PERMISSION);
        }

        // 白名单
        // 自定义过滤规则
        // 与cas权限中心交互
        String[] splitWhiteList = authUrls.split(",");

        // 用户需要登录，踢回登录页面
        // 获取token
        String token = getToken(request);
        // 验证token（cas认证中心）
        UserInfo userInfo = null;
        if(!StringUtils.isEmpty(token)){
            userInfo= userFeignClient.verify(token);
            // 将userId放入request中
            request.mutate().header("userId",userInfo.getId()+"");
            exchange.mutate().request(request);
        }else{
            // token为空，用户没有登录
            // 将userTempId放入request中
            String userTempId = getUserTempId(request);
            request.mutate().header("userTempId",userTempId);
            exchange.mutate().request(request);
        }

        for (String urlWhiteList : splitWhiteList) {
            if(uri.indexOf(urlWhiteList)!=-1&&userInfo==null){
                response.setStatusCode(HttpStatus.SEE_OTHER);// 重定向
                response.getHeaders().set(HttpHeaders.LOCATION,"http://passport.gmall.com/login.html?originUrl="+uri);
                Mono<Void> redirectMono = response.setComplete();
                return redirectMono;
            }
        }


        // chain.filter(exchange);
        return chain.filter(exchange);//out(response,ResultCodeEnum.LOGIN_AUTH);
    }

    /***
     * 获得客户端cookie中的临时id
     * @param request
     * @return
     */
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = null;

        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        if(null!=cookies&&cookies.size()>0){
            List<HttpCookie> httpCookie = cookies.get("userTempId");
            if(null!=httpCookie&&httpCookie.size()>0){
                userTempId = httpCookie.get(0).getValue();
            }
        }


        if(StringUtils.isEmpty(userTempId)){
            // 有可能是ajax异步请求，去headers获取userTempId
            List<String> strings = request.getHeaders().get("userTempId");
            if(null!=strings&&strings.size()>0) {
                userTempId = strings.get(0);
            }
        }

        return userTempId;
    }

    /***
     * 获得客户端cookie中的token
     * @param request
     * @return
     */
    private String getToken(ServerHttpRequest request) {
        String token = null;

        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        if(null!=cookies&&cookies.size()>0){
            List<HttpCookie> httpCookie = cookies.get("token");
            if(null!=httpCookie&&httpCookie.size()>0){
                token = httpCookie.get(0).getValue();
            }
        }

        if(StringUtils.isEmpty(token)){
            // 有可能是ajax异步请求，去headers获取token
            List<String> strings = request.getHeaders().get("token");
            if(null!=strings&&strings.size()>0){
                token = strings.get(0);
            }
        }

        return token;
    }

    /***
     * 处理response的返回结果
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response,ResultCodeEnum resultCodeEnum) {
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(wrap));
    }

}
