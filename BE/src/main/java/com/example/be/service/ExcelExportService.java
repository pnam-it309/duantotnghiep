package com.example.be.service;

import com.example.be.entity.Order;
import com.example.be.entity.Product;
import com.example.be.entity.User;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    public ByteArrayInputStream exportProducts(List<Product> products) throws IOException {
        String[] columns = {"ID", "Name", "Description", "Category", "Brand", "Active"};
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");

            // Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Data
            int rowIdx = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getDescription());
                row.createCell(3).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "");
                row.createCell(4).setCellValue(product.getBrand() != null ? product.getBrand().getName() : "");
                row.createCell(5).setCellValue(product.getActive() ? "Yes" : "No");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportOrders(List<Order> orders) throws IOException {
        String[] columns = {"ID", "Customer", "Total", "Status", "Payment Method", "Created At"};
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Orders");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getUser() != null ? order.getUser().getFullName() : "N/A");
                row.createCell(2).setCellValue(order.getFinalTotal().doubleValue());
                row.createCell(3).setCellValue(order.getStatus());
                row.createCell(4).setCellValue(order.getPaymentMethod());
                row.createCell(5).setCellValue(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportUsers(List<User> users) throws IOException {
        String[] columns = {"ID", "Username", "Email", "Full Name", "Phone", "Role", "Points"};
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getFullName());
                row.createCell(4).setCellValue(user.getPhoneNumber());
                row.createCell(5).setCellValue(user.getRole());
                //row.createCell(6).setCellValue(user.getRewardPoints() != null ? user.getRewardPoints() : 0);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
