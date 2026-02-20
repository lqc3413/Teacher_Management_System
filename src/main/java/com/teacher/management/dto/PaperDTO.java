package com.teacher.management.dto;

import java.util.List;

public class PaperDTO {
    private String paperType;
    private String paperName;
    private String authorType;
    private List<String> otherAuthors;
    private String journalName;
    private String indexCategory;
    private String publishDate;

    public String getPaperType() { return paperType; }
    public void setPaperType(String paperType) { this.paperType = paperType; }

    public String getPaperName() { return paperName; }
    public void setPaperName(String paperName) { this.paperName = paperName; }

    public String getAuthorType() { return authorType; }
    public void setAuthorType(String authorType) { this.authorType = authorType; }

    public List<String> getOtherAuthors() { return otherAuthors; }
    public void setOtherAuthors(List<String> otherAuthors) { this.otherAuthors = otherAuthors; }

    public String getJournalName() { return journalName; }
    public void setJournalName(String journalName) { this.journalName = journalName; }

    public String getIndexCategory() { return indexCategory; }
    public void setIndexCategory(String indexCategory) { this.indexCategory = indexCategory; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }
}
