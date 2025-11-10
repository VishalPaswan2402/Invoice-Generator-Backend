package com.vishalpaswan.invoiceGen.excelHelper;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Service
public class DataToExcel {
    private final String[] headData = excelHeader.topHeader;

    private ByteArrayInputStream dataToExcel(ArrayList<ExcelDataInfo> excelSheetData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet excelSheet = workbook.createSheet();

            // Create bold font for header
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            // Header cell style (bold + border + background)
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Add borders to header
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Optional: background color for header
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Regular data cell style (border)
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // row number
            Row row = excelSheet.createRow(0);

            // set header data in Excel table
            for (int i = 0; i < headData.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(headData[i]);
                cell.setCellStyle(headerStyle);
            }

            // set values in Excel table
            int dataRow = 1;
            for (ExcelDataInfo data : excelSheetData) {
                Row currRow = excelSheet.createRow(dataRow);
                for (int i = 0; i < headData.length; i++) {
                    Cell currCell = currRow.createCell(i);
                    switch (i) {
                        case 0 -> currCell.setCellValue(data.getSrNo());
                        case 1 -> currCell.setCellValue(data.getInvoiceNo());
                        case 2 -> currCell.setCellValue(data.getCustomerName());
                        case 3 -> currCell.setCellValue(data.getCustomerEmail());
                        case 4 -> currCell.setCellValue(data.getCustomerPhone());
                        case 5 -> currCell.setCellValue(data.getCustomerAddress());
                        case 6 -> currCell.setCellValue(data.getBillingDate());
                        case 7 -> currCell.setCellValue(data.getDueDate());
                        case 8 -> currCell.setCellValue(data.getTotalItems());
                        case 9 -> currCell.setCellValue(data.getTotalCost());
                        case 10 -> currCell.setCellValue(data.getPaidAmount());
                        case 11 -> currCell.setCellValue(data.getPendingAmount());
                        case 12 -> currCell.setCellValue(data.getStatus());
                    }
                    currCell.setCellStyle(dataStyle);
                }
                dataRow++;
            }

            // Auto-size all columns
            for (int i = 0; i < headData.length; i++) {
                excelSheet.autoSizeColumn(i);
            }

            // put sheet in workbook
            workbook.write(outputStream);
            log.info("Excel sheet is generating...");
            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (Exception ex) {
            log.error("Something went wrong while converting data to excel. : {}", ex.getMessage());
            return null;

        }
    }

    public ByteArrayInputStream convertDataToExcel(ArrayList<ExcelDataInfo> dataValues) {
        try {
            log.info("Excel sheet is processing...");
            return dataToExcel(dataValues);
        } catch (Exception ex) {
            log.error("Error while converting to excel. Please try again. : {}", ex.getMessage());
            return null;
        }
    }

}
