package com.teacher.management.dto.excel.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class ReportExportDTO {
    @ExcelProperty("教师姓名")
    @ColumnWidth(16)
    private String teacherName;

    @ExcelProperty("工号")
    @ColumnWidth(14)
    private String employeeNo;

    @ExcelProperty("报告名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("采纳级别")
    @ColumnWidth(20)
    private String level;

    @ExcelProperty("采纳日期")
    @ColumnWidth(16)
    private String adoptDate;

    @ExcelProperty("本人排名")
    @ColumnWidth(14)
    private Integer rank;

    @ExcelProperty("其他参与人")
    @ColumnWidth(32)
    private String others;

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAdoptDate() {
        return adoptDate;
    }

    public void setAdoptDate(String adoptDate) {
        this.adoptDate = adoptDate;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }
}
