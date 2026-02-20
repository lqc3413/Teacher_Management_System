package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class IpExcelDTO {
    @ExcelProperty(value = {"知识产权名称", "请填写完整名称"})
    @ColumnWidth(28)
    private String name;

    @ExcelProperty(value = {"类型", "发明专利/实用新型/软件著作权"})
    @ColumnWidth(20)
    private String type;

    @ExcelProperty(value = {"取得日期", "格式：2025-06"})
    @ColumnWidth(16)
    private String date;

    @ExcelProperty(value = {"本人排名", "填写数字，如 1"})
    @ColumnWidth(14)
    private Integer rank;

    @ExcelProperty(value = {"其他参与人", "多人用逗号分隔，如：张三,李四"})
    @ColumnWidth(32)
    private String otherParticipantsStr;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public String getOtherParticipantsStr() { return otherParticipantsStr; }
    public void setOtherParticipantsStr(String otherParticipantsStr) { this.otherParticipantsStr = otherParticipantsStr; }
}
