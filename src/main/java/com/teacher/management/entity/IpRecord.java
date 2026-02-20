package com.teacher.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("sys_ip_record")
public class IpRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String name;
    private String type;
    private String obtainDate;
    @TableField("`rank`")
    private Integer rank;
    private String otherParticipants;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getObtainDate() { return obtainDate; }
    public void setObtainDate(String obtainDate) { this.obtainDate = obtainDate; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getOtherParticipants() { return otherParticipants; }
    public void setOtherParticipants(String otherParticipants) { this.otherParticipants = otherParticipants; }
}
