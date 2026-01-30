package org.joker.comfypilot.common.tool.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Skills 文档读取工具类
 * 提供 Excel、PDF、Word 等文档格式的读取功能
 * 所有操作都限制在配置的 skills 目录下
 */
@Slf4j
@Component
@ToolSet
public class SkillsDocumentTools {

    private final SkillsRegistry skillsRegistry;
    private final ObjectMapper objectMapper;

    public SkillsDocumentTools(SkillsRegistry skillsRegistry) {
        this.skillsRegistry = skillsRegistry;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 列出 Excel 文件中的所有工作表名称
     *
     * @param path Excel 文件路径
     * @return 工作表名称列表（JSON 数组格式）
     * @throws IOException IO 异常
     */
    @Tool(name = "listExcelSheets", value = "列出 Skills 目录下的 Excel 文件中所有工作表的名称")
    public String listExcelSheets(@P(value = "Excel 文件的完整路径", required = true) String path) throws IOException {
        validatePathInSkillsDirectory(path);

        List<String> sheetNames = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = WorkbookFactory.create(fis)) {

            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
        }

        return objectMapper.writeValueAsString(sheetNames);
    }

    /**
     * 读取 Excel 文件的特定工作表内容
     *
     * @param path      Excel 文件路径
     * @param sheetName 工作表名称（可选，默认读取第一个工作表）
     * @return 工作表数据（JSON 格式）
     * @throws IOException IO 异常
     */
    @Tool(name = "readSkillExcel", value = "读取 Skills 目录下的 Excel 文件内容，返回指定工作表的数据")
    public String readSkillExcel(
            @P(value = "Excel 文件的完整路径", required = true) String path,
            @P(value = "工作表名称，为空时读取第一个工作表", required = false) String sheetName) throws IOException {

        validatePathInSkillsDirectory(path);

        Map<String, Object> result = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet;
            if (sheetName == null || sheetName.trim().isEmpty()) {
                sheet = workbook.getSheetAt(0);
            } else {
                sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new IOException("工作表不存在: " + sheetName);
                }
            }

