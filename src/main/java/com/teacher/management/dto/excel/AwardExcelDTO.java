package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class AwardExcelDTO {
    @ExcelProperty(value = {"获奖名称", "填写获奖成果完整名称"})
    @ColumnWidth(28)
    private String name;

    @ExcelProperty(value = {"获奖类型", "教学成果奖/科研成果奖/其他"})
    @ColumnWidth(20)
    private String type;

    @ExcelProperty(value = {"获奖级别", "国家级/省级/市级/校级"})
    @ColumnWidth(18)
    private String level;

    @ExcelProperty(value = {"获奖等级", "特等奖/一等奖/二等奖/三等奖"})
    @ColumnWidth(18)
    private String grade;

    @ExcelProperty(value = {"本人排名", "填写数字，如 1"})
    @ColumnWidth(14)
    private Integer rank;

    @ExcelProperty(value = {"集体排名", "填写数字，如 1"})
    @ColumnWidth(14)
    private Integer orgRank;

    @ExcelProperty(value = {"获奖日期", "格式：2025-06-15"})
    @ColumnWidth(18)
    private String date;

    @ExcelProperty(value = {"证书编号", "获奖证书上的编号"})
    @ColumnWidth(20)
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
