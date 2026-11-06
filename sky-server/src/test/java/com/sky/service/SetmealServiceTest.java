package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.*;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.*;
import com.sky.service.impl.SetmealServiceImpl;
import com.sky.vo.SetmealVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetmealServiceTest {
    @Mock SetmealMapper setmealMapper;
    @Mock SetmealDishMapper relationMapper;
    @Mock DishMapper dishMapper;
    @Mock CategoryMapper categoryMapper;
    @InjectMocks SetmealServiceImpl service;
    private SetmealDTO dto;

    @BeforeEach
    void prepare() {
        dto = new SetmealDTO();
        dto.setCategoryId(2L);
        dto.setName("测试套餐");
        dto.setPrice(new BigDecimal("20.00"));
        dto.setImage("test.png");
        dto.setSetmealDishes(Collections.singletonList(SetmealDish.builder()
                .dishId(10L).copies(2).name("客户端名称").price(BigDecimal.ZERO).build()));
    }

    private void validReferences() {
        when(categoryMapper.getById(2L)).thenReturn(Category.builder().id(2L).type(2).build());
        when(dishMapper.getById(10L)).thenReturn(Dish.builder().id(10L).name("真实菜品")
                .price(BigDecimal.TEN).status(1).build());
    }

    @Test
    void createDefaultsToStoppedAndUsesGeneratedIdAndDatabaseDishFields() {
        validReferences();
        dto.setStatus(1);
        dto.setId(999L);
        doAnswer(invocation -> {
            Setmeal meal = invocation.getArgument(0);
            assertNull(meal.getId());
            meal.setId(5L);
            return null;
        }).when(setmealMapper).insert(any());
        service.saveWithDish(dto);
        ArgumentCaptor<Setmeal> meal = ArgumentCaptor.forClass(Setmeal.class);
        verify(setmealMapper).insert(meal.capture());
        assertEquals(0, meal.getValue().getStatus());
        SetmealDish relation = dto.getSetmealDishes().get(0);
        assertEquals(5L, relation.getSetmealId());
        assertNull(relation.getId());
        assertEquals("真实菜品", relation.getName());
        assertEquals(BigDecimal.TEN, relation.getPrice());
        verify(relationMapper).insertBatch(dto.getSetmealDishes());
    }

    @Test
    void rejectsDuplicateNameBeforeWriting() {
        when(setmealMapper.getByName(dto.getName())).thenReturn(Setmeal.builder().id(8L).build());
        assertThrows(BaseException.class, () -> service.saveWithDish(dto));
        verify(setmealMapper, never()).insert(any());
    }

    @Test
    void rejectsEmptyDishList() {
        when(categoryMapper.getById(2L)).thenReturn(Category.builder().type(2).build());
        dto.setSetmealDishes(Collections.emptyList());
        assertThrows(BaseException.class, () -> service.saveWithDish(dto));
        verifyNoInteractions(relationMapper);
    }

    @Test
    void rejectsInvalidPrice() {
        dto.setPrice(new BigDecimal("-1"));
        assertThrows(BaseException.class, () -> service.saveWithDish(dto));
        verifyNoInteractions(setmealMapper);
    }

    @Test
    void rejectsDishCategoryUsedAsSetmealCategory() {
        when(categoryMapper.getById(2L)).thenReturn(Category.builder().type(1).build());
        assertThrows(BaseException.class, () -> service.saveWithDish(dto));
        verify(setmealMapper, never()).insert(any());
    }

    @Test
    void batchDeleteChecksEveryStatusBeforeAnyDelete() {
        when(setmealMapper.getById(1L)).thenReturn(Setmeal.builder().id(1L).status(0).build());
        when(setmealMapper.getById(2L)).thenReturn(Setmeal.builder().id(2L).status(1).build());
        assertThrows(DeletionNotAllowedException.class, () -> service.deleteBatch(Arrays.asList(1L, 2L)));
        verify(setmealMapper, never()).deleteById(any());
        verifyNoInteractions(relationMapper);
    }

    @Test
    void stoppedSetmealDeletesRelationsAndSetmeal() {
        when(setmealMapper.getById(1L)).thenReturn(Setmeal.builder().status(0).build());
        service.deleteBatch(Collections.singletonList(1L));
        verify(relationMapper).deleteBySetmealId(1L);
        verify(setmealMapper).deleteById(1L);
    }

    @Test
    void cannotEnableWithStoppedDish() {
        when(setmealMapper.getById(1L)).thenReturn(Setmeal.builder().status(0).build());
        when(relationMapper.getBySetmealId(1L)).thenReturn(dto.getSetmealDishes());
        when(dishMapper.getById(10L)).thenReturn(Dish.builder().status(0).build());
        assertThrows(SetmealEnableFailedException.class, () -> service.startOrStop(1, 1L));
        verify(setmealMapper, never()).update(any());
    }

    @Test
    void cannotEnableWithMissingDish() {
        when(setmealMapper.getById(1L)).thenReturn(Setmeal.builder().status(0).build());
        when(relationMapper.getBySetmealId(1L)).thenReturn(dto.getSetmealDishes());
        assertThrows(SetmealEnableFailedException.class, () -> service.startOrStop(1, 1L));
    }

    @Test
    void enablesWithSellingDishes() {
        when(setmealMapper.getById(1L)).thenReturn(Setmeal.builder().status(0).build());
        when(relationMapper.getBySetmealId(1L)).thenReturn(dto.getSetmealDishes());
        when(dishMapper.getById(10L)).thenReturn(Dish.builder().status(1).build());
        service.startOrStop(1, 1L);
        verify(setmealMapper).update(argThat(meal -> meal.getId().equals(1L) && meal.getStatus() == 1));
    }

    @Test
    void stopDoesNotRequireSellingDishes() {
        when(setmealMapper.getById(1L)).thenReturn(Setmeal.builder().status(1).build());
        service.startOrStop(0, 1L);
        verifyNoInteractions(dishMapper, relationMapper);
        verify(setmealMapper).update(any());
    }

    @Test
    void rejectsUnknownStatusAndInvalidPagination() {
        assertThrows(BaseException.class, () -> service.startOrStop(2, 1L));
        assertThrows(BaseException.class, () -> service.pageQuery(new SetmealPageQueryDTO()));
        verifyNoInteractions(setmealMapper);
    }

    @Test
    void detailIncludesCategoryAndRelations() {
        SetmealVO vo = new SetmealVO();
        vo.setCategoryName("套餐分类");
        when(setmealMapper.getDetailById(1L)).thenReturn(vo);
        when(relationMapper.getBySetmealId(1L)).thenReturn(dto.getSetmealDishes());
        assertEquals(dto.getSetmealDishes(), service.getByIdWithDish(1L).getSetmealDishes());
        assertEquals("套餐分类", vo.getCategoryName());
    }

    @Test
    void updateReplacesRelationsAndPreservesStatusWhenOmitted() {
        validReferences();
        dto.setId(1L);
        when(setmealMapper.getById(1L)).thenReturn(Setmeal.builder().id(1L).status(0).build());
        service.update(dto);
        verify(setmealMapper).update(argThat(meal -> meal.getId().equals(1L) && meal.getStatus() == 0));
        verify(relationMapper).deleteBySetmealId(1L);
        verify(relationMapper).insertBatch(dto.getSetmealDishes());
        assertEquals(1L, dto.getSetmealDishes().get(0).getSetmealId());
    }
}
