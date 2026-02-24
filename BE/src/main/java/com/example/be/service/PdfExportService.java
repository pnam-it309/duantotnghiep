package com.example.be.service;

import com.example.be.entity.Order;
import com.example.be.entity.OrderItem;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    public ByteArrayInputStream exportOrderPdf(Order order, List<OrderItem> items) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h1>HOÁ ĐƠN BÁN HÀNG</h1>");
        html.append("<p>Mã hoá đơn: ").append(order.getId()).append("</p>");
        html.append("<p>Khách hàng: ").append(order.getUser() != null ? order.getUser().getFullName() : "Khách vãng lai").append("</p>");
        html.append("<p>Ngày đặt: ").append(order.getCreatedAt()).append("</p>");
        html.append("<p>Phương thức thanh toán: ").append(order.getPaymentMethod()).append("</p>");
        
        html.append("<table border='1' width='100%' style='border-collapse: collapse;'>");
        html.append("<thead><tr><th>Sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead>");
        html.append("<tbody>");
        
        for (OrderItem item : items) {
            html.append("<tr>");
            html.append("<td>").append(item.getProductVariant().getProduct().getName()).append(" (").append(item.getProductVariant().getSku()).append(")</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>").append(item.getPrice()).append("</td>");
            html.append("<td>").append(item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity()))).append("</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody></table>");
        html.append("<h3 style='text-align: right;'>Tổng cộng: ").append(order.getFinalTotal()).append("</h3>");
        html.append("</body></html>");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(html.toString(), out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
