package com.campus.trade.mapper;

import com.github.pagehelper.Page;
import com.campus.trade.annotation.AutoFill;
import com.campus.trade.dto.ItemPageQueryDTO;
import com.campus.trade.entity.Item;
import com.campus.trade.enumeration.OperationType;
import com.campus.trade.vo.ItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ItemMapper {

    @Select("select count(id) from item where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void insert(Item item);

    Page<ItemVO> pageQuery(ItemPageQueryDTO itemPageQueryDTO);

    @Select("SELECT * FROM item WHERE id = #{id}")
    Item getById(Long id);

    @Delete("DELETE FROM item WHERE id = #{id}")
    void deleteById(Long id);

    void deleteByIds(List<Long> ids);

    @AutoFill(value = OperationType.UPDATE)
    void update(Item item);

    List<Item> list(Item item);

    Integer countByMap(Map map);
}
