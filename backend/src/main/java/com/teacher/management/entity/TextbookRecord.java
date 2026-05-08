package com.teacher.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("sys_textbook_record")
public class TextbookRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String name;
    private String publisher;
    private String publishDate;
    private String textbookLevel;
    @TableField("`rank`")
    private Integer rank;
    private String selectionDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getTextbookLevel() { return textbookLevel; }
    public void setTextbookLevel(String textbookLevel) { this.textbookLevel = textbookLevel; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getSelectionDate() { return selectionDate; }
    public void setSelectionDate(String selectionDate) { this.selectionDate = selectionDate; }
}
