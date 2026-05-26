package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车的方法
     * 该方法用于将商品添加到用户的购物车中，首先记录日志，然后创建购物车对象，
     * 复制属性并设置用户ID，最后查询购物车信息
     *
     * @param shoppingCartDTO 购物车数据传输对象，包含购物车相关信息
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车:{}", shoppingCartDTO);
        
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        } else {
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            } else {
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 减少购物车数量
     * 如果数量为1则直接删除，否则数量减1
     *
     * @param shoppingCartDTO 购物车数据传输对象
     */
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
    // 记录减少购物车数量的操作日志
        log.info("减少购物车数量:{}", shoppingCartDTO);
        
    // 创建购物车实体对象
        ShoppingCart shoppingCart = new ShoppingCart();
    // 将数据传输对象的属性复制到实体对象中
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        
    // 获取当前登录用户的ID
        Long userId = BaseContext.getCurrentId();
    // 设置购物车记录的用户ID
        shoppingCart.setUserId(userId);
        
    // 查询符合条件的购物车记录
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
    // 判断查询结果是否为空且是否有记录
        if (list != null && list.size() > 0) {
        // 获取第一条购物车记录
            ShoppingCart cart = list.get(0);
        // 判断商品数量是否为1
            if (cart.getNumber() == 1) {
            // 如果数量为1，则删除该购物车记录
                shoppingCartMapper.deleteById(cart.getId());
            } else {
            // 如果数量大于1，则将数量减1
                cart.setNumber(cart.getNumber() - 1);
            // 更新购物车记录的数量
                shoppingCartMapper.updateNumberById(cart);
            }
        }
    }

    /**
     * 查看购物车
     * 查询当前用户的所有购物车商品
     *
     * @return 购物车商品列表
     */
    public List<ShoppingCart> showShoppingCart() {
        log.info("查看购物车");
        
        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     * 删除当前用户的所有购物车商品
     */
    public void cleanShoppingCart() {
        log.info("清空购物车");
        
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}

