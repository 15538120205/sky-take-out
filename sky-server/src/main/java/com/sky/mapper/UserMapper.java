package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);
    /**
     * 插入数据
     * @param user
     */
    void insert(User user);



    /**
     * 根据id查询用户信息
     *
     * @param userId
     * @return
     */
    User getById(Long userId);

    /**
     * 根据动态条件获取用户数
     *
     * @param map
     */
    Integer getUser(HashMap<String, Object> map);
}
