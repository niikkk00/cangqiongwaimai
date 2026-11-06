package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import com.sky.entity.SetmealDish;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealDishIds(List<Long> dishIds);

    void insertBatch(@Param("setmealDishes") List<SetmealDish> setmealDishes);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId} order by id")
    List<SetmealDish> getBySetmealId(Long setmealId);

    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
