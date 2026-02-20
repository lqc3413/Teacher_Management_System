package com.teacher.management.dto;

import java.util.List;

public class IpDTO {
    private String name;
    private String type;
    private String date;
    private Integer rank;
    private List<String> otherParticipants;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public List<String> getOtherParticipants() { return otherParticipants; }
    public void setOtherParticipants(List<String> otherParticipants) { this.otherParticipants = otherParticipants; }
}
