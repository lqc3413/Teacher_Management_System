package com.teacher.management.dto;

import java.util.List;

public class ReportDTO {
    private String name;
    private String level;
    private String date;
    private Integer rank;
    private List<String> others;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public List<String> getOthers() { return others; }
    public void setOthers(List<String> others) { this.others = others; }
}
