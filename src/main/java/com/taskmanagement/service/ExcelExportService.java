package com.taskmanagement.service;

import com.taskmanagement.entity.Board;
import com.taskmanagement.entity.ShipmentEntry;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public byte[] exportBoardToExcel(Board board, List<ShipmentEntry> entries) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(board.getTitle());

            // Create header styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle groupHeaderStyle = createGroupHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle calculatedStyle = createCalculatedStyle(workbook);

            int rowNum = 0;

            // Title row
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(board.getTitle());
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            rowNum++; // Empty row

            // Group headers
            Row groupRow = sheet.createRow(rowNum++);
            int colNum = 0;
            
            Cell groupCell1 = groupRow.createCell(colNum);
            groupCell1.setCellValue("Loading Info");
            groupCell1.setCellStyle(groupHeaderStyle);
            colNum += 4;
            
            Cell groupCell2 = groupRow.createCell(colNum);
            groupCell2.setCellValue("Unloading & Transit Info");
            groupCell2.setCellStyle(groupHeaderStyle);
            colNum += 5;
            
            Cell groupCell3 = groupRow.createCell(colNum);
            groupCell3.setCellValue("Product & Financials");
            groupCell3.setCellStyle(groupHeaderStyle);

            // Column headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {
                "Consignee", "Lighter Vessel Name", "Vessel Destination", "Date",
                "Challan No", "Converting Vessel", "No of Trucks", "Discharging Location", "Final Destination",
                "Item Name", "Billable Quantity", "Lighter Cost", "Unload Cost", "Truck Cost", 
                "Total Unit Costing", "Final Amount"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (ShipmentEntry entry : entries) {
                Row row = sheet.createRow(rowNum++);
                colNum = 0;

                // Loading Info
                createCell(row, colNum++, entry.getConsignee(), dataStyle);
                createCell(row, colNum++, entry.getLighterVesselName(), dataStyle);
                createCell(row, colNum++, entry.getVesselDestination(), dataStyle);
                createCell(row, colNum++, entry.getDate() != null ? entry.getDate().format(DATE_FORMATTER) : "", dataStyle);

                // Unloading & Transit Info
                createCell(row, colNum++, entry.getChallanNo(), dataStyle);
                createCell(row, colNum++, entry.getConvertingVessel(), dataStyle);
                createCell(row, colNum++, entry.getNoOfTrucks() != null ? entry.getNoOfTrucks().toString() : "", dataStyle);
                createCell(row, colNum++, entry.getDischargingLocation(), dataStyle);
                createCell(row, colNum++, entry.getFinalDestination(), dataStyle);

                // Product & Financials
                createCell(row, colNum++, entry.getItemName(), dataStyle);
                createNumericCell(row, colNum++, entry.getBillableQuantity(), dataStyle);
                createNumericCell(row, colNum++, entry.getLighterCost(), dataStyle);
                createNumericCell(row, colNum++, entry.getUnloadCost(), dataStyle);
                createNumericCell(row, colNum++, entry.getTruckCost(), dataStyle);
                createNumericCell(row, colNum++, entry.getTotalUnitCosting(), calculatedStyle);
                createNumericCell(row, colNum++, entry.getFinalAmount(), calculatedStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createGroupHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCalculatedStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.GREEN.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(Double.parseDouble(value.toString()));
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }
}
