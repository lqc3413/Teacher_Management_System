package com.teacher.management.dto.excel.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class IpExportDTO {
    @ExcelProperty("教师姓名")
    @ColumnWidth(16)
    private String teacherName;

    @ExcelProperty("工号")
    @ColumnWidth(14)
    private String employeeNo;

    @ExcelProperty("知识产权名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("类型")
    @ColumnWidth(20)
    private String type;

    @ExcelProperty("取得日期")
    @ColumnWidth(16)
    private String obtainDate;

    @ExcelProperty("本人排名")
    @ColumnWidth(14)
    private Integer rank;

    @ExcelProperty("其他参与人")
    @ColumnWidth(32)
    private String otherParticipants;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObtainDate() {
        return obtainDate;
    }

    public void setObtainDate(String obtainDate) {
        this.obtainDate = obtainDate;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getOtherParticipants() {
        return otherParticipants;
    }

    public void setOtherParticipants(String otherParticipants) {
        this.otherParticipants = otherParticipants;
    }
}
