package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.management.common.Result;
import com.teacher.management.dto.TeacherSubmitDTO;
import com.teacher.management.entity.CollectionTask;
import com.teacher.management.entity.Submission;
import com.teacher.management.entity.User;
import com.teacher.management.mapper.CollectionTaskMapper;
import com.teacher.management.mapper.SubmissionMapper;
import com.teacher.management.mapper.UserMapper;
import com.teacher.management.service.IMessageService;
import com.teacher.management.service.TeacherService;
import com.teacher.management.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private static final Logger log = LoggerFactory.getLogger(TeacherController.class);

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private CollectionTaskMapper collectionTaskMapper;

    // Load the current user only when entity fields are needed by the endpoint.
    private User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }

    // Always use the latest submission for a task and warn if historical duplicates exist.
    private Submission getLatestSubmissionForTask(Long userId, Long taskId) {
        if (userId == null || taskId == null) {
            return null;
        }

        QueryWrapper<Submission> latestQuery = new QueryWrapper<>();
        latestQuery.eq("user_id", userId)
                .eq("task_id", taskId)
                .orderByDesc("create_time")
                .orderByDesc("id")
                .last("LIMIT 1");
        Submission latest = submissionMapper.selectOne(latestQuery);

        if (latest != null) {
            QueryWrapper<Submission> countQuery = new QueryWrapper<>();
            countQuery.eq("user_id", userId).eq("task_id", taskId);
            Long dupCount = submissionMapper.selectCount(countQuery);
            if (dupCount > 1) {
                log.warn("Detected duplicate submissions: userId={}, taskId={}, count={}", userId, taskId, dupCount);
            }
        }

        return latest;
    }
    private boolean isRejectedStatus(Integer status) {
        return Objects.equals(status, Submission.STATUS_REJECTED)
                || Objects.equals(status, Submission.STATUS_FINAL_REJECTED);
    }

    private boolean isWithinTaskWindow(CollectionTask task, LocalDateTime now) {
        return task != null
                && (task.getStartTime() == null || !now.isBefore(task.getStartTime()))
                && (task.getEndTime() == null || !now.isAfter(task.getEndTime()));
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody TeacherSubmitDTO dto) {
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("\u7528\u6237\u4e0d\u5b58\u5728");
        }

        try {
            Long submissionId = teacherService.submitInfo(user.getId(), dto);

            String teacherName = user.getRealName() != null ? user.getRealName() : user.getUsername();
            messageService.sendToAllAdmins(
                    1,
                    user.getId(),
                    submissionId,
                    "\u65b0\u7684\u6559\u5b66\u79d1\u7814\u4fe1\u606f\u63d0\u4ea4",
                    "\u6559\u5e08 " + teacherName + " \u63d0\u4ea4\u4e86\u6559\u5b66\u79d1\u7814\u4fe1\u606f\uff0c\u8bf7\u5ba1\u6838\u3002");

            Map<String, Object> data = new HashMap<>();
            data.put("submissionId", submissionId);
            data.put("status", "\u5ba1\u6838\u4e2d");

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("\u63d0\u4ea4\u5931\u8d25\uff1a" + e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<?> history(
            @RequestParam(required = false) String year,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户不存在");
        }

        QueryWrapper<Submission> sq = new QueryWrapper<>();
        sq.eq("user_id", userId);
        if (year != null && !year.isEmpty()) {
            sq.apply("YEAR(create_time) = {0}", year);
        }
        sq.orderByDesc("create_time");

        Page<Submission> pageObj = new Page<>(page, size);
        Page<Submission> result = submissionMapper.selectPage(pageObj, sq);

        Set<Long> taskIds = result.getRecords().stream()
                .map(Submission::getTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, CollectionTask> taskMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<CollectionTask> tasks = collectionTaskMapper.selectBatchIds(taskIds);
            for (CollectionTask task : tasks) {
                taskMap.put(task.getId(), task);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> records = new ArrayList<>();
        for (Submission sub : result.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", sub.getId());
            item.put("userId", sub.getUserId());
            item.put("taskId", sub.getTaskId());
            item.put("submitMonth", sub.getSubmitMonth());
            item.put("status", sub.getStatus());
            item.put("auditRemark", sub.getAuditRemark());
            item.put("createTime", sub.getCreateTime());

            CollectionTask task = sub.getTaskId() != null ? taskMap.get(sub.getTaskId()) : null;
            if (task != null) {
                item.put("taskName", task.getTaskName());
                item.put("taskEndTime", task.getEndTime());
            } else if (sub.getTaskId() != null) {
                item.put("taskName", "未知任务");
            } else {
                item.put("taskName", "历史提交");
            }

            boolean canResubmit = isRejectedStatus(sub.getStatus()) && isWithinTaskWindow(task, now);
            boolean taskClosed = isRejectedStatus(sub.getStatus())
                    && task != null
                    && task.getEndTime() != null
                    && now.isAfter(task.getEndTime());
            item.put("canResubmit", canResubmit);
            item.put("taskClosed", taskClosed);
            records.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());

        return Result.success(data);
    }
    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("\u7528\u6237\u4e0d\u5b58\u5728");
        }

        com.teacher.management.vo.SubmissionDetailVO detail = teacherService.getSubmissionDetail(id, userId);
        if (detail == null) {
            return Result.error("\u672a\u627e\u5230\u8be5\u63d0\u4ea4\u8bb0\u5f55");
        }
        return Result.success(detail);
    }

    @GetMapping("/task/current")
    public Result<?> currentTask(@RequestParam(required = false) Long taskId) {
        LocalDateTime now = LocalDateTime.now();

        CollectionTask task;
        if (taskId != null) {
            task = collectionTaskMapper.selectById(taskId);
        } else {
            QueryWrapper<CollectionTask> tq = new QueryWrapper<>();
            tq.eq("status", 1)
                    .le("start_time", now)
                    .ge("end_time", now);
            task = collectionTaskMapper.selectOne(tq);
        }

        if (task == null) {
            return Result.success(null);
        }

        Long userId = SecurityUtils.getCurrentUserId();

        boolean submissionExists = false;
        Integer submissionStatus = null;
        boolean canResubmit = false;
        Long resubmitSubmissionId = null;

        if (userId != null) {
            Submission latest = getLatestSubmissionForTask(userId, task.getId());
            if (latest != null) {
                submissionExists = true;
                submissionStatus = latest.getStatus();
                canResubmit = isRejectedStatus(submissionStatus) && isWithinTaskWindow(task, now);
                if (canResubmit) {
                    resubmitSubmissionId = latest.getId();
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("task", task);
        data.put("submissionExists", submissionExists);
        data.put("submissionStatus", submissionStatus);
        data.put("canResubmit", canResubmit);
        data.put("resubmitSubmissionId", resubmitSubmissionId);
        data.put("hasSubmitted", submissionExists);
        return Result.success(data);
    }

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("\u7528\u6237\u4e0d\u5b58\u5728");
        }

        QueryWrapper<CollectionTask> tq = new QueryWrapper<>();
        tq.eq("status", 1)
                .le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now());
        CollectionTask activeTask = collectionTaskMapper.selectOne(tq);

        boolean isSubmitted = false;
        LocalDateTime submitTime = null;
        if (activeTask != null) {
            Submission currentSub = getLatestSubmissionForTask(userId, activeTask.getId());
            isSubmitted = currentSub != null;
            submitTime = currentSub != null ? currentSub.getCreateTime() : null;
        }

        String currentYear = String.valueOf(java.time.LocalDate.now().getYear());
        QueryWrapper<Submission> yearQuery = new QueryWrapper<>();
        yearQuery.eq("user_id", userId).apply("YEAR(create_time) = {0}", currentYear);
        Long yearlyCount = submissionMapper.selectCount(yearQuery);

        int totalAchievements = teacherService.countAchievements(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("isSubmitted", isSubmitted);
        data.put("submitTime", submitTime);
        data.put("yearlyCount", yearlyCount);
        data.put("totalAchievements", totalAchievements);

        return Result.success(data);
    }

    @GetMapping("/achievements")
    public Result<?> achievements() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("\u7528\u6237\u4e0d\u5b58\u5728");
        }

        try {
            com.teacher.management.vo.AchievementGroupVO vo = teacherService.getAchievements(userId);
            return Result.success(vo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("\u83b7\u53d6\u6210\u679c\u6570\u636e\u5931\u8d25\uff1a" + e.getMessage());
        }
    }

    @GetMapping("/excel/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            teacherService.downloadTemplate(response);
        } catch (Exception e) {
            throw new RuntimeException("\u6a21\u677f\u4e0b\u8f7d\u5931\u8d25\uff1a" + e.getMessage());
        }
    }

    @GetMapping("/export/{id}")
    public void exportSubmission(@PathVariable Long id, HttpServletResponse response) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("\u7528\u6237\u4e0d\u5b58\u5728");
        }

        Submission sub = submissionMapper.selectById(id);
        if (sub == null || !sub.getUserId().equals(userId)) {
            throw new RuntimeException("\u65e0\u6743\u8bbf\u95ee\u8be5\u63d0\u4ea4\u8bb0\u5f55");
        }

        try {
            teacherService.exportSingleSubmission(id, response);
        } catch (Exception e) {
            throw new RuntimeException("\u5bfc\u51fa\u5931\u8d25\uff1a" + e.getMessage());
        }
    }

    @GetMapping("/export/achievements")
    public void exportAllAchievements(HttpServletResponse response) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("\u7528\u6237\u4e0d\u5b58\u5728");
        }

        try {
            teacherService.exportAllAchievements(userId, response);
        } catch (Exception e) {
            throw new RuntimeException("\u5bfc\u51fa\u5931\u8d25\uff1a" + e.getMessage());
        }
    }

    @PostMapping("/excel/import")
    public Result<?> importExcel(@RequestParam("file") MultipartFile file,
            @RequestParam("taskId") Long taskId) {
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("\u7528\u6237\u4e0d\u5b58\u5728");
        }

        try {
            Long submissionId = teacherService.importExcel(user.getId(), taskId, file);

            String teacherName = user.getRealName() != null ? user.getRealName() : user.getUsername();
            messageService.sendToAllAdmins(
                    1,
                    user.getId(),
                    submissionId,
                    "Excel \u6279\u91cf\u6570\u636e\u5bfc\u5165",
                    "\u6559\u5e08 " + teacherName + " \u901a\u8fc7 Excel \u5bfc\u5165\u4e86\u6559\u5b66\u79d1\u7814\u4fe1\u606f\uff0c\u8bf7\u5ba1\u6838\u3002");

            Map<String, Object> data = new HashMap<>();
            data.put("submissionId", submissionId);
            data.put("status", "\u5ba1\u6838\u4e2d");
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("\u5bfc\u5165\u5931\u8d25\uff1a" + e.getMessage());
        }
    }

    @GetMapping("/basic-info")
    public Result<?> getBasicInfo() {
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("\u7528\u6237\u4e0d\u5b58\u5728");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("education", user.getEducation());
        data.put("major", user.getMajor());
        data.put("school", user.getSchool());
        data.put("degree", user.getDegree());
        data.put("degreeDate", user.getDegreeDate());
        data.put("title", user.getTitle());
        data.put("isDualTeacher", user.getIsDualTeacher());
        data.put("skillCert", user.getSkillCert());
        return Result.success(data);
    }

    @PutMapping("/basic-info")
    public Result<?> updateBasicInfo(@RequestBody Map<String, Object> body) {
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("\u7528\u6237\u4e0d\u5b58\u5728");
        }

        user.setEducation((String) body.get("education"));
        user.setMajor((String) body.get("major"));
        user.setSchool((String) body.get("school"));
        user.setDegree((String) body.get("degree"));
        user.setDegreeDate((String) body.get("degreeDate"));
        user.setTitle((String) body.get("title"));
        Object isDual = body.get("isDualTeacher");
        user.setIsDualTeacher(isDual != null ? ((Number) isDual).intValue() : 0);
        user.setSkillCert((String) body.get("skillCert"));

        userMapper.updateById(user);
        return Result.success("\u5b66\u5386/\u804c\u79f0\u4fe1\u606f\u66f4\u65b0\u6210\u529f");
    }
}
