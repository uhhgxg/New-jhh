package com.campus.trade.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.trade.constant.MessageConstant;
import com.campus.trade.constant.StatusConstant;
import com.campus.trade.dto.ItemDTO;
import com.campus.trade.dto.ItemPageQueryDTO;
import com.campus.trade.dto.ItemSearchDTO;
import com.campus.trade.entity.Item;
import com.campus.trade.exception.DeletionNotAllowedException;
import com.campus.trade.mapper.BundleItemMapper;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.result.PageResult;
import com.campus.trade.service.ItemService;
import com.campus.trade.vo.ItemDetailVO;
import com.campus.trade.vo.ItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private BundleItemMapper bundleItemMapper;

    @Autowired
    private com.campus.trade.mapper.UserMapper userMapper;

    /**
     * 新增商品
     */
    @Override
    @Transactional
    public void saveItem(ItemDTO itemDTO) {
        log.info("新增商品：{}", itemDTO.getItemName());
        Item item = new Item();
        BeanUtils.copyProperties(itemDTO, item);
        itemMapper.insert(item);
        log.info("商品保存成功，itemId：{}", item.getId());
    }

    /**
     * 商品分页查询
     */
    @Override
    public PageResult page(ItemPageQueryDTO itemPageQueryDTO) {
        PageHelper.startPage(itemPageQueryDTO.getPage(), itemPageQueryDTO.getPageSize());
        Page<ItemVO> page = itemMapper.pageQuery(itemPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除商品
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        log.info("批量删除商品：{}", ids);

        // 判断当前商品是否处于上架中
        for (Long id : ids) {
            Item item = itemMapper.getById(id);
            if (item.getSaleStatus() != null && item.getSaleStatus().equals(StatusConstant.ON_SALE)) {
                throw new DeletionNotAllowedException(MessageConstant.ITEM_ON_SALE);
            }
        }

        // 判断当前商品是否被捆绑包关联了
        List<Long> bundleIds = bundleItemMapper.getBundleIdsByItemIds(ids);
        if (bundleIds != null && !bundleIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.ITEM_BE_RELATED_BY_BUNDLE);
        }

        // 批量删除商品
        for (Long id : ids) {
            itemMapper.deleteById(id);
        }
        log.info("商品批量删除成功");
    }

    /**
     * 修改商品信息
     */
    @Override
    @Transactional
    public void updateItem(ItemDTO itemDTO) {
        log.info("修改商品，itemId：{}", itemDTO.getId());
        Item item = new Item();
        BeanUtils.copyProperties(itemDTO, item);
        itemMapper.update(item);
        log.info("商品修改成功");
    }

    /**
     * 根据ID查询商品
     */
    @Override
    public ItemVO getById(Long id) {
        log.info("根据ID查询商品，id：{}", id);
        Item item = itemMapper.getById(id);
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(item, itemVO);
        return itemVO;
    }

    /**
     * 根据ID查询商品详情
     */
    @Override
    public ItemDetailVO getDetailById(Long id) {
        log.info("根据ID查询商品详情，id：{}", id);
        Item item = itemMapper.getById(id);
        if (item == null) {
            return null;
        }
        ItemDetailVO itemDetailVO = new ItemDetailVO();
        BeanUtils.copyProperties(item, itemDetailVO);

        // 填充卖家名称
        if (item.getSellerId() != null) {
            com.campus.trade.entity.User seller = userMapper.selectById(item.getSellerId());
            if (seller != null) {
                itemDetailVO.setSellerName(seller.getName());
            }
        }

        return itemDetailVO;
    }

    /**
     * 根据分类ID查询商品列表
     */
    @Override
    public List<Item> list(Long categoryId) {
        Item item = Item.builder()
                .categoryId(categoryId)
                .saleStatus(StatusConstant.ON_SALE)
                .build();
        return itemMapper.list(item);
    }

    /**
     * 条件查询商品
     */
    @Override
    public List<ItemVO> listWithCondition(Item item) {
        List<Item> itemList = itemMapper.list(item);
        List<ItemVO> itemVOList = new ArrayList<>();
        for (Item i : itemList) {
            ItemVO itemVO = new ItemVO();
            BeanUtils.copyProperties(i, itemVO);
            itemVOList.add(itemVO);
        }
        return itemVOList;
    }

    /**
     * 商品搜索
     */
    @Override
    public PageResult search(ItemSearchDTO itemSearchDTO) {
        ItemPageQueryDTO pageQueryDTO = new ItemPageQueryDTO();
        pageQueryDTO.setPage(itemSearchDTO.getPage());
        pageQueryDTO.setPageSize(itemSearchDTO.getPageSize());
        pageQueryDTO.setItemName(itemSearchDTO.getKeyword());
        pageQueryDTO.setCategoryId(itemSearchDTO.getCategoryId());
        pageQueryDTO.setMinPrice(itemSearchDTO.getMinPrice());
        pageQueryDTO.setMaxPrice(itemSearchDTO.getMaxPrice());
        pageQueryDTO.setConditionLevel(itemSearchDTO.getConditionLevel());
        pageQueryDTO.setSaleStatus(itemSearchDTO.getSaleStatus());

        PageHelper.startPage(itemSearchDTO.getPage(), itemSearchDTO.getPageSize());
        Page<ItemVO> page = itemMapper.pageQuery(pageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 修改商品销售状态
     */
    @Override
    public void updateSaleStatus(Integer saleStatus, Long id) {
        log.info("修改商品销售状态，id：{}，saleStatus：{}", id, saleStatus);
        Item item = Item.builder()
                .id(id)
                .saleStatus(saleStatus)
                .build();
        itemMapper.update(item);
    }

    /**
     * 查询某卖家上架的商品
     */
    @Override
    public List<ItemVO> listBySellerId(Long sellerId) {
        log.info("查询卖家商品，sellerId：{}", sellerId);
        Item item = Item.builder()
                .sellerId(sellerId)
                .saleStatus(StatusConstant.ON_SALE)
                .build();
        return listWithCondition(item);
    }

    /**
     * 增加商品浏览量
     */
    @Override
    public void incrementViewCount(Long id) {
        log.info("增加商品浏览量，id：{}", id);
        Item item = itemMapper.getById(id);
        if (item != null) {
            Integer currentCount = item.getViewCount();
            if (currentCount == null) {
                currentCount = 0;
            }
            Item updateItem = Item.builder()
                    .id(id)
                    .viewCount(currentCount + 1)
                    .build();
            itemMapper.update(updateItem);
        }
    }
}
