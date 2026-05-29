package com.campus.trade.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 捆绑包总览
 * 该类用于表示捆绑包的总览信息，实现了序列化接口
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleOverViewVO implements Serializable {
    // 已启售数量
    // 表示当前已经上架销售的捆绑包数量
    private Integer sold;

    // 已停售数量
    // 表示已经停止销售的捆绑包数量
    private Integer discontinued;
}
