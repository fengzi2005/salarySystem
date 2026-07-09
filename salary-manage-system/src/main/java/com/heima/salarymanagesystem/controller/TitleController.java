package com.heima.salarymanagesystem.controller;

import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.entity.Title;
import com.heima.salarymanagesystem.service.TitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职称（职级）管理控制器
 */
@RestController
@RequestMapping("/api/title")
@RequiredArgsConstructor
public class TitleController {

    private final TitleService titleService;

    /**
     * 查询所有职称
     * GET /api/title
     */
    @GetMapping
    public Result<List<Title>> listAll() {
        return Result.ok(titleService.list());
    }

    /**
     * 根据ID查询职称
     * GET /api/title/{id}
     */
    @GetMapping("/{id}")
    public Result<Title> getById(@PathVariable Long id) {
        return Result.ok(titleService.getById(id));
    }

    /**
     * 新增职称
     * POST /api/title
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody Title title) {
        return Result.ok(titleService.save(title));
    }

    /**
     * 更新职称
     * PUT /api/title
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Title title) {
        return Result.ok(titleService.updateById(title));
    }

    /**
     * 删除职称
     * DELETE /api/title/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(titleService.removeById(id));
    }
}
