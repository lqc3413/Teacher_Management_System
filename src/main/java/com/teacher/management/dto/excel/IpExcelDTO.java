package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class IpExcelDTO {
    @ExcelProperty("知识产权名称")
    @ColumnWidth(28)
    private String name;

    @ExcelProperty("类型")
    @ColumnWidth(20)
    private String type;

    @ExcelProperty("取得日期")
    @ColumnWidth(16)
    private String date;

    @ExcelProperty("本人排名")
    @ColumnWidth(14)
    private String rank;

    @ExcelProperty("其他参与人")
    @ColumnWidth(32)
    private String otherParticipantsStr;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getOtherParticipantsStr() {
        return otherParticipantsStr;
    }

    public void setOtherParticipantsStr(String otherParticipantsStr) {
        this.otherParticipantsStr = otherParticipantsStr;
    }
}
