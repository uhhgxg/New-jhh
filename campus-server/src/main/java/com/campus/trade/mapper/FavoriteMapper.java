package com.campus.trade.mapper;

import com.campus.trade.entity.Favorite;
import com.campus.trade.vo.FavoriteVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏数据访问接口
 * 使用MyBatis注解方式定义数据库操作方法
 */
@Mapper
public interface FavoriteMapper {

    /**
     * 插入收藏记录
     * @param favorite 收藏对象，包含收藏相关信息
     */
    void insert(Favorite favorite);

    /**
     * 删除指定用户的收藏记录
     * @param userId 用户ID
     * @param itemId 商品ID
     */
    @Delete("delete from favorite where user_id = #{userId} and item_id = #{itemId}")
    void delete(@Param("userId") Long userId, @Param("itemId") Long itemId);

    /**
     * 根据用户ID和商品ID查询收藏记录
     * @param userId 用户ID
     * @param itemId 商品ID
     * @return Favorite 收藏对象
     */
    @Select("select * from favorite where user_id = #{userId} and item_id = #{itemId}")
    Favorite getByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);

    /**
     * 分页查询用户的收藏列表
     * @param userId 用户ID
     * @return List<FavoriteVO> 收藏视图对象列表
     */
    List<FavoriteVO> pageQuery(@Param("userId") Long userId);

    /**
     * 统计商品被收藏的数量
     * @param itemId 商品ID
     * @return Integer 收藏数量
     */
    @Select("select count(id) from favorite where item_id = #{itemId}")
    Integer countByItemId(Long itemId);
}
