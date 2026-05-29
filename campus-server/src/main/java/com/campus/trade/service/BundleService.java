package com.campus.trade.service;

import com.campus.trade.dto.BundleDTO;
import com.campus.trade.dto.BundlePageQueryDTO;
import com.campus.trade.entity.Bundle;
import com.campus.trade.result.PageResult;
import com.campus.trade.vo.BundleItemVO;
import com.campus.trade.vo.BundleVO;

import java.util.List;

public interface BundleService {

    void saveWithItem(BundleDTO bundleDTO);

    PageResult pageQuery(BundlePageQueryDTO bundlePageQueryDTO);

    void deleteByIds(List<Long> ids);

    BundleVO getByIdWithItem(Long id);

    void updateWithItem(BundleDTO bundleDTO);

    void startOrStop(Integer status, Long id);

    List<Bundle> list(Bundle bundle);

    List<BundleItemVO> getItemByBundleId(Long id);
}
