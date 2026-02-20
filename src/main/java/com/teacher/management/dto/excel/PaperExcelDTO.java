package com.teacher.management.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@HeadRowHeight(30)
@ContentRowHeight(22)
public class PaperExcelDTO {
    @ExcelProperty(value = {"论文类型", "学术论文/会议论文/综述/其他"})
    @ColumnWidth(20)
    private String paperType;

    @ExcelProperty(value = {"论文名称", "填写论文完整标题"})
    @ColumnWidth(32)
    private String paperName;

    @ExcelProperty(value = {"作者类型", "第一作者/通讯作者/其他"})
    @ColumnWidth(18)
    private String authorType;

    @ExcelProperty(value = {"其他作者", "多人用逗号分隔，如：张三,李四"})
    @ColumnWidth(30)
    private String otherAuthorsStr;

    @ExcelProperty(value = {"期刊名称", "填写发表期刊全称"})
    @ColumnWidth(26)
    private String journalName;

    @ExcelProperty(value = {"收录类别", "SCI/SSCI/EI/CSCD/北核/南核/普刊"})
    @ColumnWidth(22)
    private String indexCategory;

    @ExcelProperty(value = {"发表日期", "格式：2025-06"})
    @ColumnWidth(16)
    private String publishDate;

    public String getPaperType() { return paperType; }
    public void setPaperType(String paperType) { this.paperType = paperType; }

    public String getPaperName() { return paperName; }
    public void setPaperName(String paperName) { this.paperName = paperName; }

    public String getAuthorType() { return authorType; }
    public void setAuthorType(String authorType) { this.authorType = authorType; }

    public String getOtherAuthorsStr() { return otherAuthorsStr; }
    public void setOtherAuthorsStr(String otherAuthorsStr) { this.otherAuthorsStr = otherAuthorsStr; }

    public String getJournalName() { return journalName; }
    public void setJournalName(String journalName) { this.journalName = journalName; }

    public String getIndexCategory() { return indexCategory; }
    public void setIndexCategory(String indexCategory) { this.indexCategory = indexCategory; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }
}
