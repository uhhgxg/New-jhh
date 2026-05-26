package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     * 统计指定时间区间内，每日的营业额（已完成订单的金额合计）
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("营业额统计，begin：{}，end：{}", begin, end);

        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            // 查询当天已完成订单的营业额总和
            LocalDateTime beginTime = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(current, LocalTime.MAX);

            Map<String, Object> map = new HashMap<>();
            map.put("status", 5); // 已完成
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);

            BigDecimal turnover = ordersMapper.sumByMap(map);
            if (turnover == null) {
                turnover = BigDecimal.ZERO;
            }

            dateList.add(current.toString());
            turnoverList.add(turnover.toPlainString());

            current = current.plusDays(1);
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    /**
     * 用户统计
     * 统计指定时间区间内，每日的新增用户数和截止到当天的用户总量
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        log.info("用户统计，begin：{}，end：{}", begin, end);

        List<String> dateList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            LocalDateTime beginTime = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(current, LocalTime.MAX);

            // 新增用户数
            Integer newUserCount = userMapper.countByCreateTime(beginTime, endTime);
            // 用户总量
            Integer totalUserCount = userMapper.countTotalByCreateTimeBefore(endTime);

            dateList.add(current.toString());
            newUserList.add(String.valueOf(newUserCount));
            totalUserList.add(String.valueOf(totalUserCount));

            current = current.plusDays(1);
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .newUserList(String.join(",", newUserList))
                .totalUserList(String.join(",", totalUserList))
                .build();
    }

    /**
     * 订单统计
     * 统计指定时间区间内，每日的订单总数、有效订单数，以及总订单数、有效订单总数、完成率
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        log.info("订单统计，begin：{}，end：{}", begin, end);

        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();

        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            LocalDateTime beginTime = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(current, LocalTime.MAX);

            // 统计当日全部订单数
            Map<String, Object> allMap = new HashMap<>();
            allMap.put("beginTime", beginTime);
            allMap.put("endTime", endTime);
            Integer dailyOrderCount = ordersMapper.countByMap(allMap);

            // 统计当日有效订单数（已完成）
            Map<String, Object> validMap = new HashMap<>();
            validMap.put("status", 5);
            validMap.put("beginTime", beginTime);
            validMap.put("endTime", endTime);
            Integer dailyValidOrderCount = ordersMapper.countByMap(validMap);

            dateList.add(current.toString());
            orderCountList.add(String.valueOf(dailyOrderCount));
            validOrderCountList.add(String.valueOf(dailyValidOrderCount));

            totalOrderCount += dailyOrderCount;
            validOrderCount += dailyValidOrderCount;

            current = current.plusDays(1);
        }

        // 计算订单完成率
        double orderCompletionRate = 0.0;
        if (totalOrderCount > 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名top10
     * 统计指定时间区间内，已完成订单的商品销量排名（前10）
     *
     * @param begin 开始日期，统计的起始时间点
     * @param end 结束日期，统计的结束时间点
     * @return SalesTop10ReportVO 包含销量排名前10的商品名称列表和销量列表，用逗号分隔
     */ 
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
    // 记录开始日志，输入参数begin和end
        log.info("销量排名top10，begin：{}，end：{}", begin, end);

    // 将输入的LocalDate转换为当天的最小时间（开始时间）和最大时间（结束时间）
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

    // 查询指定时间区间内销量排名前10的商品数据
        List<GoodsSalesDTO> salesTop10 = orderDetailMapper.getSalesTop10(beginTime, endTime);

    // 使用Stream API提取商品名称列表，并用逗号连接成字符串
        List<String> nameList = salesTop10.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());
    // 使用Stream API提取商品销量列表，将数字转为字符串并用逗号连接
        List<String> numberList = salesTop10.stream()
                .map(item -> String.valueOf(item.getNumber()))
                .collect(Collectors.toList());

    // 构建并返回包含名称列表和销量列表的VO对象
        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    /**
     * 导出运营数据报表
     * 基于模板文件，填充最近30天的运营数据并下载
     *
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        // 1. 查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        BusinessDataVO businessData = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX));

        // 2. 基于模板创建 Excel 文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            // 填充时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            // 填充概览数据（第4行）
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            // 填充概览数据（第5行）
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            // 填充每日明细数据（第8行起）
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO dailyData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));

                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dailyData.getTurnover());
                row.getCell(3).setCellValue(dailyData.getValidOrderCount());
                row.getCell(4).setCellValue(dailyData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dailyData.getUnitPrice());
                row.getCell(6).setCellValue(dailyData.getNewUsers());
            }

            // 3. 输出到客户端下载
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=运营数据报表.xlsx");

            excel.write(response.getOutputStream());
            excel.close();
            in.close();
        } catch (IOException e) {
            log.error("导出运营数据报表失败", e);
        }
    }
}
