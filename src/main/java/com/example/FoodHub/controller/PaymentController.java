package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.PayOSRequest;
import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.InvoiceResponse;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.dto.response.RevenueStatsResponseForCashier;
import com.example.FoodHub.enums.PaymentStatus;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.service.EmailService;
import com.example.FoodHub.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;
    EmailService emailService;

    // Thanh toán đơn hàng
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }


    @PutMapping("/callback")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentCallback(@RequestBody PayOSRequest request) {
        PaymentResponse response = paymentService.updatePayOSPaymentStatus(request);

        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

// Hủy/Hoàn tiền đơn hàng
//    @PostMapping("/cancel-or-refund/{orderId}")
//    public ResponseEntity<ApiResponse<String>> cancelOrRefundOrder(@PathVariable Integer orderId) {
//        paymentService.cancelOrRefundOrder(orderId);
//        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
//                .result("Đơn hàng đã được hủy thành công")
//                .build();
//        return ResponseEntity.ok().body(apiResponse);
//    }

    // Lấy danh sách giao dịch theo ngày
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getTransactionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        List<PaymentResponse> transactions = paymentService.getTransactionsByDate(start, end);
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
        }
        RevenueStatsResponseForCashier stats = paymentService.getRevenueStatsByDateRange(start, end);
        ApiResponse<RevenueStatsResponseForCashier> apiResponse = ApiResponse.<RevenueStatsResponseForCashier>builder()
                .result(stats)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    // Lấy thông tin hóa đơn
    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable Integer orderId) {
        InvoiceResponse response = paymentService.getOrderDetails(orderId);
        ApiResponse<InvoiceResponse> apiResponse = ApiResponse.<InvoiceResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }


    // Gửi email hóa đơn
    @PostMapping("/send-invoice-email")
    public ResponseEntity<ApiResponse<String>> sendInvoiceEmail(@RequestBody Map<String, String> request) {
        String customerEmail = request.get("customerEmail");
        Integer orderId = Integer.parseInt(request.get("orderId"));
        InvoiceResponse invoiceResponse = paymentService.getOrderDetails(orderId);
        emailService.sendInvoiceEmailAsync(customerEmail, invoiceResponse);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .result("Email invoice sent successfully")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }


    // Tạo và trả về file PDF hóa đơn
    @GetMapping("/invoice/{orderId}/pdf")
    public ResponseEntity<ByteArrayResource> generateInvoicePdf(@PathVariable Integer orderId) throws IOException, InterruptedException {
        log.info("Generating PDF invoice for order ID: {}", orderId);

        // Lấy dữ liệu hóa đơn từ API response
        ApiResponse<InvoiceResponse> apiResponse = getInvoice(orderId).getBody();
        InvoiceResponse invoice = apiResponse.getResult();

        // Kiểm tra trạng thái PAID
        if (!"PAID".equals(invoice.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND, "Order must be paid to generate invoice.");
        }

        // Tạo nội dung LaTeX
        StringBuilder latexContent = new StringBuilder();
        latexContent.append("\\documentclass[a4paper,12pt]{article}\n");
        latexContent.append("\\usepackage[utf8]{vietnam}\n"); // Hỗ trợ tiếng Việt
        latexContent.append("\\usepackage{geometry}\n");
        latexContent.append("\\geometry{a4paper, margin=1in}\n");
        latexContent.append("\\usepackage{booktabs}\n"); // Tạo bảng đẹp
        latexContent.append("\\usepackage{fancyhdr}\n");
        latexContent.append("\\pagestyle{fancy}\n");
        latexContent.append("\\fancyhf{}\n");
        latexContent.append("\\lhead{FoodHub Restaurant}\n");
        latexContent.append("\\rhead{Invoice}\n");
        latexContent.append("\\cfoot{Page \\thepage}\n");
        latexContent.append("\\begin{document}\n");

        // Tiêu đề hóa đơn
        latexContent.append("\\begin{center}\n");
        latexContent.append("\\textbf{\\large Invoice}\n");
        latexContent.append("\\end{center}\n");
        latexContent.append("\\vspace{1cm}\n");

        // Thông tin hóa đơn
        latexContent.append("\\textbf{Order ID:} ").append(invoice.getOrderId()).append("\\\\\n");

        // Sử dụng ZonedDateTime và định dạng theo yêu cầu
        ZonedDateTime nowInVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String formattedPaymentTime = nowInVietnam.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        latexContent.append("\\textbf{Date:} ").append(formattedPaymentTime).append("\\\\\n");

        latexContent.append("\\textbf{Table Number:} ").append(invoice.getTableNumber()).append("\\\\\n");
        latexContent.append("\\textbf{Customer Name:} ").append(invoice.getCustomerName()).append("\\\\\n");
        latexContent.append("\\textbf{Customer Email:} ").append(invoice.getCustomerEmail()).append("\\\\\n");
        latexContent.append("\\textbf{Total Amount:} ").append(invoice.getAmount()).append(" VND\\\\\n");
        latexContent.append("\\textbf{Payment Method:} ").append(invoice.getPaymentMethod()).append("\\\\\n");
        latexContent.append("\\textbf{Status:} ").append(invoice.getStatus()).append("\\\\\n");
        latexContent.append("\\textbf{Transaction ID:} ").append(invoice.getTransactionId() != null ? invoice.getTransactionId() : "N/A").append("\\\\\n");
        latexContent.append("\\vspace{0.5cm}\n");

        // Bảng chi tiết món ăn
        latexContent.append("\\begin{tabular}{llcc}\n");
        latexContent.append("\\toprule\n");
        latexContent.append("Item Name & Quantity & Unit Price (VND) & Total (VND) \\\\\n");
        latexContent.append("\\midrule\n");
        for (Map<String, Object> item : invoice.getOrderItems()) {
            latexContent.append(item.get("itemName")).append(" & ")
                    .append(item.get("quantity")).append(" & ")
                    .append(((BigDecimal) item.get("price")).toString()).append(" & ")
                    .append(((BigDecimal) item.get("total")).toString()).append(" \\\\\n");
        }
        latexContent.append("\\bottomrule\n");
        latexContent.append("\\end{tabular}\n");

        latexContent.append("\\vspace{1cm}\n");
        latexContent.append("\\textbf{Thank you for your order!}\n");
        latexContent.append("\\end{document}\n");

        // Tạo file LaTeX
        File tempLatexFile = File.createTempFile("invoice_" + orderId, ".tex");
        try (FileWriter writer = new FileWriter(tempLatexFile)) {
            writer.write(latexContent.toString());
        } catch (IOException e) {
            log.error("Failed to write LaTeX file for order ID: {}, error: {}", orderId, e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to create LaTeX file");
        }

        // Biên dịch LaTeX sang PDF (giả định latexmk đã cài đặt)
        File tempPdfFile = new File(tempLatexFile.getParent(), "invoice_" + orderId + ".pdf");
        ProcessBuilder pb = new ProcessBuilder("latexmk", "-pdf", tempLatexFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor(); // Chờ quá trình hoàn tất
        if (exitCode != 0) {
            log.error("LaTeX compilation failed for order ID: {}, exit code: {}", orderId, exitCode);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to generate PDF, LaTeX compilation error");
        }

        // Đọc file PDF thành ByteArrayResource
        byte[] pdfBytes;
        try {
            pdfBytes = Files.readAllBytes(tempPdfFile.toPath());
        } catch (IOException e) {
            log.error("Failed to read PDF file for order ID: {}, error: {}", orderId, e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to read generated PDF file");
        }
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        // Xóa file tạm (chỉ xóa nếu không cần giữ lại)
        if (tempLatexFile.exists()) tempLatexFile.delete();
        if (tempPdfFile.exists()) tempPdfFile.delete();

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