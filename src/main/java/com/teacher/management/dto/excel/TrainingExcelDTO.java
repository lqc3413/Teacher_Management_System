package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class TrainingExcelDTO {
    @ExcelProperty(value = {"培训类型", "如：骨干教师培训"})
    @ColumnWidth(20)
    private String type;

    @ExcelProperty(value = {"培训名称", "填写培训项目全称"})
    @ColumnWidth(28)
    private String name;

    @ExcelProperty(value = {"培训形式", "线上/线下/混合"})
    @ColumnWidth(16)
    private String form;

    @ExcelProperty(value = {"培训学时", "填写数字"})
    @ColumnWidth(12)
    private Integer hours;

    @ExcelProperty(value = {"组织单位", "主办方全称"})
    @ColumnWidth(24)
    private String organizer;

    @ExcelProperty(value = {"开始日期", "格式：2025-06-01"})
    @ColumnWidth(18)
    private String startDate;

    @ExcelProperty(value = {"结束日期", "格式：2025-06-30"})
    @ColumnWidth(18)
    private String endDate;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }

    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
