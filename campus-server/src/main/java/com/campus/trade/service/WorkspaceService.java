package com.campus.trade.service;

import com.campus.trade.vo.BundleOverViewVO;
import com.campus.trade.vo.BusinessDataVO;
import com.campus.trade.vo.ItemOverViewVO;
import com.campus.trade.vo.TradeOrderOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {

    /**
     * 根据时间段统计营业数据
     *
     * @param begin
     * @param end
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询订单管理数据
     *
     * @return
     */
    TradeOrderOverViewVO getOrderOverView();

    /**
     * 查询商品总览
     *
     * @return
     */
    ItemOverViewVO getItemOverView();

    /**
     * 查询捆绑包总览
     *
     * @return
     */
    BundleOverViewVO getBundleOverView();
}
