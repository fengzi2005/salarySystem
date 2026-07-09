package com.heima.salarymanagesystem.controller;

import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.entity.Position;
import com.heima.salarymanagesystem.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位管理控制器
 */
@RestController
@RequestMapping("/api/position")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /**
     * 查询所有岗位
     * GET /api/position
     */
    @GetMapping
    public Result<List<Position>> listAll() {
        return Result.ok(positionService.list());
    }

    /**
     * 根据ID查询岗位
     * GET /api/position/{id}
     */
    @GetMapping("/{id}")
    public Result<Position> getById(@PathVariable Long id) {
        return Result.ok(positionService.getById(id));
    }

    /**
     * 新增岗位
     * POST /api/position
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody Position position) {
        return Result.ok(positionService.save(position));
    }

    /**
     * 更新岗位
     * PUT /api/position
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Position position) {
        return Result.ok(positionService.updateById(position));
    }

    /**
     * 删除岗位
     * DELETE /api/position/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(positionService.removeById(id));
    }
}
