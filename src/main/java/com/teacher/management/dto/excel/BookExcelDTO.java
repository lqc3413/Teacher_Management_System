package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class BookExcelDTO {
    @ExcelProperty("著作名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("出版社")
    @ColumnWidth(22)
    private String publisher;

    @ExcelProperty("出版日期")
    @ColumnWidth(16)
    private String date;

    @ExcelProperty("教材等级")
    @ColumnWidth(20)
    private String level;

    @ExcelProperty("本人排名")
    @ColumnWidth(14)
    private String rank;

    @ExcelProperty("入选日期")
    @ColumnWidth(16)
    private String selectionDate;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getSelectionDate() {
        return selectionDate;
    }

    public void setSelectionDate(String selectionDate) {
        this.selectionDate = selectionDate;
    }
}
