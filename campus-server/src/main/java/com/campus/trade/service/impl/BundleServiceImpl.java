package com.campus.trade.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.trade.constant.MessageConstant;
import com.campus.trade.constant.StatusConstant;
import com.campus.trade.dto.BundleDTO;
import com.campus.trade.dto.BundlePageQueryDTO;
import com.campus.trade.entity.Bundle;
import com.campus.trade.entity.BundleItem;
import com.campus.trade.entity.Item;
import com.campus.trade.exception.DeletionNotAllowedException;
import com.campus.trade.exception.BundleEnableFailedException;
import com.campus.trade.mapper.BundleItemMapper;
import com.campus.trade.mapper.BundleMapper;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.result.PageResult;
import com.campus.trade.service.BundleService;
import com.campus.trade.vo.BundleItemVO;
import com.campus.trade.vo.BundleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class BundleServiceImpl implements BundleService {

    @Autowired
    private BundleMapper bundleMapper;

    @Autowired
    private BundleItemMapper bundleItemMapper;

    @Autowired
    private ItemMapper itemMapper;

    /**
     * 新增捆绑包，同时保存捆绑包和商品的关联关系
     */
    @Override
    @Transactional
    public void saveWithItem(BundleDTO bundleDTO) {
        log.info("新增捆绑包：{}", bundleDTO.getName());
        Bundle bundle = new Bundle();
        BeanUtils.copyProperties(bundleDTO, bundle);

        // 向捆绑包表插入数据
        bundleMapper.insert(bundle);

        // 获取生成的捆绑包id
        Long bundleId = bundle.getId();

        // 保存捆绑包和商品的关联关系
        List<BundleItem> bundleItems = bundleDTO.getBundleItems();
        if (bundleItems != null && !bundleItems.isEmpty()) {
            bundleItems.forEach(bundleItem -> {
                bundleItem.setBundleId(bundleId);
            });
            bundleItemMapper.insertBatch(bundleItems);
        }
        log.info("捆绑包及其商品关联保存成功，bundleId：{}", bundleId);
    }

    /**
     * 捆绑包分页查询
     */
    @Override
    public PageResult pageQuery(BundlePageQueryDTO bundlePageQueryDTO) {
        int pageNum = bundlePageQueryDTO.getPage();
        int pageSize = bundlePageQueryDTO.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        Page<BundleVO> page = bundleMapper.pageQuery(bundlePageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除捆绑包
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        log.info("批量删除捆绑包：{}", ids);

        // 判断捆绑包是否处于启用中
        ids.forEach(id -> {
            Bundle bundle = bundleMapper.getById(id);
            if (bundle.getStatus() != null && bundle.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.BUNDLE_ON_SALE);
            }
        });

        // 删除捆绑包和商品的关联关系
        bundleItemMapper.deleteByBundleIds(ids);

        // 删除捆绑包
        bundleMapper.deleteByIds(ids);
        log.info("捆绑包批量删除成功");
    }

    /**
     * 根据ID查询捆绑包及关联的商品数据
     */
    @Override
    public BundleVO getByIdWithItem(Long id) {
        log.info("根据ID查询捆绑包及商品，id：{}", id);
        Bundle bundle = bundleMapper.getById(id);
        List<BundleItem> bundleItems = bundleItemMapper.getByBundleId(id);

        BundleVO bundleVO = new BundleVO();
        BeanUtils.copyProperties(bundle, bundleVO);
        bundleVO.setBundleItems(bundleItems);

        return bundleVO;
    }

    /**
     * 修改捆绑包及商品关联
     */
    @Override
    @Transactional
    public void updateWithItem(BundleDTO bundleDTO) {
        log.info("修改捆绑包及商品，bundleId：{}", bundleDTO.getId());
        Bundle bundle = new Bundle();
        BeanUtils.copyProperties(bundleDTO, bundle);

        // 修改捆绑包基本信息
        bundleMapper.update(bundle);

        Long bundleId = bundleDTO.getId();

        // 删除当前捆绑包关联的商品数据
        bundleItemMapper.deleteByBundleId(bundleId);

        // 重新插入当前捆绑包关联的商品数据
        List<BundleItem> bundleItems = bundleDTO.getBundleItems();
        if (bundleItems != null && !bundleItems.isEmpty()) {
            bundleItems.forEach(bundleItem -> {
                bundleItem.setBundleId(bundleId);
            });
            bundleItemMapper.insertBatch(bundleItems);
        }
        log.info("捆绑包及商品关联修改成功");
    }

    /**
     * 捆绑包启用停用
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        log.info("捆绑包启用停用，id：{}，status：{}", id, status);

        // 启用捆绑包时，判断捆绑包内是否有下架商品
        if (status.equals(StatusConstant.ENABLE)) {
            List<BundleItemVO> itemList = bundleItemMapper.getItemByBundleId(id);
            if (itemList != null && !itemList.isEmpty()) {
                itemList.forEach(item -> {
                    if (item.getStatus() != null && item.getStatus().equals(StatusConstant.OFF_SALE)) {
                        throw new BundleEnableFailedException(MessageConstant.BUNDLE_ENABLE_FAILED);
                    }
                });
            }
        }

        Bundle bundle = Bundle.builder()
                .id(id)
                .status(status)
                .build();
        bundleMapper.update(bundle);
        log.info("捆绑包状态修改成功");
    }

    /**
     * 条件查询捆绑包
     */
    @Override
    public List<Bundle> list(Bundle bundle) {
        return bundleMapper.list(bundle);
    }

    /**
     * 根据捆绑包ID查询商品选项
     */
    @Override
    public List<BundleItemVO> getItemByBundleId(Long id) {
        return bundleItemMapper.getItemByBundleId(id);
    }
}
