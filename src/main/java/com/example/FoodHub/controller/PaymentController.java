package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.PayOSRequest;
import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.InvoiceResponse;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.dto.response.RevenueStatsResponseForCashier;
import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.enums.PaymentStatus;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.PaymentMapper;
import com.example.FoodHub.repository.PaymentRepository;
import com.example.FoodHub.service.EmailService;
import com.example.FoodHub.service.PaymentService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/cashier")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;
    EmailService emailService;
PaymentRepository   paymentRepository;  
    PaymentMapper paymentMapper;

//
//    @PostMapping("/payment")
//    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
//        PaymentResponse response = paymentService.createPayment(request);
//        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
//                .result(response)
//                .build();
//        return ResponseEntity.ok().body(apiResponse);
//    }   cái này của khứa khang có phải không?
    


    @GetMapping("/check-new-orders")
    public ApiResponse<List<PaymentResponse>> checkNewOrders() {
        try {
            // Kiểm tra đơn hàng mới trong 30 giây qua
            Instant thirtySecondsAgo = Instant.now().minusSeconds(30);
            System.out.println("Checking new orders after: " + thirtySecondsAgo);

            // Tìm đơn hàng với trạng thái PENDING
            List<Payment> payments = paymentRepository.findByStatusAndCreatedAtAfter("PENDING", thirtySecondsAgo);
            System.out.println("Found " + payments.size() + " new orders: " + payments);

            // Ánh xạ sang PaymentResponse
            List<PaymentResponse> result = payments.stream()
                    .map(paymentMapper::toPaymentResponse)
                    .toList();
            System.out.println("Mapped result: " + result);

            return ApiResponse.<List<PaymentResponse>>builder()
                    .code(1000)
                    .message("Success")
                    .result(result)
                    .build();
        } catch (Exception e) {
            System.out.println("Error checking new orders: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.<List<PaymentResponse>>builder()
                    .code(9999)
                    .message("Error: " + e.getMessage())
                    .result(List.of())
                    .build();
        }
    }

    // Thanh toán đơn hàng
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }
//
//    @PutMapping("/callback")
//    public ResponseEntity<ApiResponse<PaymentResponse>> paymentCallback(@RequestBody PayOSRequest request) {
//        PaymentResponse response = paymentService.updatePayOSPaymentStatus(request);
//
//        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
//                .result(response)
//                .build();
//        return ResponseEntity.ok(apiResponse);
//    }

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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        List<PaymentResponse> transactions = paymentService.getTransactionsByDate(start, end);
        ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder()
                .result(transactions)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    // Lấy giao dịch hôm nay
    @GetMapping("/todays-transactions")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getTodaysTransactions() {
        log.info("Fetching today's transactions at /cashier/todays-transactions");
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Instant start = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            Instant end = now.toLocalDate().plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).minusSeconds(1).toInstant();
            List<PaymentResponse> transactions = paymentService.getTransactionsByDate(start, end);
            log.info("Found {} transactions for today", transactions.size());
            ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder()
                    .code(0)
                    .result(transactions)
                    .build();
            return ResponseEntity.ok().body(apiResponse);
        } catch (Exception e) {
            log.error("Error fetching today's transactions: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to fetch today's transactions");
        }
    }

    // Lấy thống kê doanh thu hôm nay
    @GetMapping("/todays-revenue-stats")
    public ResponseEntity<ApiResponse<RevenueStatsResponseForCashier>> getTodaysRevenueStats() {
        log.info("Fetching today's revenue stats at /cashier/todays-revenue-stats");
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Instant date = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            RevenueStatsResponseForCashier stats = paymentService.getRevenueStatsByDate(date);
            log.info("Revenue stats retrieved: total={}", stats.getTotalRevenue());
            ApiResponse<RevenueStatsResponseForCashier> apiResponse = ApiResponse.<RevenueStatsResponseForCashier>builder()
                    .code(0)
                    .result(stats)
                    .build();
            return ResponseEntity.ok().body(apiResponse);
        } catch (Exception e) {
            log.error("Error fetching revenue stats: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to fetch revenue stats");
        }
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


    @GetMapping("/invoice/{orderId}/pdf")
    public ResponseEntity<ByteArrayResource> generateInvoicePdf(@PathVariable Integer orderId) throws IOException {
        System.out.println("Generating PDF invoice for order ID: " + orderId);

        // Lấy dữ liệu hóa đơn
        ApiResponse<InvoiceResponse> apiResponse = getInvoice(orderId).getBody();
        InvoiceResponse invoice = apiResponse.getResult();

        // Kiểm tra trạng thái PAID
        if (!"PAID".equals(invoice.getStatus())) {
            System.out.println("Order ID " + orderId + " is not paid, cannot generate invoice");
            throw new AppException(ErrorCode.ORDER_NOT_FOUND, "Order must be paid to generate invoice.");
        }

        // Tạo PDF với khổ giấy nhỏ (80mm x 150mm)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(new PageSize(226, 425)); // 80mm x 150mm
        pdf.getDocumentInfo().setTitle("Invoice - FoodHub");

        Document document = new Document(pdf);
        document.setMargins(2, 2, 2, 2); // Giảm lề tối đa để vừa khít

        // Màu cam chủ đạo
        DeviceRgb ORANGE_COLOR = new DeviceRgb(255, 98, 0); // #FF6200

        // Tạo một bảng duy nhất cho toàn bộ nội dung
        float[] columnWidths = {226}; // Một cột duy nhất chiếm toàn bộ chiều rộng
        Table table = new Table(UnitValue.createPointArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setBorder(null); // Tắt viền mặc định của bảng chính

        // Tiêu đề nhà hàng
        table.addCell(new Cell().add(new Paragraph(new Text("FoodHub Restaurant").setFontSize(10).setBold().setFontColor(ORANGE_COLOR))
                .setTextAlignment(TextAlignment.CENTER)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("123 Culinary Road, Ho Chi Minh City")
                .setFontSize(6)
                .setTextAlignment(TextAlignment.CENTER)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Hotline: 0123 456 789")
                .setFontSize(6)
                .setTextAlignment(TextAlignment.CENTER)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Email: contact@foodhub.vn")
                .setFontSize(6)
                .setTextAlignment(TextAlignment.CENTER)).setBorder(null));

        // Tiêu đề hóa đơn
        table.addCell(new Cell().add(new Paragraph(new Text("INVOICE").setFontSize(8).setBold().setFontColor(ORANGE_COLOR))
                .setTextAlignment(TextAlignment.CENTER)).setBorder(null));

        // Thông tin hóa đơn
        String paymentTime = invoice.getFormattedPaymentDate() != null ? invoice.getFormattedPaymentDate() : "N/A";
        table.addCell(new Cell().add(new Paragraph("Invoice No: " + invoice.getOrderId()).setFontSize(6)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Date: " + paymentTime).setFontSize(6)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Table: " + (invoice.getTableNumber() != null ? invoice.getTableNumber() : "N/A")).setFontSize(6)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Customer: " + (invoice.getCustomerName() != null ? invoice.getCustomerName() : "N/A")).setFontSize(6)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Email: " + (invoice.getCustomerEmail() != null ? invoice.getCustomerEmail() : "N/A")).setFontSize(6)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Payment Method: " + (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "N/A")).setFontSize(6)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Status: " + (invoice.getStatus() != null ? invoice.getStatus() : "N/A")).setFontSize(6)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Transaction ID: " + (invoice.getTransactionId() != null ? invoice.getTransactionId() : "N/A")).setFontSize(6)).setBorder(null));

        // Bảng chi tiết món ăn
        table.addCell(new Cell().add(new Paragraph("Items").setBold().setFontSize(6).setFontColor(ORANGE_COLOR)).setBorder(null));
        float[] itemColumnWidths = {110, 25, 45, 46}; // Tối ưu lại để vừa khít
        Table itemTable = new Table(UnitValue.createPointArray(itemColumnWidths));
        itemTable.setWidth(UnitValue.createPercentValue(100));
        itemTable.setBorder(new SolidBorder(ORANGE_COLOR, 0.5f)); // Chỉ giữ viền cam cho bảng items

        // Tiêu đề chi tiết
        itemTable.addHeaderCell(new Cell().add(new Paragraph("Item").setBold().setFontColor(ColorConstants.WHITE).setFontSize(5))
                .setBackgroundColor(ORANGE_COLOR));
        itemTable.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold().setFontColor(ColorConstants.WHITE).setFontSize(5))
                .setBackgroundColor(ORANGE_COLOR));
        itemTable.addHeaderCell(new Cell().add(new Paragraph("Price").setBold().setFontColor(ColorConstants.WHITE).setFontSize(5))
                .setBackgroundColor(ORANGE_COLOR));
        itemTable.addHeaderCell(new Cell().add(new Paragraph("Total").setBold().setFontColor(ColorConstants.WHITE).setFontSize(5))
                .setBackgroundColor(ORANGE_COLOR));

        // Dữ liệu món ăn (hiển thị tất cả món)
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Map<String, Object>> orderItems = invoice.getOrderItems();
        for (Map<String, Object> item : orderItems) {
            String itemName = (String) item.get("itemName");
            Integer quantity = (Integer) item.get("quantity");
            BigDecimal price = (BigDecimal) item.get("price");
            BigDecimal total = price.multiply(new BigDecimal(quantity));

            // Loại bỏ ký tự đặc biệt như ___ nếu có trong itemName
            itemName = itemName != null ? itemName.replaceAll("[_\\-]", "") : "N/A";

            itemTable.addCell(new Cell().add(new Paragraph(itemName).setFontSize(5)));
            itemTable.addCell(new Cell().add(new Paragraph(quantity != null ? quantity.toString() : "0").setFontSize(5)));
            itemTable.addCell(new Cell().add(new Paragraph(price != null ? price.toString() : "0").setFontSize(5)));
            itemTable.addCell(new Cell().add(new Paragraph(total.toString()).setFontSize(5)));

            totalAmount = totalAmount.add(total);
        }

        // Hàng tổng cộng
        itemTable.addCell(new Cell(1, 3).add(new Paragraph("Total").setBold().setFontSize(5).setTextAlignment(TextAlignment.RIGHT)));
        itemTable.addCell(new Cell().add(new Paragraph(totalAmount.toString() + " VND").setBold().setFontSize(5)));
        table.addCell(new Cell().add(itemTable));

        // Chân trang
        table.addCell(new Cell().add(new Paragraph("Thank you for choosing FoodHub!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(6)).setBorder(null));
        table.addCell(new Cell().add(new Paragraph("Please check your invoice before leaving.")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(5)).setBorder(null));

        document.add(table);

        // Đóng document
        document.close();

        // Trả về PDF
        byte[] pdfBytes = baos.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=invoice_" + orderId + ".pdf");
        System.out.println("Generated PDF for order ID: " + orderId + ", Size: " + pdfBytes.length + " bytes");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfBytes.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }


    @GetMapping("/search-transactions")
    public ApiResponse<List<PaymentResponse>> searchTransactions(@RequestParam("query") String query) {
        try {
            System.out.println("Search transactions with query: " + query);

            List<Payment> payments = new ArrayList<>();
            if (query.matches("\\d+")) {
                Integer orderId = Integer.parseInt(query);
                Optional<Payment> payment = paymentRepository.findByOrderId(orderId);
                payment.ifPresent(payments::add);
                System.out.println("Search by orderId: " + orderId + ", Found: " + payments);
            } else {
                payments = paymentRepository.findByTransactionIdContaining(query);
                System.out.println("Search by transactionId containing: " + query + ", Found: " + payments);
            }

            List<PaymentResponse> result = payments.stream()
                    .map(paymentMapper::toPaymentResponse)
                    .collect(Collectors.toList());
            System.out.println("Mapped search result: " + result);

            return ApiResponse.<List<PaymentResponse>>builder()
                    .code(1000)
                    .message("Success")
                    .result(result)
                    .build();
        } catch (Exception e) {
            System.out.println("Error searching transactions: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.<List<PaymentResponse>>builder()
                    .code(9999)
                    .message("Error: " + e.getMessage())
                    .result(List.of())
                    .build();
        }
    }

    @GetMapping("/suggestions")
    public ApiResponse<List<String>> getSuggestions(@RequestParam("query") String query) {
        try {
            System.out.println("Fetching suggestions for query: " + query);

            List<String> suggestions = new ArrayList<>();
            List<Payment> orderIdSuggestions = paymentRepository.findSuggestionsByOrderId(query);
            suggestions.addAll(orderIdSuggestions.stream()
                    .map(p -> p.getOrder().getId() + (p.getTransactionId() != null ? "-" + p.getTransactionId() : ""))
                    .collect(Collectors.toList()));

            if (!query.matches("\\d+")) {
                List<Payment> transactionIdSuggestions = paymentRepository.findSuggestionsByTransactionId(query);
                suggestions.addAll(transactionIdSuggestions.stream()
                        .map(p -> p.getOrder().getId() + (p.getTransactionId() != null ? "-" + p.getTransactionId() : ""))
                        .collect(Collectors.toList()));
            }

            suggestions = suggestions.stream()
                    .distinct()
                    .limit(5) // Giới hạn tối đa 5 gợi ý
                    .collect(Collectors.toList());
            System.out.println("Suggestions result: " + suggestions);

            return ApiResponse.<List<String>>builder()
                    .code(1000)
                    .message("Success")
                    .result(suggestions)
                    .build();
        } catch (Exception e) {
            System.out.println("Error fetching suggestions: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.<List<String>>builder()
                    .code(9999)
                    .message("Error: " + e.getMessage())
                    .result(List.of())
                    .build();
        }
    }


}