package com.teacher.management.dto;

import java.time.LocalDateTime;

/**
 * 创建/编辑采集任务 DTO
 */
public class TaskCreateDTO {

    private String taskName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
