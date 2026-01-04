package com.example.be.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelTemplateService {

    public ByteArrayInputStream generateTemplate(String entityType) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(entityType);
            Row headerRow = sheet.createRow(0);

            String[] columns = getColumnsForEntity(entityType);

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Add a sample row to help users understand formats
            addSampleRow(sheet, entityType);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private String[] getColumnsForEntity(String entityType) {
        switch (entityType.toLowerCase()) {
            case "brands":
                return new String[] { "Name" };
            case "categories":
                return new String[] { "Name" };
            case "colors":
                return new String[] { "Color Name", "Hex Code" };
            case "sizes":
                return new String[] { "Size Value" };
            case "users":
                return new String[] { "Username", "Password", "Email" };
            case "products":
                return new String[] { "Name", "Slug", "Description", "Category ID", "Brand ID", "Active (true/false)" };
            case "product_variants": // using underscore for mapping ease
                return new String[] { "Product ID", "Size ID", "Color ID", "Price", "Stock Quantity", "SKU" };
            case "coupons":
                return new String[] { "Code", "Discount Amount", "Min Order Value", "Expiry Date (yyyy-mm-dd)" };
            case "discounts":
                return new String[] { "Product ID", "Discount Percent", "Start Date", "End Date", "Active" };
            default:
                return new String[] {};
        }
    }

    private void addSampleRow(Sheet sheet, String entityType) {
        Row row = sheet.createRow(1);
        switch (entityType.toLowerCase()) {
            case "brands":
                row.createCell(0).setCellValue("Nike");
                break;
            case "categories":
                row.createCell(0).setCellValue("Sneakers");
                break;
            case "colors":
                row.createCell(0).setCellValue("Red");
                row.createCell(1).setCellValue("#FF0000");
                break;
            case "sizes":
                row.createCell(0).setCellValue("XL");
                break;
            case "users":
                row.createCell(0).setCellValue("john_doe");
                row.createCell(1).setCellValue("secret123");
                row.createCell(2).setCellValue("john@example.com");
                break;
            case "products":
                row.createCell(0).setCellValue("Air Jordan 1");
                row.createCell(1).setCellValue("air-jordan-1");
                row.createCell(2).setCellValue("Classic basketball shoes");
                row.createCell(3).setCellValue(1);
                row.createCell(4).setCellValue(1);
                row.createCell(5).setCellValue(true);
                break;
            case "product_variants":
                row.createCell(0).setCellValue(101);
                row.createCell(1).setCellValue(1);
                row.createCell(2).setCellValue(1);
                row.createCell(3).setCellValue(150.00);
                row.createCell(4).setCellValue(50);
                row.createCell(5).setCellValue("AJ1-RED-XL");
                break;
            case "coupons":
                row.createCell(0).setCellValue("WELCOME2024");
                row.createCell(1).setCellValue(50000); // 50k
                row.createCell(2).setCellValue(200000); // min 200k
                // Note: Dates in Excel are often numeric, but users might type text.
                // The import service handles numeric dates. We'll leave it empty or simple
                // formatted.
                break;
            case "discounts":
                row.createCell(0).setCellValue(101);
                row.createCell(1).setCellValue(15.5);
                // Dates left blank or use POI date format if strict
                break;
        }
    }
}
