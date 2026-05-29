package com.campus.trade.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ShoppingCartDTO implements Serializable {

    private Long itemId;
    private Long bundleId;
}
