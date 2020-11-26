package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.ware.WareOrderTask;
import com.atguigu.gmall.model.ware.WareOrderTaskDetail;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.mq.service.RabbitService;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    RabbitService rabbitService;



    @Override
    public List<OrderDetail> getDetailArrayList(String userId) {

        List<OrderDetail> orderDetails = new ArrayList<>();

        // 将购物车被选中数据，转化成List<OrderDetail>
        List<CartInfo> cartInfos = cartFeignClient.getCartByUserId(userId);
        for (CartInfo cartInfo : cartInfos) {
            BigDecimal bigDecimal1 = new BigDecimal(cartInfo.getIsChecked()+"");
            BigDecimal bigDecimal2 = new BigDecimal("1");
            int i = bigDecimal1.compareTo(bigDecimal2);// -1 0 1
            if(i==0){
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetails.add(orderDetail);
            }

        }
        return orderDetails;
    }

    @Override
    public String genTradeNo(String userId) {

        String tradeNo = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("user:"+userId+":tradeNo",tradeNo);

        return tradeNo;
    }

    @Override
    public Boolean checkTradeNo(String tradeNo, String userId) {

        Boolean b = false;

        String tradeNoForCache = (String)redisTemplate.opsForValue().get("user:" + userId + ":tradeNo");

        if(null!=tradeNoForCache&&tradeNoForCache.equals(tradeNo)){
            b = true;
            // 删除已经使用的tradeNo
            redisTemplate.delete("user:" + userId + ":tradeNo");
        }
        return b;
    }

    @Override
    public OrderInfo submitOrder(OrderInfo orderInfo) {

        // 保存订单
        String outTradeNo = "atguigu";
        long currentTimeMillis = System.currentTimeMillis();
        outTradeNo += currentTimeMillis;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String format = sdf.format(new Date());
        outTradeNo+= format;
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
        orderInfo.setTotalAmount(getTotalAmount(orderInfo.getOrderDetailList()));
        orderInfo.setOrderComment("快来买");
        orderInfo.setCreateTime(new Date());
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,1);
        Date time = instance.getTime();
        orderInfo.setExpireTime(time);// 一般时24小时候过期
        orderInfo.setImgUrl(orderInfo.getOrderDetailList().get(0).getImgUrl());

        orderInfoMapper.insert(orderInfo);
        // 保存订单详情
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            // 校验库存
            // 校验价格

            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
        }
        return orderInfo;
    }

    @Override
    public OrderInfo getOrderById(Long orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);

        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper);

        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

    @Override
    public void updateOrder(Map<String, Object> map) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
        orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",(String)map.get("out_trade_no"));

        OrderInfo orderInfoDb = orderInfoMapper.selectOne(queryWrapper);
        OrderInfo orderById = getOrderById(orderInfoDb.getId());

        // 将gmall商城的order订单转化成库存系统的order订单
        WareOrderTask wareOrderTask = new WareOrderTask();
        List<WareOrderTaskDetail> orderTaskDetails = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderById.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            WareOrderTaskDetail wareOrderTaskDetail = new WareOrderTaskDetail();
            wareOrderTaskDetail.setSkuId(orderDetail.getSkuId()+"");
            wareOrderTaskDetail.setSkuNum(orderDetail.getSkuNum());
            wareOrderTaskDetail.setSkuName(orderDetail.getSkuName());
            orderTaskDetails.add(wareOrderTaskDetail);
        }

        wareOrderTask.setDetails(orderTaskDetails);
        wareOrderTask.setOrderId(orderById.getId()+"");
        wareOrderTask.setConsignee(orderById.getConsignee());
        wareOrderTask.setConsigneeTel(orderById.getConsigneeTel());
        wareOrderTask.setCreateTime(new Date());
        wareOrderTask.setPaymentWay(PaymentWay.ONLINE.getComment());
        wareOrderTask.setDeliveryAddress(orderById.getDeliveryAddress());

        // 发送订单支付完成的队列
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,MqConst.ROUTING_WARE_STOCK, JSON.toJSONString(wareOrderTask));

        orderInfoMapper.update(orderInfo,queryWrapper);


    }

    private BigDecimal getTotalAmount(List<OrderDetail> detailArrayList) {
        BigDecimal totalAmout = new BigDecimal("0");
        for (OrderDetail orderDetail : detailArrayList) {
            BigDecimal orderPrice = orderDetail.getOrderPrice();
            totalAmout = totalAmout.add(orderPrice);
        }
        return totalAmout;
    }

}
