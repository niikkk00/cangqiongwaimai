package com.sky.controller;

import com.sky.controller.admin.SetmealController;
import com.sky.controller.admin.DishController;
import com.sky.dto.SetmealDTO;
import com.sky.exception.BaseException;
import com.sky.handler.GlobalExceptionHandler;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.service.DishService;
import com.sky.vo.SetmealVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SetmealControllerTest {
    private MockMvc mvc;
    private SetmealService service;
    private DishService dishService;

    @BeforeEach
    void setup() {
        service = mock(SetmealService.class);
        dishService = mock(DishService.class);
        SetmealController controller = new SetmealController();
        DishController dishController = new DishController();
        ReflectionTestUtils.setField(controller, "setmealService", service);
        ReflectionTestUtils.setField(dishController, "dishService", dishService);
        mvc = MockMvcBuilders.standaloneSetup(controller, dishController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void bindsCreateAndUpdateJson() throws Exception {
        String json = "{\"categoryId\":2,\"name\":\"套餐\",\"price\":20,\"image\":\"a.png\","
                + "\"setmealDishes\":[{\"dishId\":10,\"copies\":2}]}";
        mvc.perform(post("/admin/setmeal").contentType("application/json").content(json))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        mvc.perform(put("/admin/setmeal").contentType("application/json").content(json))
                .andExpect(status().isOk());
        verify(service).saveWithDish(argThat(dto -> dto.getSetmealDishes().get(0).getCopies() == 2));
        verify(service).update(any(SetmealDTO.class));
    }

    @Test
    void bindsPageFiltersAndReturnsPageEnvelope() throws Exception {
        when(service.pageQuery(any())).thenReturn(new PageResult(0L, Collections.emptyList()));
        mvc.perform(get("/admin/setmeal/page").param("page", "1").param("pageSize", "10")
                .param("categoryId", "2").param("name", "套餐").param("status", "0"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isArray());
        verify(service).pageQuery(argThat(q -> q.getPageSize() == 10 && q.getStatus() == 0
                && q.getCategoryId() == 2 && "套餐".equals(q.getName())));
    }

    @Test
    void bindsCommaSeparatedIdsAndStatusQuery() throws Exception {
        mvc.perform(delete("/admin/setmeal").param("ids", "1,2"))
                .andExpect(status().isOk());
        verify(service).deleteBatch(Arrays.asList(1L, 2L));
        mvc.perform(post("/admin/setmeal/status/1").param("id", "2"))
                .andExpect(status().isOk());
        verify(service).startOrStop(1, 2L);
    }

    @Test
    void returnsDetailAndDishSelection() throws Exception {
        SetmealVO vo = new SetmealVO();
        vo.setId(1L);
        vo.setCategoryName("套餐分类");
        when(service.getByIdWithDish(1L)).thenReturn(vo);
        when(dishService.list(2L)).thenReturn(Collections.emptyList());
        mvc.perform(get("/admin/setmeal/1")).andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.categoryName").value("套餐分类"));
        mvc.perform(get("/admin/dish/list").param("categoryId", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
        verify(dishService).list(2L);
    }

    @Test
    void businessErrorUsesProjectEnvelope() throws Exception {
        doThrow(new BaseException("套餐不存在")).when(service).startOrStop(1, 2L);
        mvc.perform(post("/admin/setmeal/status/1").param("id", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("套餐不存在"));
    }
}
