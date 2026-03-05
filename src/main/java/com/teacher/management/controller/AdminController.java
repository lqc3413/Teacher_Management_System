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
import jakarta.servlet.http.HttpServletResponse;
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
            @RequestParam(required = false) Long deptId) {

        // 1. 分页查询提交记录
        Page<Submission> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Submission> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        // 按部门筛选：先查出该部门的所有用户ID
        if (deptId != null) {
            QueryWrapper<User> deptUserQuery = new QueryWrapper<>();
            deptUserQuery.eq("dept_id", deptId);
            List<User> deptUsers = userMapper.selectList(deptUserQuery);
            List<Long> deptUserIds = deptUsers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            if (deptUserIds.isEmpty()) {
                // 该部门无用户，直接返回空
                Map<String, Object> emptyData = new HashMap<>();
                emptyData.put("records", new ArrayList<>());
                emptyData.put("total", 0);
                return Result.success(emptyData);
            }
            wrapper.in("user_id", deptUserIds);
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
     * 审核提交记录（终审）
     * 
     * @param id          提交记录ID
     * @param status      审核结果: 1=已归档(通过), 2=被退回(驳回), 4=退回部门主任
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

        // 管理员终审：可审核 status=0（无部门主任的直接提交）或 status=3（部门初审通过）
        if (sub.getStatus() != Submission.STATUS_PENDING
                && sub.getStatus() != Submission.STATUS_DEPT_APPROVED) {
            return Result.error("该记录当前状态不允许终审操作");
        }

        // 校验 status 参数有效性
        if (status != Submission.STATUS_APPROVED
                && status != Submission.STATUS_REJECTED
                && status != Submission.STATUS_FINAL_REJECTED) {
            return Result.error("无效的审核状态，可选值：1(通过), 2(驳回), 4(退回部门主任)");
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

            if (status == Submission.STATUS_APPROVED) {
                // 终审通过
                messageService.sendMessage(
                        2, adminId, sub.getUserId(), sub.getId(),
                        "申报终审通过",
                        "您的教学科研信息申报已通过终审，已归档。");
            } else if (status == Submission.STATUS_REJECTED) {
                // 终审驳回
                String reason = (auditRemark != null && !auditRemark.isEmpty()) ? auditRemark : "未填写";
                messageService.sendMessage(
                        3, adminId, sub.getUserId(), sub.getId(),
                        "申报终审被驳回",
                        "您的教学科研信息申报被终审驳回，原因：" + reason);
            } else if (status == Submission.STATUS_FINAL_REJECTED) {
                // 退回给部门主任重新审核
                String reason = (auditRemark != null && !auditRemark.isEmpty()) ? auditRemark : "未填写";
                // 通知教师
                messageService.sendMessage(
                        3, adminId, sub.getUserId(), sub.getId(),
                        "申报被退回至部门主任",
                        "您的教学科研信息申报被退回给部门主任重新审核，原因：" + reason);
            }
        } catch (Exception e) {
            // 通知发送失败不影响审核结果
        }

        return Result.success();
    }

    /**
     * 批量导出提交数据（支持筛选）
     */
    @GetMapping("/export")
    public void exportSubmissions(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        teacherService.exportSubmissions(status, deptId, response);
    }

    /**
     * 导出单条提交详情
     */
    @GetMapping("/export/{id}")
    public void exportSingle(@PathVariable Long id,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        teacherService.exportSingleSubmission(id, response);
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

        // 4. 本月新增提交（按创建时间）
        java.time.LocalDateTime monthStart = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long monthlyCount = submissionMapper.selectCount(
                new QueryWrapper<Submission>().ge("create_time", monthStart));
        data.put("monthlyCount", monthlyCount);

        return Result.success(data);
    }
}
