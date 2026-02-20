package com.teacher.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("sys_report_record")
public class ReportRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String name;
    private String level;
    private String adoptDate;
    @TableField("`rank`")
    private Integer rank;
    private String others;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getAdoptDate() { return adoptDate; }
    public void setAdoptDate(String adoptDate) { this.adoptDate = adoptDate; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getOthers() { return others; }
    public void setOthers(String others) { this.others = others; }
}
