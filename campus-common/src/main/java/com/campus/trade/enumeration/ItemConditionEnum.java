package com.campus.trade.enumeration;

/**
 * 商品新旧程度枚举
 */
public enum ItemConditionEnum {

    BRAND_NEW(1, "全新"),
    LIKE_NEW(2, "九成新"),
    GOOD(3, "八成新"),
    FAIR(4, "七成新");

    private final Integer code;
    private final String desc;

    ItemConditionEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
