package com.campus.trade.service;

import com.campus.trade.dto.ShoppingCartDTO;
import com.campus.trade.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);
    
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
    
    List<ShoppingCart> showShoppingCart();
    
    void cleanShoppingCart();
}
