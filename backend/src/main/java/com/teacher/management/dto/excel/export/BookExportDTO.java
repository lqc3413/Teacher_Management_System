package com.teacher.management.dto.excel.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class BookExportDTO {
    @ExcelProperty("教师姓名")
    @ColumnWidth(16)
    private String teacherName;

    @ExcelProperty("工号")
    @ColumnWidth(14)
    private String employeeNo;

    @ExcelProperty("著作名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("出版社")
    @ColumnWidth(22)
    private String publisher;

    @ExcelProperty("出版日期")
    @ColumnWidth(16)
    private String publishDate;

    @ExcelProperty("教材等级")
    @ColumnWidth(20)
    private String textbookLevel;

    @ExcelProperty("本人排名")
    @ColumnWidth(14)
    private Integer rank;

    @ExcelProperty("入选日期")
    @ColumnWidth(16)
    private String selectionDate;

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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getTextbookLevel() {
        return textbookLevel;
    }

    public void setTextbookLevel(String textbookLevel) {
        this.textbookLevel = textbookLevel;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getSelectionDate() {
        return selectionDate;
    }

    public void setSelectionDate(String selectionDate) {
        this.selectionDate = selectionDate;
    }
}
