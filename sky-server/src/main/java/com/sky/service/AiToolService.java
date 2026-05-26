package com.sky.service;

import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI助手工具调用服务
 * <p>
 * 提供可供AI模型调用的业务工具方法，包括订单查询、菜品查询等。
 * 方法上标注 {@link Tool} 注解后，Spring AI 会自动将其注册为可调用工具。
 * </p>
 */
@Service
@Slf4j
public class AiToolService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    private static final String[] ORDER_STATUS = {
        "", "待付款", "待接单", "已接单", "派送中", "已完成", "已取消", "退款"
    };

    // ==================== 订单查询工具 ====================

    /**
     * 查询当前登录用户的所有订单
     */
    @Tool(description = "查询当前用户的所有订单，返回订单号、订单状态、金额和下单时间等信息")
    public String queryMyOrders() {
        Long userId = BaseContext.getCurrentId();
        log.info("AI工具调用 - 查询用户订单, userId={}", userId);

        if (userId == null) {
            return "未获取到用户信息，请确认已登录。";
        }

        List<Orders> orders = ordersMapper.getByUserId(userId);
        if (orders == null || orders.isEmpty()) {
            return "您目前没有任何订单记录哦~";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("当前用户共有 ").append(orders.size()).append(" 个订单：\n");
        for (Orders order : orders) {
            sb.append("- 订单号: ").append(order.getNumber())
                    .append(", 状态: ").append(getStatusText(order.getStatus()))
                    .append(", 金额: ").append(order.getAmount()).append("元")
                    .append(", 下单时间: ").append(order.getOrderTime());
            if (order.getConsignee() != null) {
                sb.append(", 收货人: ").append(order.getConsignee());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 根据订单号查询订单详情
     */
    @Tool(description = "根据订单号查询订单详情，包含订单状态、金额、收货地址等信息")
    public String queryOrderByNumber(
            @ToolParam(description = "订单号，例如 20240525123456789") String orderNumber) {
        log.info("AI工具调用 - 查询订单详情, orderNumber={}", orderNumber);

        Orders order = ordersMapper.getByNumber(orderNumber);
        if (order == null) {
            return "未找到订单号为 " + orderNumber + " 的订单，请确认订单号是否正确。";
        }

        return String.format(
                "订单号: %s\n状态: %s\n金额: %s元\n下单时间: %s\n收货人: %s\n联系电话: %s\n收货地址: %s\n备注: %s",
                order.getNumber(),
                getStatusText(order.getStatus()),
                order.getAmount(),
                order.getOrderTime(),
                order.getConsignee() != null ? order.getConsignee() : "无",
                order.getPhone() != null ? order.getPhone() : "无",
                order.getAddress() != null ? order.getAddress() : "无",
                order.getRemark() != null && !order.getRemark().isEmpty() ? order.getRemark() : "无"
        );
    }

    // ==================== 菜品查询工具 ====================

    /**
     * 按名称搜索菜品
     */
    @Tool(description = "根据关键词搜索菜品，返回匹配的菜品名称、价格、分类和描述。可用于搜索特定类型或口味的菜品")
    public String searchDishes(
            @ToolParam(description = "搜索关键词，例如\"麻辣\"、\"鱼\"、\"素菜\"") String keyword) {
        log.info("AI工具调用 - 搜索菜品, keyword={}", keyword);

        List<Category> categories = categoryMapper.list(1);
        Dish condition = Dish.builder()
                .name(keyword)
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish> dishes = dishMapper.list(condition);

        if (dishes == null || dishes.isEmpty()) {
            return "没有找到与\"" + keyword + "\"相关的菜品哦~";
        }

        return formatDishResult(categories, dishes);
    }

    /**
     * 按分类查询菜品
     */
    @Tool(description = "根据菜品分类名称查询该分类下的所有菜品。分类名称例如：热菜、凉菜、主食、汤羹、饮品、甜品等")
    public String searchDishesByCategory(
            @ToolParam(description = "菜品分类名称，例如\"热菜\"、\"凉菜\"、\"主食\"、\"饮品\"") String categoryName) {
        log.info("AI工具调用 - 按分类查询菜品, categoryName={}", categoryName);

        // 查找匹配的分类
        List<Category> allCategories = categoryMapper.list(1);
        Category matched = allCategories.stream()
                .filter(c -> c.getName() != null && c.getName().contains(categoryName))
                .findFirst()
                .orElse(null);

        if (matched == null) {
            return "没有找到名为\"" + categoryName + "\"的菜品分类，请确认分类名称是否正确。";
        }

        List<Category> categories = categoryMapper.list(1);
        Dish condition = Dish.builder()
                .categoryId(matched.getId())
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish> dishes = dishMapper.list(condition);

        if (dishes == null || dishes.isEmpty()) {
            return "\"" + matched.getName() + "\"分类下暂时没有菜品哦~";
        }

        return "「" + matched.getName() + "」分类下的菜品：\n" + formatDishResult(categories, dishes);
    }

    /**
     * 格式化菜品列表为可读字符串
     */
    private String formatDishResult(List<Category> categories, List<Dish> dishes) {
        StringBuilder sb = new StringBuilder();
        sb.append("共找到 ").append(dishes.size()).append(" 个菜品：\n");
        for (Dish dish : dishes) {
            String catName = categories.stream()
                    .filter(c -> c.getId().equals(dish.getCategoryId()))
                    .findFirst()
                    .map(Category::getName)
                    .orElse("未分类");
            sb.append("- ").append(dish.getName())
                    .append(", 价格: ").append(dish.getPrice()).append("元")
                    .append(", 分类: ").append(catName);
            if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                sb.append(", 简介: ").append(dish.getDescription());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 将订单状态码转换为中文描述
     */
    private String getStatusText(Integer status) {
        if (status == null || status < 1 || status >= ORDER_STATUS.length) {
            return "未知";
        }
        return ORDER_STATUS[status];
    }
}