            result.put("sheetName", sheet.getSheetName());
            result.put("data", parseSheet(sheet));
            result.put("totalRows", sheet.getLastRowNum() + 1);
        }

        return objectMapper.writeValueAsString(result);
    }

    /**
     * 读取 Excel 文件的所有工作表内容
     *
     * @param path Excel 文件路径
     * @return 所有工作表的数据（JSON 格式）
     * @throws IOException IO 异常
     */
    @Tool(name = "readSkillExcelAllSheets", value = "读取 Skills 目录下的 Excel 文件的所有工作表内容")
    public String readSkillExcelAllSheets(@P(value = "Excel 文件的完整路径", required = true) String path) throws IOException {
        validatePathInSkillsDirectory(path);

        List<Map<String, Object>> sheets = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = WorkbookFactory.create(fis)) {

            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Map<String, Object> sheetData = new HashMap<>();
                sheetData.put("sheetName", sheet.getSheetName());
                sheetData.put("data", parseSheet(sheet));
                sheetData.put("totalRows", sheet.getLastRowNum() + 1);
                sheets.add(sheetData);
            }
        }

        return objectMapper.writeValueAsString(sheets);
    }

    /**
     * 读取 PDF 文件的文本内容
     *
     * @param path PDF 文件路径
     * @return PDF 文本内容
     * @throws IOException IO 异常
     */
    @Tool(name = "readSkillPdf", value = "读取 Skills 目录下的 PDF 文件文本内容")
    public String readSkillPdf(@P(value = "PDF 文件的完整路径", required = true) String path) throws IOException {
        validatePathInSkillsDirectory(path);

        File pdfFile = new File(path);
        if (!pdfFile.exists()) {
            throw new IOException("PDF 文件不存在: " + path);
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 读取 PDF 文件的指定页面文本内容
     *
     * @param path      PDF 文件路径
     * @param startPage 起始页码（从 1 开始）
     * @param endPage   结束页码（包含）
     * @return PDF 指定页面的文本内容
     * @throws IOException IO 异常
     */
    @Tool(name = "readSkillPdfPages", value = "读取 Skills 目录下的 PDF 文件指定页面的文本内容")
    public String readSkillPdfPages(
            @P(value = "PDF 文件的完整路径", required = true) String path,
            @P(value = "起始页码（从 1 开始）", required = true) int startPage,
            @P(value = "结束页码（包含）", required = true) int endPage) throws IOException {

        validatePathInSkillsDirectory(path);

        if (startPage < 1 || endPage < startPage) {
            throw new IllegalArgumentException("无效的页码范围: " + startPage + " - " + endPage);
        }

        File pdfFile = new File(path);
        if (!pdfFile.exists()) {
            throw new IOException("PDF 文件不存在: " + path);
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(Math.min(endPage, document.getNumberOfPages()));
            return stripper.getText(document);
        }
    }

    /**
     * 获取 PDF 文件的页数
     *
     * @param path PDF 文件路径
     * @return PDF 总页数
     * @throws IOException IO 异常
     */
    @Tool(name = "getSkillPdfPageCount", value = "获取 Skills 目录下的 PDF 文件的总页数")
    public int getSkillPdfPageCount(@P(value = "PDF 文件的完整路径", required = true) String path) throws IOException {
        validatePathInSkillsDirectory(path);

        File pdfFile = new File(path);
        if (!pdfFile.exists()) {
            throw new IOException("PDF 文件不存在: " + path);
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            return document.getNumberOfPages();
        }
    }

    /**
     * 读取 Word 文档（.docx）的文本内容
     *
     * @param path Word 文件路径
     * @return Word 文档的文本内容
     * @throws IOException IO 异常
     */
    @Tool(name = "readSkillWord", value = "读取 Skills 目录下的 Word 文档（.docx）的文本内容")
    public String readSkillWord(@P(value = "Word 文件的完整路径", required = true) String path) throws IOException {
        validatePathInSkillsDirectory(path);

        File wordFile = new File(path);
        if (!wordFile.exists()) {
            throw new IOException("Word 文件不存在: " + path);
        }

        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(wordFile);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text).append("\n");
                }
            }
        }

        return content.toString();
    }

    /**
     * 读取 Word 文档的结构化内容（包含段落和样式信息）
     *
     * @param path Word 文件路径
     * @return Word 文档的结构化内容（JSON 格式）
     * @throws IOException IO 异常
     */
    @Tool(name = "readSkillWordStructured", value = "读取 Skills 目录下的 Word 文档的结构化内容（包含段落和样式）")
    public String readSkillWordStructured(@P(value = "Word 文件的完整路径", required = true) String path) throws IOException {
        validatePathInSkillsDirectory(path);

        File wordFile = new File(path);
        if (!wordFile.exists()) {
            throw new IOException("Word 文件不存在: " + path);
        }

        List<Map<String, Object>> paragraphs = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(wordFile);
             XWPFDocument document = new XWPFDocument(fis)) {

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    Map<String, Object> paraInfo = new HashMap<>();
                    paraInfo.put("text", text);
                    paraInfo.put("style", paragraph.getStyle());
                    paraInfo.put("alignment", paragraph.getAlignment().toString());
                    paragraphs.add(paraInfo);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paragraphs", paragraphs);
        result.put("totalParagraphs", paragraphs.size());

        return objectMapper.writeValueAsString(result);
    }

    /**
     * 解析 Excel 工作表为列表数据
     *
     * @param sheet Excel 工作表
     * @return 解析后的数据列表
     */
    private List<List<Object>> parseSheet(Sheet sheet) {
        List<List<Object>> data = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.##########");

        for (Row row : sheet) {
            List<Object> rowData = new ArrayList<>();
            for (Cell cell : row) {
                Object value = getCellValue(cell, df);
                rowData.add(value);
            }
            data.add(rowData);
        }

        return data;
    }

    /**
     * 获取单元格的值
     *
     * @param cell 单元格
     * @param df   数字格式化器
     * @return 单元格的值
     */
    private Object getCellValue(Cell cell, DecimalFormat df) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，返回整数格式
                    if (numericValue == Math.floor(numericValue)) {
                        return (long) numericValue;
                    }
                    return Double.parseDouble(df.format(numericValue));
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            case BLANK:
                return null;
            case ERROR:
                return "ERROR:" + cell.getErrorCellValue();
            default:
                return null;
        }
    }

    /**
     * 校验路径是否在配置的 skills 目录下
     *
     * @param path 要校验的路径
     * @throws SecurityException 如果路径不在 skills 目录下
     */
    private void validatePathInSkillsDirectory(String path) {
        try {
            Path filePath = Paths.get(path).toAbsolutePath().normalize();

            // 检查路径是否在任意一个配置的 skills 目录下
            boolean inSkillsDir = false;
            for (Path configuredDir : skillsRegistry.getConfiguredDirectories()) {
                if (filePath.startsWith(configuredDir)) {
                    inSkillsDir = true;
                    break;
                }
            }

            if (!inSkillsDir) {
                throw new SecurityException("安全限制：只能访问配置的 Skills 目录下的文件。配置的目录: "
                        + skillsRegistry.getConfiguredDirectories());
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("路径校验失败: " + path, e);
        }
    }
}
