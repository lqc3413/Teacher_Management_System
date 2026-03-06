package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.management.common.Result;
import com.teacher.management.dto.DeptDirectorAuditRequest;
import com.teacher.management.entity.CollectionTask;
import com.teacher.management.entity.Department;
import com.teacher.management.entity.Submission;
import com.teacher.management.entity.User;
import com.teacher.management.mapper.CollectionTaskMapper;
import com.teacher.management.mapper.DepartmentMapper;
import com.teacher.management.mapper.SubmissionMapper;
import com.teacher.management.mapper.UserMapper;
import com.teacher.management.service.IMessageService;
import com.teacher.management.service.TeacherService;
import com.teacher.management.vo.SubmissionDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门主任专用接口
 * 负责本部门教师申报的初审
 */
@RestController
@RequestMapping("/api/dept-director")
public class DeptDirectorController {

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private CollectionTaskMapper collectionTaskMapper;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private TeacherService teacherService;

    /**
     * 获取当前登录的部门主任用户信息（通过 JWT userId，主键查询）
     */
    private User getCurrentDirector() {
        Long userId = com.teacher.management.utils.SecurityUtils.getCurrentUserId();
        if (userId == null)
            return null;
        return userMapper.selectById(userId);
    }

    /**
     * 获取本部门待初审的提交列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   可选状态筛选 (0=待初审, 4=终审退回待重审)
     */
    @GetMapping("/pending")
    public Result<?> listPendingSubmissions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        User director = getCurrentDirector();
        if (director == null || director.getDeptId() == null) {
            return Result.error("无法获取部门信息");
        }

        Long deptId = director.getDeptId();

        // 1. 获取本部门所有教师的 userId 列表
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("dept_id", deptId);
        userQuery.ne("id", director.getId()); // 排除自己
        List<User> deptUsers = userMapper.selectList(userQuery);
        List<Long> deptUserIds = deptUsers.stream().map(User::getId).collect(Collectors.toList());

        if (deptUserIds.isEmpty()) {
            Page<Map<String, Object>> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setRecords(new ArrayList<>());
            emptyPage.setTotal(0);
            return Result.success(emptyPage);
        }

        // 2. 查询这些教师的提交记录（待初审 status=0 或 终审退回 status=4）
        Page<Submission> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Submission> subQuery = new QueryWrapper<>();
        subQuery.in("user_id", deptUserIds);
        if (status != null) {
            subQuery.eq("status", status);
        } else {
            subQuery.in("status", Arrays.asList(
                    Submission.STATUS_PENDING,
                    Submission.STATUS_FINAL_REJECTED));
        }
        subQuery.orderByDesc("create_time");

        Page<Submission> submissionPage = submissionMapper.selectPage(page, subQuery);

        // 3. 批量查询任务名称
        Set<Long> taskIds = submissionPage.getRecords().stream()
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

        // 4. 补充教师姓名、工号、部门名称、任务名称
        Map<Long, User> userMap = deptUsers.stream().collect(Collectors.toMap(User::getId, u -> u));
        Department dept = departmentMapper.selectById(deptId);
        String deptName = dept != null ? dept.getName() : "";

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Submission sub : submissionPage.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", sub.getId());
            item.put("userId", sub.getUserId());
            item.put("taskId", sub.getTaskId());
            item.put("submitMonth", sub.getSubmitMonth());
            if (sub.getTaskId() != null) {
                item.put("taskName", taskMap.getOrDefault(sub.getTaskId(), "未知任务"));
            } else {
                item.put("taskName", "历史提交");
            }
            item.put("status", sub.getStatus());
            item.put("auditRemark", sub.getAuditRemark());
            item.put("createTime", sub.getCreateTime());
            item.put("deptAuditStatus", sub.getDeptAuditStatus());
            item.put("deptAuditRemark", sub.getDeptAuditRemark());
            item.put("deptAuditTime", sub.getDeptAuditTime());

            User teacher = userMap.get(sub.getUserId());
            item.put("realName", teacher != null ? teacher.getRealName() : "");
            item.put("employeeNo", teacher != null ? teacher.getEmployeeNo() : "");
            item.put("deptName", deptName);

