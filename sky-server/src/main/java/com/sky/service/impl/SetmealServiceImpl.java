package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Transactional
    public void saveWithDish(SetmealDTO dto) {
        validateAndPrepare(dto, null);
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(dto, setmeal);
        setmeal.setId(null);
        // 新增套餐一律默认停售，通过起售接口发布。
        setmeal.setStatus(StatusConstant.DISABLE);
        try {
            setmealMapper.insert(setmeal);
        } catch (DuplicateKeyException ex) {
            throw new BaseException("套餐名称已存在");
        }
        saveRelations(setmeal.getId(), dto.getSetmealDishes());
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO query) {
        if (query.getPage() < 1 || query.getPageSize() < 1) {
            throw new BaseException("页码和每页记录数必须大于0");
        }
        if (query.getStatus() != null) {
            validateStatus(query.getStatus());
        }
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(query);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BaseException("请选择需要删除的套餐");
        }
        // 全部校验通过后再删除，避免批量操作只删除一部分。
        for (Long id : ids) {
            if (StatusConstant.ENABLE.equals(requireSetmeal(id).getStatus())) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        for (Long id : new HashSet<>(ids)) {
            setmealDishMapper.deleteBySetmealId(id);
            setmealMapper.deleteById(id);
        }
    }

    @Override
    public SetmealVO getByIdWithDish(Long id) {
        SetmealVO vo = setmealMapper.getDetailById(id);
        if (vo == null) {
            throw new BaseException("套餐不存在");
        }
        vo.setSetmealDishes(setmealDishMapper.getBySetmealId(id));
        return vo;
    }

    @Override
    @Transactional
    public void update(SetmealDTO dto) {
        Setmeal existing = requireSetmeal(dto.getId());
        validateAndPrepare(dto, existing.getId());
        Integer status = dto.getStatus() == null ? existing.getStatus() : dto.getStatus();
        validateStatus(status);
        if (StatusConstant.ENABLE.equals(status)) {
            checkDishesOnSale(dto.getSetmealDishes());
        }
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(dto, setmeal);
        setmeal.setStatus(status);
        try {
            setmealMapper.update(setmeal);
        } catch (DuplicateKeyException ex) {
            throw new BaseException("套餐名称已存在");
        }
        setmealDishMapper.deleteBySetmealId(setmeal.getId());
        saveRelations(setmeal.getId(), dto.getSetmealDishes());
    }

    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        validateStatus(status);
        requireSetmeal(id);
        if (StatusConstant.ENABLE.equals(status)) {
            checkDishesOnSale(setmealDishMapper.getBySetmealId(id));
        }
        setmealMapper.update(Setmeal.builder().id(id).status(status).build());
    }

    private Setmeal requireSetmeal(Long id) {
        if (id == null || id <= 0) {
            throw new BaseException("套餐id不能为空且必须大于0");
        }
        Setmeal setmeal = setmealMapper.getById(id);
        if (setmeal == null) {
            throw new BaseException("套餐不存在");
        }
        return setmeal;
    }

    private void validateStatus(Integer status) {
        if (!StatusConstant.ENABLE.equals(status) && !StatusConstant.DISABLE.equals(status)) {
            throw new BaseException("套餐状态只能为0或1");
        }
    }

    private void validateAndPrepare(SetmealDTO dto, Long currentId) {
        if (!StringUtils.hasText(dto.getName()) || dto.getCategoryId() == null
                || dto.getPrice() == null || dto.getPrice().signum() < 0
                || !StringUtils.hasText(dto.getImage())) {
            throw new BaseException("套餐名称、分类、价格、图片不能为空，价格不能小于0");
        }
        dto.setName(dto.getName().trim());
        if (dto.getStatus() != null) {
            validateStatus(dto.getStatus());
        }
        Setmeal sameName = setmealMapper.getByName(dto.getName());
        if (sameName != null && !sameName.getId().equals(currentId)) {
            throw new BaseException("套餐名称已存在");
        }
        Category category = categoryMapper.getById(dto.getCategoryId());
        if (category == null || !Integer.valueOf(2).equals(category.getType())) {
            throw new BaseException("请选择有效的套餐分类");
        }
        List<SetmealDish> relations = dto.getSetmealDishes();
        if (relations == null || relations.isEmpty()) {
            throw new BaseException("套餐必须包含菜品");
        }
        Set<Long> dishIds = new HashSet<>();
        for (SetmealDish relation : relations) {
            if (relation == null || relation.getDishId() == null
                    || relation.getCopies() == null || relation.getCopies() <= 0) {
                throw new BaseException("菜品id不能为空且份数必须大于0");
            }
            if (!dishIds.add(relation.getDishId())) {
                throw new BaseException("套餐不能包含重复菜品，请修改菜品份数");
            }
            Dish dish = dishMapper.getById(relation.getDishId());
            if (dish == null) {
                throw new BaseException("套餐包含不存在的菜品");
            }
            // 冗余字段取自菜品表，避免客户端提交过期或被篡改的价格、名称。
            relation.setName(dish.getName());
            relation.setPrice(dish.getPrice());
        }
    }

    private void checkDishesOnSale(List<SetmealDish> relations) {
        if (relations == null || relations.isEmpty()) {
            throw new SetmealEnableFailedException("套餐必须包含菜品，无法启售");
        }
        for (SetmealDish relation : relations) {
            Dish dish = dishMapper.getById(relation.getDishId());
            if (dish == null || !StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
    }

    private void saveRelations(Long id, List<SetmealDish> relations) {
        for (SetmealDish relation : relations) {
            relation.setId(null);
            relation.setSetmealId(id);
        }
        setmealDishMapper.insertBatch(relations);
    }
}
