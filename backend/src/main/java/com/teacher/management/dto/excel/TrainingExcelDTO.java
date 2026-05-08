package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class TrainingExcelDTO {
    @ExcelProperty("培训类型")
    @ColumnWidth(20)
    private String type;

    @ExcelProperty("培训名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("培训形式")
    @ColumnWidth(16)
    private String form;

    @ExcelProperty("培训学时")
    @ColumnWidth(12)
    private String hours;

    @ExcelProperty("组织单位")
    @ColumnWidth(24)
    private String organizer;

    @ExcelProperty("开始日期")
    @ColumnWidth(18)
    private String startDate;

    @ExcelProperty("结束日期")
    @ColumnWidth(18)
    private String endDate;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
