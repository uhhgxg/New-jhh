package com.campus.trade.service;

import com.campus.trade.vo.ItemSalesTop10VO;
import com.campus.trade.vo.TradeOrderReportVO;
import com.campus.trade.vo.TurnoverReportVO;
import com.campus.trade.vo.UserReportVO;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    TradeOrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    /**
     * 商品销量排名top10
     *
     * @param begin
     * @param end
     * @return
     */
    ItemSalesTop10VO getSalesTop10(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据报表
     *
     * @param response
     */
    void exportBusinessData(HttpServletResponse response);
}
