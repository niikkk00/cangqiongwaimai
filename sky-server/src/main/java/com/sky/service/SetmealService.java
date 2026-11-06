package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import java.util.List;

public interface SetmealService {
    void saveWithDish(SetmealDTO dto);
    PageResult pageQuery(SetmealPageQueryDTO query);
    void deleteBatch(List<Long> ids);
    SetmealVO getByIdWithDish(Long id);
    void update(SetmealDTO dto);
    void startOrStop(Integer status, Long id);
}
