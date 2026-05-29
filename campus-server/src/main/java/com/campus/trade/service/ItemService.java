package com.campus.trade.service;

import com.campus.trade.dto.ItemDTO;
import com.campus.trade.dto.ItemPageQueryDTO;
import com.campus.trade.dto.ItemSearchDTO;
import com.campus.trade.entity.Item;
import com.campus.trade.result.PageResult;
import com.campus.trade.vo.ItemDetailVO;
import com.campus.trade.vo.ItemVO;

import java.util.List;

public interface ItemService {
    void saveItem(ItemDTO itemDTO);

    PageResult page(ItemPageQueryDTO itemPageQueryDTO);

    void deleteByIds(List<Long> ids);

    void updateItem(ItemDTO itemDTO);

    ItemVO getById(Long id);

    ItemDetailVO getDetailById(Long id);

    List<Item> list(Long categoryId);

    List<ItemVO> listWithCondition(Item item);

    PageResult search(ItemSearchDTO itemSearchDTO);

    void updateSaleStatus(Integer saleStatus, Long id);

    void incrementViewCount(Long id);

    List<ItemVO> listBySellerId(Long sellerId);
}
