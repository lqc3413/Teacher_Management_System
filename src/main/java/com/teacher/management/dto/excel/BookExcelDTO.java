package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class BookExcelDTO {
    @ExcelProperty(value = {"著作名称", "填写著作完整名称"})
    @ColumnWidth(28)
    private String name;

    @ExcelProperty(value = {"出版社", "出版社全称"})
    @ColumnWidth(22)
    private String publisher;

    @ExcelProperty(value = {"出版日期", "格式：2025-06"})
    @ColumnWidth(16)
    private String date;

    @ExcelProperty(value = {"教材等级", "国家级/省级/校级/未入选"})
    @ColumnWidth(20)
    private String level;

    @ExcelProperty(value = {"本人排名", "填写数字，如 1"})
    @ColumnWidth(14)
    private Integer rank;

    @ExcelProperty(value = {"入选日期", "格式：2025-06"})
    @ColumnWidth(16)
    private String selectionDate;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getSelectionDate() { return selectionDate; }
    public void setSelectionDate(String selectionDate) { this.selectionDate = selectionDate; }
}
