package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车数据访问接口
 * 使用MyBatis的@Mapper注解标记为数据访问层接口
 */
@Mapper
public interface ShoppingCartMapper {
    /**
     * 查询购物车列表
     * @param shoppingCart 查询条件，包含用户ID、菜品ID等信息
     * @return 购物车列表
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 向购物车中添加商品
     * @param shoppingCart 要添加的购物车商品信息
     */
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 更新购物车商品数量
     * @param shoppingCart 包含商品ID和新数量的购物车对象
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);
    
    /**
     * 根据ID删除购物车商品
     * @param id 要删除的购物车商品ID
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
    
    /**
     * 根据用户ID清空购物车
     * @param userId 用户ID
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 批量插入购物车
     * @param shoppingCartList
     */
    void insertBatch(@Param("shoppingCartList") List<ShoppingCart> shoppingCartList);
}
