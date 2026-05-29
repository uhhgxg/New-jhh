package com.campus.trade.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.campus.trade.constant.MessageConstant;
import com.campus.trade.constant.StatusConstant;
import com.campus.trade.context.BaseContext;
import com.campus.trade.dto.CategoryDTO;
import com.campus.trade.dto.CategoryPageQueryDTO;
import com.campus.trade.entity.Category;
import com.campus.trade.exception.DeletionNotAllowedException;
import com.campus.trade.mapper.CategoryMapper;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.mapper.BundleMapper;
import com.campus.trade.result.PageResult;
import com.campus.trade.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private BundleMapper bundleMapper;

    public void save(CategoryDTO categoryDTO) {
        log.info("新增分类：{}", categoryDTO);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        category.setStatus(StatusConstant.DISABLE);

        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.insert(category);
        log.info("分类新增成功，id：{}", category.getId());
    }

    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询分类，页码：{}，每页条数：{}", categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        log.info("查询到分类总数：{}", page.getTotal());
        return new PageResult(page.getTotal(), page.getResult());
    }

    public void deleteById(Long id) {
        log.info("删除分类，id：{}", id);
        Integer count = itemMapper.countByCategoryId(id);
        if(count > 0){
            log.warn("删除分类失败，该分类下有关联商品，id：{}", id);
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_ITEM);
        }

        count = bundleMapper.countByCategoryId(id);
        if(count > 0){
            log.warn("删除分类失败，该分类下有关联捆绑包，id：{}", id);
            throw new DeletionNotAllowedException(MessageConstant.ITEM_BE_RELATED_BY_BUNDLE);
        }

        categoryMapper.deleteById(id);
        log.info("分类删除成功，id：{}", id);
    }

    public void update(CategoryDTO categoryDTO) {
        log.info("修改分类：{}", categoryDTO);
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);

        categoryMapper.update(category);
        log.info("分类修改成功，id：{}", category.getId());
    }

    public void startOrStop(Integer status, Long id) {
        log.info("启用/禁用分类，id：{}，status：{}", id, status);
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.update(category);
        log.info("分类状态修改成功，id：{}，新状态：{}", id, status == 1 ? "启用" : "禁用");
    }

    public List<Category> list(Integer type) {
        log.info("根据类型查询分类，type：{}", type);
        List<Category> list = categoryMapper.list(type);
        log.info("查询到分类数量：{}", list == null ? 0 : list.size());
        return list;
    }
}
