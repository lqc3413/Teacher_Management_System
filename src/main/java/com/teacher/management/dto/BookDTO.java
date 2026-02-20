package com.teacher.management.dto;

public class BookDTO {
    private String name;
    private String publisher;
    private String date;
    private String level;
    private Integer rank;
    private String selectionDate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getSelectionDate() { return selectionDate; }
    public void setSelectionDate(String selectionDate) { this.selectionDate = selectionDate; }
}
