package com.campus.trade.mapper;

import com.campus.trade.entity.BundleItem;
import com.campus.trade.vo.BundleItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BundleItemMapper {

    List<Long> getBundleIdsByItemIds(@Param("itemIds") List<Long> itemIds);

    void deleteByBundleIds(@Param("bundleIds") List<Long> bundleIds);

    @Select("select * from bundle_item where bundle_id = #{bundleId}")
    List<BundleItem> getByBundleId(Long bundleId);

    void insertBatch(List<BundleItem> bundleItems);

    @Delete("delete from bundle_item where bundle_id = #{bundleId}")
    void deleteByBundleId(Long bundleId);

    @Select("select bi.name, bi.copies, i.images as image, i.item_description as description, i.sale_status as status " +
            "from bundle_item bi left join item i on bi.item_id = i.id " +
            "where bi.bundle_id = #{bundleId}")
    List<BundleItemVO> getItemByBundleId(Long bundleId);
}
