package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.management.common.Result;
import com.teacher.management.entity.Notice;
import com.teacher.management.mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知公告控制器
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    @Autowired
    private NoticeMapper noticeMapper;

    /**
     * 分页查询通知列表（管理员用）
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String title,
                          @RequestParam(required = false) Integer status) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(title != null && !title.isEmpty(), Notice::getTitle, title);
        wrapper.eq(status != null, Notice::getStatus, status);
        wrapper.orderByDesc(Notice::getCreatedAt);
        Page<Notice> result = noticeMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    /**
     * 查询已发布的通知（教师 Dashboard 用）
     */
    @GetMapping("/published")
    public Result<?> published() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1);
        wrapper.orderByDesc(Notice::getPublishTime);
        wrapper.last("LIMIT 10");
        List<Notice> list = noticeMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 新增通知
     */
    @PostMapping
    public Result<?> save(@RequestBody Notice notice) {
        notice.setCreatedAt(LocalDateTime.now());
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            notice.setPublishTime(LocalDateTime.now());
        }
        return noticeMapper.insert(notice) > 0 ? Result.success() : Result.error("新增失败");
    }

    /**
     * 编辑通知
     */
    @PutMapping
    public Result<?> update(@RequestBody Notice notice) {
        // 如果状态从草稿改为发布，自动设置发布时间
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            Notice old = noticeMapper.selectById(notice.getId());
            if (old != null && old.getStatus() == 0) {
                notice.setPublishTime(LocalDateTime.now());
            }
        }
        return noticeMapper.updateById(notice) > 0 ? Result.success() : Result.error("修改失败");
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return noticeMapper.deleteById(id) > 0 ? Result.success() : Result.error("删除失败");
    }
}