            resultList.add(item);
        }

        // 构造分页返回
        Map<String, Object> pageResult = new LinkedHashMap<>();
        pageResult.put("records", resultList);
        pageResult.put("total", submissionPage.getTotal());
        pageResult.put("current", submissionPage.getCurrent());
        pageResult.put("size", submissionPage.getSize());
        pageResult.put("pages", submissionPage.getPages());

        return Result.success(pageResult);
    }

    /**
     * 部门初审操作
     *
     * @param id      提交记录ID
     * @param request 包含 approved(boolean) 和 remark(String)
     */
    @PutMapping("/audit/{id}")
    public Result<?> auditSubmission(
            @PathVariable Long id,
            @RequestBody DeptDirectorAuditRequest request) {

        User director = getCurrentDirector();
        if (director == null) {
            return Result.error("无法获取当前用户信息");
        }

        Submission sub = submissionMapper.selectById(id);
        if (sub == null) {
            return Result.error("提交记录不存在");
        }

        // 校验：只能审核待初审(0)或终审退回(4)的记录
        if (sub.getStatus() != Submission.STATUS_PENDING
                && sub.getStatus() != Submission.STATUS_FINAL_REJECTED) {
            return Result.error("该记录当前状态不允许部门初审");
        }

        // 校验：不能审核自己的提交
        if (sub.getUserId().equals(director.getId())) {
            return Result.error("不能审核自己的提交记录");
        }

        // 校验：只能审核本部门教师的提交
        User teacher = userMapper.selectById(sub.getUserId());
        if (teacher == null || !director.getDeptId().equals(teacher.getDeptId())) {
            return Result.error("只能审核本部门教师的提交");
        }

        // 执行初审
        sub.setDeptAuditStatus(request.getApproved() ? 1 : 2);
        sub.setDeptAuditorId(director.getId());
        sub.setDeptAuditTime(LocalDateTime.now());
        sub.setDeptAuditRemark(request.getRemark());

        if (Boolean.TRUE.equals(request.getApproved())) {
            // 部门通过 → 状态变为"部门已通过，待终审"
            sub.setStatus(Submission.STATUS_DEPT_APPROVED);
        } else {
            // 部门驳回 → 状态变为"已驳回"
            sub.setStatus(Submission.STATUS_REJECTED);
            sub.setAuditRemark("部门初审驳回：" + (request.getRemark() != null ? request.getRemark() : ""));
        }

        submissionMapper.updateById(sub);

        // 发送通知
        try {
            if (Boolean.TRUE.equals(request.getApproved())) {
                messageService.sendMessage(
                        2, director.getId(), sub.getUserId(), sub.getId(),
                        "部门初审通过",
                        "您的教学科研信息申报已通过部门初审，等待管理员终审。");
            } else {
                String reason = (request.getRemark() != null && !request.getRemark().isEmpty())
                        ? request.getRemark()
                        : "未填写";
                messageService.sendMessage(
                        3, director.getId(), sub.getUserId(), sub.getId(),
                        "部门初审被退回",
                        "您的教学科研信息申报被部门退回，原因：" + reason);
            }
        } catch (Exception e) {
            // 通知失败不影响审核
        }

        return Result.success();
    }

    /**
     * 查看提交详情（仅限本部门，返回完整材料数据）
     */
    @GetMapping("/submission/{id}")
    public Result<?> getSubmissionDetail(@PathVariable Long id) {
        User director = getCurrentDirector();
        if (director == null) {
            return Result.error("无法获取当前用户信息");
        }

        Submission sub = submissionMapper.selectById(id);
        if (sub == null) {
            return Result.error("提交记录不存在");
        }

        // 校验是否本部门的提交
        User teacher = userMapper.selectById(sub.getUserId());
        if (teacher == null || !director.getDeptId().equals(teacher.getDeptId())) {
            return Result.error("无权查看其他部门的提交");
        }

        // 使用与管理员相同的详情查询，返回完整材料数据
        SubmissionDetailVO detail = teacherService.getSubmissionDetailForAdmin(id);
        if (detail == null) {
            return Result.error("未找到该提交记录");
        }
        return Result.success(detail);
    }

    /**
     * 部门主任仪表盘统计数据
     */
    @GetMapping("/stats")
    public Result<?> stats() {
        User director = getCurrentDirector();
        if (director == null || director.getDeptId() == null) {
            return Result.error("无法获取部门信息");
        }

        Long deptId = director.getDeptId();

        // 获取本部门教师ID列表（排除自己）
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("dept_id", deptId);
        userQuery.ne("id", director.getId());
        List<User> deptUsers = userMapper.selectList(userQuery);
        List<Long> deptUserIds = deptUsers.stream().map(User::getId).collect(Collectors.toList());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("deptTeacherCount", deptUsers.size());

        if (deptUserIds.isEmpty()) {
            stats.put("pendingCount", 0);
            stats.put("approvedCount", 0);
            stats.put("rejectedCount", 0);
            stats.put("finalRejectedCount", 0);
        } else {
            // 待初审数量 (status=0)
            QueryWrapper<Submission> pendingQuery = new QueryWrapper<>();
            pendingQuery.in("user_id", deptUserIds);
            pendingQuery.eq("status", Submission.STATUS_PENDING);
            stats.put("pendingCount", submissionMapper.selectCount(pendingQuery));

            // 部门已通过待终审 (status=3)
            QueryWrapper<Submission> approvedQuery = new QueryWrapper<>();
            approvedQuery.in("user_id", deptUserIds);
            approvedQuery.eq("status", Submission.STATUS_DEPT_APPROVED);
            stats.put("approvedCount", submissionMapper.selectCount(approvedQuery));

            // 已驳回 (status=2)
            QueryWrapper<Submission> rejectedQuery = new QueryWrapper<>();
            rejectedQuery.in("user_id", deptUserIds);
            rejectedQuery.eq("status", Submission.STATUS_REJECTED);
            stats.put("rejectedCount", submissionMapper.selectCount(rejectedQuery));

            // 终审退回 (status=4)
            QueryWrapper<Submission> finalRejQuery = new QueryWrapper<>();
            finalRejQuery.in("user_id", deptUserIds);
            finalRejQuery.eq("status", Submission.STATUS_FINAL_REJECTED);
            stats.put("finalRejectedCount", submissionMapper.selectCount(finalRejQuery));
        }

        // 部门名称
        Department dept = departmentMapper.selectById(deptId);
        stats.put("deptName", dept != null ? dept.getName() : "");

        return Result.success(stats);
    }
}
