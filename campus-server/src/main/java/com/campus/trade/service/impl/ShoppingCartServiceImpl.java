package com.campus.trade.service.impl;

import com.campus.trade.context.BaseContext;
import com.campus.trade.dto.ShoppingCartDTO;
import com.campus.trade.entity.Bundle;
import com.campus.trade.entity.Item;
import com.campus.trade.entity.ShoppingCart;
import com.campus.trade.mapper.BundleMapper;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.mapper.ShoppingCartMapper;
import com.campus.trade.service.ShoppingCartService;
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
    private ItemMapper itemMapper;

    @Autowired
    private BundleMapper bundleMapper;

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
            Long itemId = shoppingCartDTO.getItemId();
            if (itemId != null) {
                Item item = itemMapper.getById(itemId);
                shoppingCart.setName(item.getItemName());
                shoppingCart.setAmount(item.getUnitPrice());
                shoppingCart.setImage(item.getImages());
            } else {
                Long bundleId = shoppingCartDTO.getBundleId();
                Bundle bundle = bundleMapper.getById(bundleId);
                shoppingCart.setName(bundle.getName());
                shoppingCart.setAmount(bundle.getPrice());
                shoppingCart.setImage(bundle.getImage());
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
        log.info("减少购物车数量:{}", shoppingCartDTO);

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            if (cart.getNumber() == 1) {
                shoppingCartMapper.deleteById(cart.getId());
            } else {
                cart.setNumber(cart.getNumber() - 1);
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
