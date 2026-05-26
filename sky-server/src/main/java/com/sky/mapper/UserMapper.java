package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户数据访问接口
 * 使用MyBatis注解方式定义数据库操作方法
 */
@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户信息，用于微信登录
     * @param openid 微信用户的唯一标识
     * @return 返回对应用户信息
     */
    @Select("select * from user where openid = #{openid}")
    User wxLogin(String openid);

    /**
     * 插入新用户信息
     * @param user 包含用户信息的实体对象
     */
    void insert(User user);

    /**
     * 根据用户ID查询用户信息
     * @param id 用户唯一标识
     * @return 返回对应用户信息
     */
    @Select("select * from user where id = #{id}")
    User selectById(Long id);

    /**
     * 统计指定时间区间内的新增用户数
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(id) from user where create_time >= #{begin} and create_time <= #{end}")
    Integer countByCreateTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 统计截止到指定时间的用户总量
     * @param end
     * @return
     */
    @Select("select count(id) from user where create_time <= #{end}")
    Integer countTotalByCreateTimeBefore(@Param("end") LocalDateTime end);

    /**
     * 根据条件统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
