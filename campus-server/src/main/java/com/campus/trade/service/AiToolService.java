package com.campus.trade.service;

import com.campus.trade.constant.StatusConstant;
import com.campus.trade.context.BaseContext;
import com.campus.trade.entity.Category;
import com.campus.trade.entity.Item;
import com.campus.trade.entity.TradeOrder;
import com.campus.trade.mapper.CategoryMapper;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.mapper.TradeOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI助手工具调用服务
 */
@Service
@Slf4j
public class AiToolService {

    @Autowired
    private TradeOrderMapper tradeOrderMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    private static final String[] TRADE_STATUS = {
        "", "待付款", "待发货", "已发货", "已完成", "已取消", "退款中", "已退款"
    };

    @Tool(description = "查询当前用户的所有交易订单，返回交易编号、交易状态、金额和交易时间等信息")
    public String queryMyOrders() {
        Long userId = BaseContext.getCurrentId();
        log.info("AI工具调用 - 查询用户订单, userId={}", userId);

        if (userId == null) {
            return "未获取到用户信息，请确认已登录。";
        }

        List<TradeOrder> orders = tradeOrderMapper.getByUserId(userId);
        if (orders == null || orders.isEmpty()) {
            return "您目前没有任何交易订单记录哦~";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("当前用户共有 ").append(orders.size()).append(" 个订单：\n");
        for (TradeOrder order : orders) {
            sb.append("- 交易编号: ").append(order.getTradeNo())
                    .append(", 状态: ").append(getStatusText(order.getTradeStatus()))
                    .append(", 金额: ").append(order.getTotalAmount()).append("元")
                    .append(", 交易时间: ").append(order.getTradeTime());
            if (order.getConsignee() != null) {
                sb.append(", 收货人: ").append(order.getConsignee());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Tool(description = "根据交易编号查询订单详情")
    public String queryOrderByNumber(
            @ToolParam(description = "交易编号") String tradeNo) {
        log.info("AI工具调用 - 查询订单详情, tradeNo={}", tradeNo);

        TradeOrder order = tradeOrderMapper.getByNumber(tradeNo);
        if (order == null) {
            return "未找到交易编号为 " + tradeNo + " 的订单，请确认编号是否正确。";
        }

        return String.format(
                "交易编号: %s\n状态: %s\n金额: %s元\n交易时间: %s\n收货人: %s\n联系电话: %s\n收货地址: %s\n备注: %s",
                order.getTradeNo(),
                getStatusText(order.getTradeStatus()),
                order.getTotalAmount(),
                order.getTradeTime(),
                order.getConsignee() != null ? order.getConsignee() : "无",
                order.getPhone() != null ? order.getPhone() : "无",
                order.getAddress() != null ? order.getAddress() : "无",
                order.getTradeRemark() != null && !order.getTradeRemark().isEmpty() ? order.getTradeRemark() : "无"
        );
    }

    @Tool(description = "根据关键词搜索二手商品，返回匹配的商品名称、价格、分类和描述")
    public String searchItems(
            @ToolParam(description = "搜索关键词，例如\"教材\"、\"手机\"、\"台灯\"") String keyword) {
        log.info("AI工具调用 - 搜索商品, keyword={}", keyword);

        List<Category> categories = categoryMapper.list(1);
        Item condition = Item.builder()
                .itemName(keyword)
                .saleStatus(StatusConstant.ON_SALE)
                .build();
        List<Item> items = itemMapper.list(condition);

        if (items == null || items.isEmpty()) {
            return "没有找到与\"" + keyword + "\"相关的商品哦~";
        }

        return formatItemResult(categories, items);
    }

    @Tool(description = "根据商品分类名称查询该分类下的所有商品")
    public String searchItemsByCategory(
            @ToolParam(description = "分类名称，例如\"教材图书\"、\"数码电子\"、\"生活用品\"") String categoryName) {
        log.info("AI工具调用 - 按分类查询商品, categoryName={}", categoryName);

        List<Category> allCategories = categoryMapper.list(1);
        Category matched = allCategories.stream()
                .filter(c -> c.getName() != null && c.getName().contains(categoryName))
                .findFirst()
                .orElse(null);

        if (matched == null) {
            return "没有找到名为\"" + categoryName + "\"的商品分类，请确认分类名称是否正确。";
        }

        List<Category> categories = categoryMapper.list(1);
        Item condition = Item.builder()
                .categoryId(matched.getId())
                .saleStatus(StatusConstant.ON_SALE)
                .build();
        List<Item> items = itemMapper.list(condition);

        if (items == null || items.isEmpty()) {
            return "\"" + matched.getName() + "\"分类下暂时没有商品哦~";
        }

        return "「" + matched.getName() + "」分类下的商品：\n" + formatItemResult(categories, items);
    }

    private String formatItemResult(List<Category> categories, List<Item> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("共找到 ").append(items.size()).append(" 个商品：\n");
        for (Item item : items) {
            String catName = categories.stream()
                    .filter(c -> c.getId().equals(item.getCategoryId()))
                    .findFirst()
                    .map(Category::getName)
                    .orElse("未分类");
            sb.append("- ").append(item.getItemName())
                    .append(", 价格: ").append(item.getUnitPrice()).append("元");
            if (item.getOriginalPrice() != null) {
                sb.append(" (原价: ").append(item.getOriginalPrice()).append("元)");
            }
            sb.append(", 分类: ").append(catName);
            if (item.getItemDescription() != null && !item.getItemDescription().isEmpty()) {
                sb.append(", 简介: ").append(item.getItemDescription());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String getStatusText(Integer status) {
        if (status == null || status < 1 || status >= TRADE_STATUS.length) {
            return "未知";
        }
        return TRADE_STATUS[status];
    }
}
