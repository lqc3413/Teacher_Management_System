package com.teacher.management.dto;

public class AwardDTO {
    private String name;
    private String type;
    private String level;
    private String grade;
    private Integer rank;
    private Integer orgRank;
    private String date;
    private String certNo;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public Integer getOrgRank() { return orgRank; }
    public void setOrgRank(Integer orgRank) { this.orgRank = orgRank; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }
}
