package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import com.sky.entity.Dish;

import java.util.List;

public interface DishService {
    List<Dish> list(Long categoryId);
    /**
     * 新增菜品和口味数据
     * @param dishDTO
     */

    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品和口味数据
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(long id);

    /**
     * 根据id修改菜品和口味数据
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);
}
