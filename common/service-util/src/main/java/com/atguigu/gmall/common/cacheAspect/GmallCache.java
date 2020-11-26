package com.atguigu.gmall.common.cacheAspect;

import com.atguigu.gmall.model.cart.CartInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GmallCache {

    public String prefix() default "GmallCache:";

    public String cartPrefix() default "GmallCache:cart:";

    public Class cartList() default CartInfo.class;

}
