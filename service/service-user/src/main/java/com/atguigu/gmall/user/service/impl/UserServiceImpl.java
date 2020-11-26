package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public Map<String, Object> login(UserInfo userInfo) {
        Map<String , Object> map = null;
        //验证
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name",userInfo.getLoginName());
        queryWrapper.eq("passwd", DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes()));
        UserInfo userInfoResult = userInfoMapper.selectOne(queryWrapper);

        if (userInfoResult!=null) {
            //token
            String token = UUID.randomUUID().toString();
            //存缓存
            redisTemplate.opsForValue().set("user:" + token,userInfoResult.getId());
            //返回结果
            map = new HashMap<>();
            map.put("name", userInfoResult.getName());
            map.put("nickName", userInfoResult.getNickName());
            map.put("token", token);
        }

        return map;
    }

    @Override
    public UserInfo verify(String token) {

        String userKeyPrefix = RedisConst.USER_KEY_PREFIX;

        UserInfo userInfo = null;
        //存到缓存里
        Integer userId = (Integer) redisTemplate.opsForValue().get("user:" + token);

        if (userId != null && userId > 0) {

            userInfo = userInfoMapper.selectById(userId);
        }
        return userInfo;
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {

        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        List<UserAddress> userAddresses = userAddressMapper.selectList(queryWrapper);
        return userAddresses;
    }
}
