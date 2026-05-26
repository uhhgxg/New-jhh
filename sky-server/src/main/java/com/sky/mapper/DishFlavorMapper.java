package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    @AutoFill(value = OperationType.INSERT)
    void insertBatch(List<DishFlavor> flavors);

    void deleteByDishIds(@Param("dishIds") List<Long> dishIds);

    void deleteByDishId(Long dishId);

    /**
     * 根据菜品ID查询口味数据
     * @param dishId
     * @return
     */
    List<DishFlavor> getByDishId(Long dishId);
}
