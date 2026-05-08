package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.management.common.Result;
import com.teacher.management.dto.TaskCreateDTO;
import com.teacher.management.entity.CollectionTask;
import com.teacher.management.mapper.CollectionTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/task")
public class TaskController {

    @Autowired
    private CollectionTaskMapper taskMapper;

    /**
     * 创建采集任务（初始状态为未发布）
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody TaskCreateDTO dto) {
        if (dto.getTaskName() == null || dto.getTaskName().trim().isEmpty()) {
            return Result.error("任务名称不能为空");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            return Result.error("起止时间不能为空");
        }
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            return Result.error("截止时间不能早于开始时间");
        }

        CollectionTask task = new CollectionTask();
        task.setTaskName(dto.getTaskName().trim());
        task.setStartTime(dto.getStartTime());
        task.setEndTime(dto.getEndTime());
        task.setStatus(0); // 未发布
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);

        return Result.success(task);
    }

    /**
     * 分页查询任务列表
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<CollectionTask> pageObj = new Page<>(page, size);
        QueryWrapper<CollectionTask> qw = new QueryWrapper<>();
        qw.orderByDesc("create_time");

        Page<CollectionTask> result = taskMapper.selectPage(pageObj, qw);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    /**
     * 发布任务（先将其他进行中的任务设为已结束，保证同时只有一个活动任务）
     */
    @PutMapping("/publish/{id}")
    @Transactional
    public Result<?> publish(@PathVariable Long id) {
        CollectionTask task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (task.getStatus() == 2) {
            return Result.error("已结束的任务不能重新发布");
        }

        // 1. 将所有进行中的任务设为已结束
        UpdateWrapper<CollectionTask> uw = new UpdateWrapper<>();
        uw.eq("status", 1).set("status", 2);
        taskMapper.update(null, uw);

        // 2. 将当前任务设为进行中
        task.setStatus(1);
        taskMapper.updateById(task);

        return Result.success("任务已发布");
    }

    /**
     * 强制结束任务
     */
    @PutMapping("/end/{id}")
    public Result<?> end(@PathVariable Long id) {
        CollectionTask task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (task.getStatus() != 1) {
            return Result.error("只能结束进行中的任务");
        }

        task.setStatus(2);
        taskMapper.updateById(task);
        return Result.success("任务已结束");
    }

    /**
     * 删除任务（仅未发布的任务可删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        CollectionTask task = taskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        if (task.getStatus() != 0) {
            return Result.error("只能删除未发布的任务");
        }

        taskMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
