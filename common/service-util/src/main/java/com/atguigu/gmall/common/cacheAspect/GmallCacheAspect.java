package com.atguigu.gmall.common.cacheAspect;


import com.atguigu.gmall.model.product.SkuInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Around("@annotation(com.atguigu.gmall.common.cacheAspect.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {

        MethodSignature methodSignature = (MethodSignature) point.getSignature();// 方法原始信息
        String name = methodSignature.getMethod().getName();
        System.out.println(name);

        Class returnType = ((MethodSignature) point.getSignature()).getReturnType();

        Object object = new Object();

        // 返回值类型，参数，缓存前后缀
        object = getCacheHit(point);
        String cacheKey = getCacheKey(point);
        if (null == object) {
            String lock = UUID.randomUUID().toString();
            Boolean ifDb = redisTemplate.opsForValue().setIfAbsent(cacheKey + ":lock", lock, 1, TimeUnit.SECONDS);

            if (ifDb) {
                // 访问db
                try {
                    object = point.proceed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                if(null!=object){
                    // 同步缓存
                    redisTemplate.opsForValue().set(cacheKey, object);
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    // 设置lua脚本返回的数据类型
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    // 设置lua脚本返回类型为Long
                    redisScript.setResultType(Long.class);
                    redisScript.setScriptText(script);
                    redisTemplate.execute(redisScript, Arrays.asList(cacheKey + ":lock"),lock);

                }
            }else{
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return getCacheHit(point);
            }
        }

        return object;
    }

    /***
     * 先查询缓存
     * @param point
     * @return
     */
    private Object getCacheHit(ProceedingJoinPoint point) {
        Object object;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();// 方法原始信息
        Class returnType = methodSignature.getReturnType();// 方法返回类型
        GmallCache gmallCacheAnnotation = methodSignature.getMethod().getAnnotation(GmallCache.class);// 方法注解
        String cacheKey = getCacheKey(point);

        object = redisTemplate.opsForValue().get(cacheKey);
        return object;
    }

    private String getCacheKey(ProceedingJoinPoint point) {
        Object object;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();// 方法原始信息
        GmallCache gmallCacheAnnotation = methodSignature.getMethod().getAnnotation(GmallCache.class);// 方法注解

        String name = methodSignature.getMethod().getName();
        System.out.println(name);

        Object[] args = point.getArgs();
        String cacheKey = gmallCacheAnnotation.prefix();
        for (Object arg : args) {
            cacheKey = cacheKey + ":"+name+":" + arg;
        }
        return cacheKey;
    }
}
