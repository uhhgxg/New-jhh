package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品总览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemOverViewVO implements Serializable {
    // 已上架数量
    private Integer sold;

    // 已下架数量
    private Integer discontinued;
}
