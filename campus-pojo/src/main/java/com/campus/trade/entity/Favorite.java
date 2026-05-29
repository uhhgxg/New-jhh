package com.campus.trade.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //用户ID
    private Long userId;

    //商品ID
    private Long itemId;

    //收藏时间
    private LocalDateTime createTime;
}
