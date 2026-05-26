
package com.sky.mapper;

import com.sky.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShopMapper {

    @Select("select * from shop where id = #{id}")
    Shop getById(Long id);

    @Update("update shop set status = #{status} where id = #{id}")
    void update(Shop shop);
}
