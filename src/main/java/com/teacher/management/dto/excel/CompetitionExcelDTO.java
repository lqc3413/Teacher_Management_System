package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class CompetitionExcelDTO {
    @ExcelProperty(value = {"竞赛类别", "A类/B类/C类/D类"})
    @ColumnWidth(16)
    private String category;

    @ExcelProperty(value = {"竞赛名称", "填写竞赛全称"})
    @ColumnWidth(28)
    private String name;

    @ExcelProperty(value = {"主办单位", "以获奖证书为准"})
    @ColumnWidth(24)
    private String organizer;

    @ExcelProperty(value = {"获奖时间", "格式：2025-06-15"})
    @ColumnWidth(18)
    private String awardDate;

    @ExcelProperty(value = {"证书编号", "获奖证书上的编号"})
    @ColumnWidth(20)
    private String certNo;

    @ExcelProperty(value = {"证书名称", "获奖证书完整名称"})
    @ColumnWidth(28)
    private String certName;

    @ExcelProperty(value = {"奖项级别", "国家级/省级"})
    @ColumnWidth(14)
    private String awardLevel;

    @ExcelProperty(value = {"奖项等级", "特等奖/一等奖/二等奖/三等奖"})
    @ColumnWidth(18)
    private String awardGrade;

    @ExcelProperty(value = {"参赛学生", "多人用逗号分隔"})
    @ColumnWidth(28)
    private String studentsStr;

    @ExcelProperty(value = {"指导教师", "多人用逗号分隔"})
    @ColumnWidth(28)
    private String advisorTeachersStr;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getAwardDate() { return awardDate; }
    public void setAwardDate(String awardDate) { this.awardDate = awardDate; }

    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }

    public String getCertName() { return certName; }
    public void setCertName(String certName) { this.certName = certName; }

    public String getAwardLevel() { return awardLevel; }
    public void setAwardLevel(String awardLevel) { this.awardLevel = awardLevel; }

    public String getAwardGrade() { return awardGrade; }
    public void setAwardGrade(String awardGrade) { this.awardGrade = awardGrade; }

    public String getStudentsStr() { return studentsStr; }
    public void setStudentsStr(String studentsStr) { this.studentsStr = studentsStr; }

    public String getAdvisorTeachersStr() { return advisorTeachersStr; }
    public void setAdvisorTeachersStr(String advisorTeachersStr) { this.advisorTeachersStr = advisorTeachersStr; }
}
