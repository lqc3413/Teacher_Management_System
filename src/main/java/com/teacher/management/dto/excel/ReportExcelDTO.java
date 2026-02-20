package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class ReportExcelDTO {
    @ExcelProperty(value = {"报告名称", "填写报告完整名称"})
    @ColumnWidth(28)
    private String name;

    @ExcelProperty(value = {"采纳级别", "国家级/省部级/地厅级/县处级"})
    @ColumnWidth(20)
    private String level;

    @ExcelProperty(value = {"采纳日期", "格式：2025-06"})
    @ColumnWidth(16)
    private String date;

    @ExcelProperty(value = {"本人排名", "填写数字，如 1"})
    @ColumnWidth(14)
    private Integer rank;

    @ExcelProperty(value = {"其他参与人", "多人用逗号分隔，如：张三,李四"})
    @ColumnWidth(32)
    private String othersStr;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getOthersStr() { return othersStr; }
    public void setOthersStr(String othersStr) { this.othersStr = othersStr; }
}
