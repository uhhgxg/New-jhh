package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * 菜品业务逻辑实现类
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品及口味信息
     * @param dishDTO 菜品数据传输对象，包含菜品基本信息和口味列表
     */
    @Override
    @Transactional
    public void saveWithFlavors(DishDTO dishDTO) {
        log.info("新增菜品及口味，菜品名：{}，口味数量：{}", dishDTO.getName(), 
                dishDTO.getFlavors() == null ? 0 : dishDTO.getFlavors().size());
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        log.info("菜品基本信息保存成功，dishId：{}", dish.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
            log.info("菜品口味保存成功，口味数量：{}", flavors.size());
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO 分页查询条件，包含页码、每页条数、菜品名称、分类ID、状态等
     * @return 分页结果，包含总记录数和菜品列表
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        
        long total = page.getTotal();
        List<DishVO> records = page.getResult();
        
        return new PageResult(total, records);
    }

    /**
     * 批量删除菜品
     * @param ids 待删除的菜品ID列表
     * @throws DeletionNotAllowedException 当菜品处于起售中或被套餐关联时抛出异常
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        log.info("批量删除菜品：{}", ids);

        // 判断当前菜品是否能够删除---是否处于起售中？？
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                // 当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断当前菜品是否能够删除---是否被套餐关联了？？
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            // 当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品表中的菜品数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
        }
        
        // 批量删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 修改菜品及口味信息
     * @param dishDTO 菜品数据传输对象，包含修改后的菜品信息和口味列表
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        log.info("修改菜品及口味，dishId：{}", dishDTO.getId());
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.update(dish);
        log.info("菜品基本信息修改成功");

        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        log.info("删除旧口味数据成功");

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
            log.info("新口味数据保存成功，口味数量：{}", flavors.size());
        }
    }

    /**
     * 根据ID查询菜品及关联的口味信息
     * @param id 菜品ID
     * @return 菜品详细信息，包含口味列表
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        log.info("根据ID查询菜品及口味，id：{}", id);
        Dish dish = dishMapper.getById(id);
        
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(flavors);
        log.info("查询到口味数量：{}", flavors == null ? 0 : flavors.size());

        return dishVO;
    }

    /**
     * 根据分类ID查询启用的菜品列表
     * @param categoryId 分类ID
     * @return 菜品列表
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品及口味信息
     * @param dish 查询条件
     * @return 菜品及口味列表
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
