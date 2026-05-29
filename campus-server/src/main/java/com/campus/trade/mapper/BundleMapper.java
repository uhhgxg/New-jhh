package com.campus.trade.mapper;

import com.github.pagehelper.Page;
import com.campus.trade.annotation.AutoFill;
import com.campus.trade.dto.BundlePageQueryDTO;
import com.campus.trade.entity.Bundle;
import com.campus.trade.enumeration.OperationType;
import com.campus.trade.vo.BundleItemVO;
import com.campus.trade.vo.BundleVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * BundleMapper接口 - 用于处理Bundle(捆绑包)相关的数据库操作
 * 使用MyBatis注解来定义SQL语句
 */
@Mapper
public interface BundleMapper {

    /**
     * 根据分类ID查询捆绑包数量
     * @param id 分类ID
     * @return 该分类下的捆绑包数量
     */
    @Select("select count(id) from bundle where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 插入新的捆绑包信息
     * @param bundle 捆绑包对象
     * @AutoFill注解表示在插入操作时自动填充某些字段
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Bundle bundle);

    /**
     * 分页查询捆绑包信息
     * @param bundlePageQueryDTO 捆绑包分页查询条件对象
     * @return 分页查询结果，包含捆绑包列表和分页信息
     */
    Page<BundleVO> pageQuery(BundlePageQueryDTO bundlePageQueryDTO);

    /**
     * 根据ID查询捆绑包信息
     * @param id 捆绑包ID
     * @return 捆绑包对象
     */
    @Select("select * from bundle where id = #{id}")
    Bundle getById(Long id);

    /**
     * 根据ID删除捆绑包
     * @param id 捆绑包ID
     */
    @Delete("delete from bundle where id = #{id}")
    void deleteById(Long id);

    /**
     * 更新捆绑包信息
     * @param bundle 捆绑包对象
     * @AutoFill注解表示在更新操作时自动填充某些字段
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Bundle bundle);

    /**
     * 根据ID列表批量删除捆绑包
     * @param ids 捆绑包ID列表
     */
    void deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 查询捆绑包列表
     * @param bundle 捆绑包查询条件对象
     * @return 符合条件的捆绑包列表
     */
    List<Bundle> list(Bundle bundle);

    @Select("select bi.name, bi.copies, i.images as image, i.item_description as description " +
            "from bundle_item bi left join item i on bi.item_id = i.id " +
            "where bi.bundle_id = #{bundleId}")
    List<BundleItemVO> getItemByBundleId(Long bundleId);

    Integer countByMap(Map map);
}
