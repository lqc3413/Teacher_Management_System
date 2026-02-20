package com.teacher.management.handler;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import org.apache.poi.ss.usermodel.*;

/**
 * Excel 模板样式处理器 —— 仅负责覆盖第二行（提示行）为弱化灰色小字样式
 * 配合 HorizontalCellStyleStrategy 使用，order 设为 60000 确保在其之后执行
 */
public class TemplateStyleHandler implements CellWriteHandler {

    private CellStyle hintStyle;
    private boolean initialized = false;

    /**
     * 必须大于 HorizontalCellStyleStrategy 的 50000，
     * 这样本 handler 在 HorizontalCellStyleStrategy 之后执行，能成功覆盖第二行样式
     */
    @Override
    public int order() {
        return 60000;
    }

    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        Cell cell = context.getCell();
        if (cell == null) return;

        int rowIndex = cell.getRowIndex();

        // 第一行表头：设置行高
        if (rowIndex == 0) {
            cell.getRow().setHeightInPoints(34);
        }
        // 第二行提示行：覆盖为弱化样式
        else if (rowIndex == 1) {
            if (!initialized) {
                initHintStyle(cell.getSheet().getWorkbook());
            }
            cell.setCellStyle(hintStyle);
            cell.getRow().setHeightInPoints(36);
        }
    }

    private synchronized void initHintStyle(Workbook workbook) {
        if (initialized) return;

        hintStyle = workbook.createCellStyle();
        Font sf = workbook.createFont();
        sf.setItalic(true);
        sf.setFontHeightInPoints((short) 8);
        sf.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        sf.setFontName("微软雅黑");
        hintStyle.setFont(sf);
        hintStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        hintStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        hintStyle.setAlignment(HorizontalAlignment.CENTER);
        hintStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        hintStyle.setWrapText(true);
        hintStyle.setBorderBottom(BorderStyle.THIN);
        hintStyle.setBorderTop(BorderStyle.THIN);
        hintStyle.setBorderLeft(BorderStyle.THIN);
        hintStyle.setBorderRight(BorderStyle.THIN);

        initialized = true;
    }
}
