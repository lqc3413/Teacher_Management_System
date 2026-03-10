package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class AwardExcelDTO {
    @ExcelProperty("获奖名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("获奖类型")
    @ColumnWidth(20)
    private String type;

    @ExcelProperty("获奖级别")
    @ColumnWidth(18)
    private String level;

    @ExcelProperty("获奖等级")
    @ColumnWidth(18)
    private String grade;

    @ExcelProperty("本人排名")
    @ColumnWidth(14)
    private String rank;

    @ExcelProperty("集体排名")
    @ColumnWidth(14)
    private String orgRank;

    @ExcelProperty("获奖日期")
    @ColumnWidth(18)
    private String date;

    @ExcelProperty("证书编号")
    @ColumnWidth(20)
    private String certNo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getOrgRank() {
        return orgRank;
    }

    public void setOrgRank(String orgRank) {
        this.orgRank = orgRank;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }
}
