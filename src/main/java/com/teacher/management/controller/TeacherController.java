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
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

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

    /**
     * 提交当月教学科研信息
     */
    @PostMapping("/submit")
    public Result<?> submit(@RequestBody TeacherSubmitDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", username);
        User user = userMapper.selectOne(query);

        if (user == null) {
            return Result.error("用户不存在");
        }

        try {
            Long submissionId = teacherService.submitInfo(user.getId(), dto);

            // 发送通知给所有管理员
            String teacherName = user.getRealName() != null ? user.getRealName() : username;
            messageService.sendToAllAdmins(
                    1, // type=1: 提交通知
                    user.getId(),
                    submissionId,
                    "新的教学科研信息提交",
                    "教师 " + teacherName + " 提交了教学科研信息，请审核。");

            Map<String, Object> data = new HashMap<>();
            data.put("submissionId", submissionId);
            data.put("status", "审核中");

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 查询提交历史记录
     * 
     * @param year 可选年份筛选，如 "2026"
     * @param page 页码，默认 1
     * @param size 每页条数，默认 10
     */
    @GetMapping("/history")
    public Result<?> history(
            @RequestParam(required = false) String year,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> uq = new QueryWrapper<>();
        uq.eq("username", username);
        User user = userMapper.selectOne(uq);
        if (user == null) {
            return Result.error("用户不存在");
        }

        QueryWrapper<Submission> sq = new QueryWrapper<>();
        sq.eq("user_id", user.getId());
        if (year != null && !year.isEmpty()) {
            sq.apply("YEAR(create_time) = {0}", year);
        }
        sq.orderByDesc("create_time");

        // new Page object
        Page<Submission> pageObj = new Page<>(page, size);
        Page<Submission> result = submissionMapper.selectPage(pageObj, sq);

        // 批量查询任务名称
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

        // 组装返回数据
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

            if (sub.getTaskId() != null) {
                item.put("taskName", taskMap.getOrDefault(sub.getTaskId(), "未知任务"));
            } else {
                item.put("taskName", "历史提交");
            }
            records.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());

        return Result.success(data);
    }

    /**
     * 查看某次提交的详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> uq = new QueryWrapper<>();
        uq.eq("username", username);
        User user = userMapper.selectOne(uq);
        if (user == null) {
            return Result.error("用户不存在");
        }

        com.teacher.management.vo.SubmissionDetailVO detail = teacherService.getSubmissionDetail(id, user.getId());
        if (detail == null) {
            return Result.error("未找到该提交记录");
        }
        return Result.success(detail);
    }

    /**
     * 获取当前进行中的采集任务
     */
    @GetMapping("/task/current")
    public Result<?> currentTask() {
        QueryWrapper<CollectionTask> tq = new QueryWrapper<>();
        tq.eq("status", 1)
                .le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now());
        CollectionTask task = collectionTaskMapper.selectOne(tq);

        if (task == null) {
            return Result.success(null); // 无活动任务
        }

        // 检查当前用户是否已对此任务提交
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        QueryWrapper<User> uq = new QueryWrapper<>();
        uq.eq("username", username);
        User user = userMapper.selectOne(uq);

        boolean hasSubmitted = false;
        if (user != null) {
            QueryWrapper<Submission> sq = new QueryWrapper<>();
            sq.eq("user_id", user.getId()).eq("task_id", task.getId());
            hasSubmitted = submissionMapper.selectCount(sq) > 0;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("task", task);
        data.put("hasSubmitted", hasSubmitted);
        return Result.success(data);
    }

    /**
     * Dashboard 统计数据
     */
    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> uq = new QueryWrapper<>();
        uq.eq("username", username);
        User user = userMapper.selectOne(uq);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Long userId = user.getId();

        // 1. 查询当前活动任务，判断是否已提交
        QueryWrapper<CollectionTask> tq = new QueryWrapper<>();
        tq.eq("status", 1)
                .le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now());
        CollectionTask activeTask = collectionTaskMapper.selectOne(tq);

        boolean isSubmitted = false;
        LocalDateTime submitTime = null;
        if (activeTask != null) {
            QueryWrapper<Submission> sq = new QueryWrapper<>();
            sq.eq("user_id", userId).eq("task_id", activeTask.getId());
            Submission currentSub = submissionMapper.selectOne(sq);
            isSubmitted = currentSub != null;
            submitTime = currentSub != null ? currentSub.getCreateTime() : null;
        }

        // 2. 本年度累计填报次数
        String currentYear = String.valueOf(java.time.LocalDate.now().getYear());
        QueryWrapper<Submission> yearQuery = new QueryWrapper<>();
        yearQuery.eq("user_id", userId).apply("YEAR(create_time) = {0}", currentYear);
        Long yearlyCount = submissionMapper.selectCount(yearQuery);

        // 3. 累计成果数
        int totalAchievements = teacherService.countAchievements(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("isSubmitted", isSubmitted);
        data.put("submitTime", submitTime);
        data.put("yearlyCount", yearlyCount);
        data.put("totalAchievements", totalAchievements);

        return Result.success(data);
    }

    /**
     * 获取当前教师的全部成果聚合数据
     */
    @GetMapping("/achievements")
    public Result<?> achievements() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> uq = new QueryWrapper<>();
        uq.eq("username", username);
        User user = userMapper.selectOne(uq);
        if (user == null) {
            return Result.error("用户不存在");
        }

        try {
            com.teacher.management.vo.AchievementGroupVO vo = teacherService.getAchievements(user.getId());
            return Result.success(vo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取成果数据失败：" + e.getMessage());
        }
    }

    /**
     * 下载 Excel 标准模板
     */
    @GetMapping("/excel/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            teacherService.downloadTemplate(response);
        } catch (Exception e) {
            throw new RuntimeException("模板下载失败：" + e.getMessage());
        }
    }

    /**
     * 导出个人某次提交的详情为 Excel
     */
    @GetMapping("/export/{id}")
    public void exportSubmission(@PathVariable Long id, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> uq = new QueryWrapper<>();
        uq.eq("username", username);
        User user = userMapper.selectOne(uq);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 校验提交归属
        Submission sub = submissionMapper.selectById(id);
        if (sub == null || !sub.getUserId().equals(user.getId())) {
            throw new RuntimeException("无权访问该提交记录");
        }

        try {
            teacherService.exportSingleSubmission(id, response);
        } catch (Exception e) {
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出教师全部已通过的成果数据为 Excel
     */
    @GetMapping("/export/achievements")
    public void exportAllAchievements(HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> uq = new QueryWrapper<>();
        uq.eq("username", username);
        User user = userMapper.selectOne(uq);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        try {
            teacherService.exportAllAchievements(user.getId(), response);
        } catch (Exception e) {
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 上传 Excel 导入数据
     */
    @PostMapping("/excel/import")
    public Result<?> importExcel(@RequestParam("file") MultipartFile file,
            @RequestParam("taskId") Long taskId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", username);
        User user = userMapper.selectOne(query);

        if (user == null) {
            return Result.error("用户不存在");
        }

        try {
            Long submissionId = teacherService.importExcel(user.getId(), taskId, file);

            // 发送通知给所有管理员
            String teacherName = user.getRealName() != null ? user.getRealName() : username;
            messageService.sendToAllAdmins(
                    1,
                    user.getId(),
                    submissionId,
                    "Excel 批量数据导入",
                    "教师 " + teacherName + " 通过 Excel 导入了教学科研信息，请审核。");

            Map<String, Object> data = new HashMap<>();
            data.put("submissionId", submissionId);
            data.put("status", "审核中");
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户的学历/职称信息
     */
    @GetMapping("/basic-info")
    public Result<?> getBasicInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> uq = new QueryWrapper<>();
        uq.eq("username", username);
        User user = userMapper.selectOne(uq);
        if (user == null) {
            return Result.error("用户不存在");
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

    /**
     * 更新当前用户的学历/职称信息
     */
    @PutMapping("/basic-info")
    public Result<?> updateBasicInfo(@RequestBody Map<String, Object> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        QueryWrapper<User> uq = new QueryWrapper<>();
        uq.eq("username", username);
        User user = userMapper.selectOne(uq);
        if (user == null) {
            return Result.error("用户不存在");
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
        return Result.success("学历/职称信息更新成功");
    }
}
