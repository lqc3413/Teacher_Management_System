package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.management.common.Result;
import com.teacher.management.entity.Department;
import com.teacher.management.entity.Submission;
import com.teacher.management.entity.User;
import com.teacher.management.entity.CollectionTask;
import com.teacher.management.mapper.CollectionTaskMapper;
import com.teacher.management.mapper.SubmissionMapper;
import com.teacher.management.mapper.UserMapper;
import com.teacher.management.service.IDepartmentService;
import com.teacher.management.service.IMessageService;
import com.teacher.management.service.TeacherService;
import com.teacher.management.vo.SubmissionDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员专用接口
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private CollectionTaskMapper collectionTaskMapper;

    /**
     * 分页查询所有教师提交记录（带教师姓名、工号、学院名称）
     */
    @GetMapping("/submissions")
    public Result<?> listSubmissions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String submitMonth) {

        // 1. 分页查询提交记录
        Page<Submission> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Submission> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (submitMonth != null && !submitMonth.isEmpty()) {
            wrapper.eq("submit_month", submitMonth);
        }
        wrapper.orderByDesc("create_time");
        Page<Submission> result = submissionMapper.selectPage(page, wrapper);

        // 2. 收集所有 userId，批量查询教师信息
        List<Long> userIds = result.getRecords().stream()
                .map(Submission::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        Map<Long, String> deptMap = new HashMap<>();

        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userMap.put(u.getId(), u);
            }

            // 收集所有 deptId，批量查询学院名称
            Set<Long> deptIds = users.stream()
                    .map(User::getDeptId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!deptIds.isEmpty()) {
                List<Department> depts = departmentService.listByIds(deptIds);
                for (Department d : depts) {
                    deptMap.put(d.getId(), d.getName());
                }
            }
        }

        // 2.1 收集所有 taskId，批量查询任务名称
        Set<Long> taskIds = result.getRecords().stream()
                .map(Submission::getTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> taskMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<CollectionTask> tasks = collectionTaskMapper.selectBatchIds(taskIds);
            for (CollectionTask t : tasks) {
                taskMap.put(t.getId(), t.getTaskName());
            }
        }

        // 3. 组装返回数据
        List<Map<String, Object>> records = new ArrayList<>();
        for (Submission sub : result.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", sub.getId());
            item.put("userId", sub.getUserId());
            item.put("submitMonth", sub.getSubmitMonth());
            item.put("status", sub.getStatus());
            item.put("auditRemark", sub.getAuditRemark());
            item.put("createTime", sub.getCreateTime());
            
            // Add taskName
            if (sub.getTaskId() != null) {
                item.put("taskName", taskMap.getOrDefault(sub.getTaskId(), "未知任务"));
            } else {
                item.put("taskName", "历史提交");
            }

            User user = userMap.get(sub.getUserId());
            if (user != null) {
                item.put("teacherName", user.getRealName());
                item.put("employeeNo", user.getEmployeeNo());
                String deptName = deptMap.get(user.getDeptId());
                item.put("deptName", deptName != null ? deptName : "");
            } else {
                item.put("teacherName", "未知");
                item.put("employeeNo", "");
                item.put("deptName", "");
            }
            records.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    /**
     * 查看某次提交的完整详情（管理员视角，不限用户）
     */
    @GetMapping("/submissions/detail/{id}")
    public Result<?> submissionDetail(@PathVariable Long id) {
        SubmissionDetailVO detail = teacherService.getSubmissionDetailForAdmin(id);
        if (detail == null) {
            return Result.error("未找到该提交记录");
        }
        return Result.success(detail);
    }

    /**
     * 审核提交记录
     * @param id          提交记录ID
     * @param status      审核结果: 1=已归档(通过), 2=被退回(驳回)
     * @param auditRemark 审核意见
     */
    @PutMapping("/submissions/audit/{id}")
    public Result<?> auditSubmission(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String auditRemark) {

        Submission sub = submissionMapper.selectById(id);
        if (sub == null) {
            return Result.error("提交记录不存在");
        }
        if (sub.getStatus() != 0) {
            return Result.error("该记录已审核，不能重复操作");
        }

        sub.setStatus(status);
        sub.setAuditRemark(auditRemark);
        submissionMapper.updateById(sub);

        // 发送通知给对应教师
        try {
            // 获取当前管理员的 userId
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminUsername = auth.getName();
            QueryWrapper<User> adminQuery = new QueryWrapper<>();
            adminQuery.eq("username", adminUsername);
            User adminUser = userMapper.selectOne(adminQuery);
            Long adminId = adminUser != null ? adminUser.getId() : null;

            if (status == 1) {
                // 审核通过
                messageService.sendMessage(
                    2, adminId, sub.getUserId(), sub.getId(),
                    "申报审核通过",
                    "您 " + sub.getSubmitMonth() + " 的教学科研信息申报已通过审核。"
                );
            } else if (status == 2) {
                // 审核驳回
                String reason = (auditRemark != null && !auditRemark.isEmpty()) ? auditRemark : "未填写";
                messageService.sendMessage(
                    3, adminId, sub.getUserId(), sub.getId(),
                    "申报审核被退回",
                    "您 " + sub.getSubmitMonth() + " 的教学科研信息申报被退回，原因：" + reason
                );
            }
        } catch (Exception e) {
            // 通知发送失败不影响审核结果
        }

        return Result.success();
    }

    /**
     * 管理员仪表盘统计数据
     */
    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> data = new HashMap<>();

        // 1. 注册教师数量 (roleId = 2)
        long teacherCount = userMapper.selectCount(
                new QueryWrapper<User>().eq("role_id", 2));
        data.put("teacherCount", teacherCount);

        // 2. 待审核提交数量 (status = 0)
        long pendingCount = submissionMapper.selectCount(
                new QueryWrapper<Submission>().eq("status", 0));
        data.put("pendingCount", pendingCount);

        // 3. 累计成果数
        int totalAchievements = teacherService.countAllAchievements();
        data.put("totalAchievements", totalAchievements);

        // 4. 本月新增提交
        String currentMonth = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        long monthlyCount = submissionMapper.selectCount(
                new QueryWrapper<Submission>().eq("submit_month", currentMonth));
        data.put("monthlyCount", monthlyCount);

        return Result.success(data);
    }
}
