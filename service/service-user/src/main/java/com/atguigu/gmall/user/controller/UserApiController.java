package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/user/passport")
public class UserApiController {

    @Autowired
    UserService userService;


    @RequestMapping("getUserAddressList/{userId}")
    List<UserAddress> getUserAddressList(@PathVariable("userId") String userId , HttpServletRequest request){

        String userIdTest = request.getHeader("userId");
        List<UserAddress> userAddresses = userService.getUserAddressList(userId);
        return userAddresses;
    }

    @RequestMapping("verify/{token}")
    public UserInfo verify(@PathVariable("token") String token) {

        UserInfo userInfo = userService.verify(token);
        return userInfo;
    }

    @RequestMapping("login")
    public Result login(@RequestBody UserInfo userInfo) {

        Map<String,Object> map = userService.login(userInfo);
        if (null!=map) {
            return Result.ok(map);
        } else {
            return Result.fail();
        }
    }
}
