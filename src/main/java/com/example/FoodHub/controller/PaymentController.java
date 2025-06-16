package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.InvoiceResponse;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.dto.response.RevenueStatsResponseForCashier;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.service.EmailService;
import com.example.FoodHub.service.PaymentService;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cashier")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;


    @Autowired
    private EmailService emailService;

    // Thanh toán đơn hàng
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    // Hủy/Hoàn tiền đơn hàng
    @PostMapping("/cancel-or-refund/{orderId}")
    public ResponseEntity<ApiResponse<String>> cancelOrRefundOrder(@PathVariable Integer orderId) {
        paymentService.cancelOrRefundOrder(orderId);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .result("Đơn hàng đã được hủy thành công")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    // Lấy danh sách giao dịch theo ngày
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getTransactionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(required = false) String status) {
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        List<PaymentResponse> transactions;
        if (status != null) {
            transactions = paymentService.getTransactionsByDateAndStatus(start, end, status);
        } else {
            transactions = paymentService.getTransactionsByDate(start, end);
        }
        ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder()
                .result(transactions)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    // Lấy tổng doanh thu theo ngày
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenueByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date) {
        BigDecimal totalRevenue = paymentService.getTotalRevenueByDate(date);
        ApiResponse<BigDecimal> apiResponse = ApiResponse.<BigDecimal>builder()
                .result(totalRevenue)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    // Lấy thống kê doanh thu
    @GetMapping("/revenue-stats")
    public ResponseEntity<ApiResponse<RevenueStatsResponseForCashier>> getRevenueStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        if (date != null) {
            RevenueStatsResponseForCashier stats = paymentService.getRevenueStatsByDate(date);
            ApiResponse<RevenueStatsResponseForCashier> apiResponse = ApiResponse.<RevenueStatsResponseForCashier>builder()
                    .result(stats)
                    .build();
            return ResponseEntity.ok().body(apiResponse);
        } else if (start != null && end != null) {
            if (end.isBefore(start)) {
                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
            }
            RevenueStatsResponseForCashier stats = paymentService.getRevenueStatsByDateRange(start, end);
            ApiResponse<RevenueStatsResponseForCashier> apiResponse = ApiResponse.<RevenueStatsResponseForCashier>builder()
                    .result(stats)
                    .build();
            return ResponseEntity.ok().body(apiResponse);
        } else {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    // Lấy thông tin hóa đơn
    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable Integer orderId) {
        InvoiceResponse response = paymentService.getOrderDetails(orderId);
        if (response == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        ApiResponse<InvoiceResponse> apiResponse = ApiResponse.<InvoiceResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }



    // Gửi email hóa đơn
    @PostMapping("/send-invoice-email")
    public ResponseEntity<ApiResponse<String>> sendInvoiceEmail(@RequestBody Map<String, String> request) {
        try {
            String customerEmail = request.get("customerEmail");
            Integer orderId = Integer.parseInt(request.get("orderId"));
            InvoiceResponse invoiceResponse = paymentService.getOrderDetails(orderId);
            if (invoiceResponse == null) {
                throw new AppException(ErrorCode.ORDER_NOT_FOUND);
            }
            emailService.sendInvoiceEmailAsync(customerEmail, invoiceResponse);
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .result("Email invoice sent successfully")
                    .build();
            return ResponseEntity.ok().body(apiResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.<String>builder()
                    .result("Failed to send email: " + e.getMessage())
                    .build());
        }
    }



    // Tạo và trả về file PDF hóa đơn
    @GetMapping("/invoice/{orderId}/pdf")
    public ResponseEntity<ByteArrayResource> generateInvoicePdf(@PathVariable Integer orderId) throws IOException {
        log.info("Generating PDF invoice for order ID: {}", orderId);

        // Lấy dữ liệu hóa đơn từ API response
        ResponseEntity<ApiResponse<InvoiceResponse>> invoiceResponse = getInvoice(orderId);
        if (invoiceResponse.getStatusCode().isError() || invoiceResponse.getBody() == null || invoiceResponse.getBody().getResult() == null) {
            log.error("Invoice data not found or error for order ID: {}", orderId);
            throw new AppException(ErrorCode.ORDER_NOT_FOUND, "Invoice data not found for order ID: " + orderId);
        }
        InvoiceResponse invoice = invoiceResponse.getBody().getResult();

        // Kiểm tra trạng thái PAID
        if (!"PAID".equals(invoice.getStatus())) {
            log.error("Order ID: {} has status {}, must be PAID", orderId, invoice.getStatus());
            throw new AppException(ErrorCode.ORDER_NOT_FOUND, "Order must be paid to generate invoice.");
        }

        // Sử dụng paymentDate trực tiếp, không chuyển đổi múi giờ
        String formattedPaymentTime;
        if (invoice.getPaymentDate() != null) {
            formattedPaymentTime = invoice.getPaymentDate().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            log.info("Using PaymentDate (UTC) formatted: {}", formattedPaymentTime);
        } else {
            log.warn("PaymentDate not found for order ID: {}, using current time as fallback", orderId);
            formattedPaymentTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }

        // Tạo PDF bằng iText
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(72, 72, 72, 72); // 1 inch margins

        // Tiêu đề hóa đơn
        Paragraph title = new Paragraph("Invoice")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // Thông tin hóa đơn
        document.add(new Paragraph("Order ID: " + invoice.getOrderId()));
        document.add(new Paragraph("Date: " + formattedPaymentTime));
        document.add(new Paragraph("Table Number: " + invoice.getTableNumber()));
        document.add(new Paragraph("Customer Name: " + invoice.getCustomerName()));
        document.add(new Paragraph("Customer Email: " + invoice.getCustomerEmail()));
        document.add(new Paragraph("Total Amount: " + invoice.getAmount() + " VND"));
        document.add(new Paragraph("Payment Method: " + invoice.getPaymentMethod()));
        document.add(new Paragraph("Status: " + invoice.getStatus()));
        document.add(new Paragraph("Transaction ID: " + (invoice.getTransactionId() != null ? invoice.getTransactionId() : "N/A")));
        document.add(new Paragraph("\n"));

        // Bảng chi tiết món ăn
        Table table = new Table(new float[]{2, 1, 1, 1});
        table.addCell(new Cell().add(new Paragraph("Item Name")).setBold());
        table.addCell(new Cell().add(new Paragraph("Quantity")).setBold());
        table.addCell(new Cell().add(new Paragraph("Unit Price (VND)")).setBold());
        table.addCell(new Cell().add(new Paragraph("Total (VND)")).setBold());
        for (Map<String, Object> item : invoice.getOrderItems()) {
            table.addCell(new Cell().add(new Paragraph(item.get("itemName").toString())));
            table.addCell(new Cell().add(new Paragraph(item.get("quantity").toString())));
            table.addCell(new Cell().add(new Paragraph(((BigDecimal) item.get("price")).toString())));
            table.addCell(new Cell().add(new Paragraph(((BigDecimal) item.get("total")).toString())));
        }
        document.add(table);

        // Chú thích
        document.add(new Paragraph("\nThank you for your order!"));
        document.close();

        // Đọc file PDF thành ByteArrayResource
        byte[] pdfBytes = baos.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        // Xóa file tạm (không cần vì dùng iText trực tiếp)
        // (Bỏ qua bước này)

        // Trả về response với file PDF
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=invoice_" + orderId + ".pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfBytes.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}