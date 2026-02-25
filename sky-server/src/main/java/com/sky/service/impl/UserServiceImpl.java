package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //调用微信接口，获取微信用户openid

//        Map<String, String> map = new HashMap<>();
//        map.put("appid",weChatProperties.getAppid());
//        map.put("secret",weChatProperties.getSecret());
//        map.put("js_code",userLoginDTO.getCode());
//        map.put("grant_type","authorization_code");
//        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);
//        JSONObject jsonObject = JSONObject.parseObject(json);
//        String openid = jsonObject.getString("openid");
        String openid = getOpenid(userLoginDTO.getCode());
        //openid为空表示失败,抛出业务异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //根据openid查询数据库，判断该用户是否存在
        User user = userMapper.getByOpenid(openid);

        //用户不存在，创建新用户
        if (user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
                    userMapper.insert(user);
        }
        //返回用户对象

        return user;
    }

    private String getOpenid(String code) {
        //调用微信接口，获取微信用户openid
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getString("openid");
    }
}
