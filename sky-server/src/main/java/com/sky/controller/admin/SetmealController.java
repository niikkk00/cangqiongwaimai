package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result<Void> save(@RequestBody SetmealDTO dto) {
        setmealService.saveWithDish(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO query) {
        return Result.success(setmealService.pageQuery(query));
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result<Void> delete(@RequestParam List<Long> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        return Result.success(setmealService.getByIdWithDish(id));
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result<Void> update(@RequestBody SetmealDTO dto) {
        setmealService.update(dto);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售、停售")
    public Result<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}
