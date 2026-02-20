package com.teacher.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("sys_paper_record")
public class PaperRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String paperType;
    private String paperName;
    private String authorType;
    private String otherAuthors;
    private String journalName;
    private String indexCategory;
    private String publishDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public String getPaperType() { return paperType; }
    public void setPaperType(String paperType) { this.paperType = paperType; }

    public String getPaperName() { return paperName; }
    public void setPaperName(String paperName) { this.paperName = paperName; }

    public String getAuthorType() { return authorType; }
    public void setAuthorType(String authorType) { this.authorType = authorType; }

    public String getOtherAuthors() { return otherAuthors; }
    public void setOtherAuthors(String otherAuthors) { this.otherAuthors = otherAuthors; }

    public String getJournalName() { return journalName; }
    public void setJournalName(String journalName) { this.journalName = journalName; }

    public String getIndexCategory() { return indexCategory; }
    public void setIndexCategory(String indexCategory) { this.indexCategory = indexCategory; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }
}
