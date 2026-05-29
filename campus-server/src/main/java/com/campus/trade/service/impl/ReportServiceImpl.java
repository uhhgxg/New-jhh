package com.campus.trade.service.impl;

import com.campus.trade.dto.ItemSalesDTO;
import com.campus.trade.mapper.TradeOrderDetailMapper;
import com.campus.trade.mapper.TradeOrderMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.ReportService;
import com.campus.trade.service.WorkspaceService;
import com.campus.trade.vo.BusinessDataVO;
import com.campus.trade.vo.TradeOrderReportVO;
import com.campus.trade.vo.ItemSalesTop10VO;
import com.campus.trade.vo.TurnoverReportVO;
import com.campus.trade.vo.UserReportVO;
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
    private TradeOrderMapper tradeOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TradeOrderDetailMapper tradeOrderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;

    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("营业额统计，begin：{}，end：{}", begin, end);

        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            LocalDateTime beginTime = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(current, LocalTime.MAX);

            Map<String, Object> map = new HashMap<>();
            map.put("tradeStatus", 4); // 已完成
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);

            BigDecimal turnover = tradeOrderMapper.sumByMap(map);
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

    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        log.info("用户统计，begin：{}，end：{}", begin, end);

        List<String> dateList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            LocalDateTime beginTime = LocalDateTime.of(current, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(current, LocalTime.MAX);

            Integer newUserCount = userMapper.countByCreateTime(beginTime, endTime);
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

    public TradeOrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
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

            Map<String, Object> allMap = new HashMap<>();
            allMap.put("beginTime", beginTime);
            allMap.put("endTime", endTime);
            Integer dailyOrderCount = tradeOrderMapper.countByMap(allMap);

            Map<String, Object> validMap = new HashMap<>();
            validMap.put("tradeStatus", 4);
            validMap.put("beginTime", beginTime);
            validMap.put("endTime", endTime);
            Integer dailyValidOrderCount = tradeOrderMapper.countByMap(validMap);

            dateList.add(current.toString());
            orderCountList.add(String.valueOf(dailyOrderCount));
            validOrderCountList.add(String.valueOf(dailyValidOrderCount));

            totalOrderCount += dailyOrderCount;
            validOrderCount += dailyValidOrderCount;

            current = current.plusDays(1);
        }

        double orderCompletionRate = 0.0;
        if (totalOrderCount > 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        return TradeOrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    public ItemSalesTop10VO getSalesTop10(LocalDate begin, LocalDate end) {
        log.info("销量排名top10，begin：{}，end：{}", begin, end);

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<ItemSalesDTO> salesTop10 = tradeOrderDetailMapper.getSalesTop10(beginTime, endTime);

        List<String> nameList = salesTop10.stream()
                .map(ItemSalesDTO::getName)
                .collect(Collectors.toList());
        List<String> numberList = salesTop10.stream()
                .map(item -> String.valueOf(item.getNumber()))
                .collect(Collectors.toList());

        return ItemSalesTop10VO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    public void exportBusinessData(HttpServletResponse response) {
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        BusinessDataVO businessData = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX));

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

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
