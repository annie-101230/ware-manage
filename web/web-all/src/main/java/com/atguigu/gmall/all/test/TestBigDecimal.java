package com.atguigu.gmall.all.test;

import java.math.BigDecimal;

public class TestBigDecimal {

    public static void main(String[] args) {
        // 初始化
        BigDecimal b1 = new BigDecimal(0.01d);
        BigDecimal b2 = new BigDecimal(0.01f);
        BigDecimal b3 = new BigDecimal("0.01");
        System.out.println(b1);
        System.out.println(b2);
        System.out.println(b3);

        // 比较
        BigDecimal bigDecimal1 = new BigDecimal("0");
        BigDecimal bigDecimal2 = new BigDecimal("1");
        int i = bigDecimal1.compareTo(bigDecimal2);// -1 0 1
        System.out.println(i);

        // 运算
        BigDecimal add = bigDecimal1.add(bigDecimal2);
        System.out.println(add);
        BigDecimal subtract = bigDecimal1.subtract(bigDecimal2);
        System.out.println(subtract);
        BigDecimal multiply = bigDecimal1.multiply(bigDecimal2);
        System.out.println(multiply);
        BigDecimal b4 = new BigDecimal("6");
        BigDecimal b5 = new BigDecimal("7");
        BigDecimal divide = b4.divide(b5,3,BigDecimal.ROUND_HALF_DOWN);
        System.out.println(divide);

        // 约等于
        BigDecimal add1 = b1.add(b2);
        System.out.println(add1);
        BigDecimal bigDecimal = add1.setScale(3, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(bigDecimal);

    }
}
