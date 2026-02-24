package com.example.be.controller;

import com.example.be.entity.Order;
import com.example.be.entity.Product;
import com.example.be.entity.User;
import com.example.be.repository.OrderRepository;
import com.example.be.repository.ProductRepository;
import com.example.be.repository.UserRepository;
import com.example.be.service.ExcelExportService;
import com.example.be.service.OrderService;
import com.example.be.service.PdfExportService;
import com.example.be.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @GetMapping("/products/excel")
    public ResponseEntity<InputStreamResource> exportProductsExcel() throws IOException {
        List<Product> products = productRepository.findAll();
        ByteArrayInputStream in = excelExportService.exportProducts(products);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=products.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/orders/excel")
    public ResponseEntity<InputStreamResource> exportOrdersExcel() throws IOException {
        List<Order> orders = orderRepository.findAll();
        ByteArrayInputStream in = excelExportService.exportOrders(orders);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=orders.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/orders/pdf")
    public ResponseEntity<InputStreamResource> exportOrderPdf(@RequestParam Long orderId) throws IOException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        var items = orderService.getOrderItems(orderId);
        ByteArrayInputStream in = pdfExportService.exportOrderPdf(order, items);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=order_" + orderId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }

    @GetMapping("/users/excel")
    public ResponseEntity<InputStreamResource> exportUsersExcel() throws IOException {
        List<User> users = userRepository.findAll();
        ByteArrayInputStream in = excelExportService.exportUsers(users);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=users.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
