package com.example.FoodHub.controller;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
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
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.*;

import com.itextpdf.layout.borders.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmailService emailService;

    // Thanh toán đơn hàng
    @PostMapping("/process")
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
    @GetMapping("/transactions/date")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getTransactionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        List<PaymentResponse> transactions = paymentService.getTransactionsByDate(start, end);
        ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder()
                .result(transactions)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    // Lấy danh sách giao dịch theo ngày và trạng thái
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(value = "status", required = false, defaultValue = "all") String status) {
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        List<PaymentResponse> transactions = paymentService.getTransactionsByDateAndStatus(start, end, status);
        ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder()
                .result(transactions)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }


    // Endpoint gợi ý tìm kiếm
    @GetMapping("/transactions/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getTransactionSuggestions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(value = "query", required = false) String query) {
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        List<String> suggestions = paymentService.getTransactionSuggestions(start, end, query);
        ApiResponse<List<String>> apiResponse = ApiResponse.<List<String>>builder()
                .result(suggestions)
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

    // Tạo và trả về file PDF hóa đơn với thiết kế chuyên nghiệp
    @GetMapping("/invoice/{orderId}/pdf")
    public ResponseEntity<ByteArrayResource> generateInvoicePdf(@PathVariable Integer orderId) throws IOException {
        log.info("Generating PDF invoice for order ID: {}", orderId);

        // Lấy dữ liệu hóa đơn từ API response
        ApiResponse<InvoiceResponse> apiResponse = getInvoice(orderId).getBody();
        if (apiResponse == null || apiResponse.getResult() == null) {
            log.error("Invoice data not found for order ID: {}", orderId);
            throw new AppException(ErrorCode.ORDER_NOT_FOUND, "Invoice data not found for order ID: " + orderId);
        }
        InvoiceResponse invoice = apiResponse.getResult();

        // Kiểm tra trạng thái PAID
        if (!"PAID".equals(invoice.getStatus())) {
            log.error("Order status is not PAID: {}", invoice.getStatus());
            throw new AppException(ErrorCode.ORDER_NOT_FOUND, "Order must be paid to generate invoice.");
        }

        // Tạo nội dung PDF bằng iText
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(40, 40, 40, 40);

        // Định nghĩa màu sắc dựa trên giao diện payment
        Color primaryColor = new DeviceRgb(255, 152, 0);      // #ff9800 (cam nhạt - tiêu đề)
        Color secondaryColor = new DeviceRgb(117, 117, 117);   // #757575 (xám - footer)
        Color accentColor = new DeviceRgb(173, 216, 230);      // #ADD8E6 (xanh nhạt - header bảng)
        Color lightGray = new DeviceRgb(244, 244, 244);        // #f4f4f4 (xám nhạt - nền)
        Color darkGray = new DeviceRgb(51, 51, 51);            // #333 (đen - font chính)

        // Định nghĩa font mặc định (không cố định IDENTITY_H)
        PdfFont font;
        PdfFont boldFont;
        try {
            // Sử dụng font mặc định của hệ thống
            font = PdfFontFactory.createFont();
            boldFont = PdfFontFactory.createFont();
        } catch (IOException e) {
            log.error("Failed to create default font, falling back to system font. Error: {}", e.getMessage());
            font = PdfFontFactory.createFont();
            boldFont = PdfFontFactory.createFont();
        }

        // Áp dụng font cho toàn bộ document
        document.setFont(font);

        // Header với logo và thông tin công ty
        Table headerTable = new Table(2);
        headerTable.setWidth(UnitValue.createPercentValue(100));

        // Cột trái - Logo/Tên công ty
        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.TOP);

        Paragraph companyName = new Paragraph("FoodHub")
                .setBold()
                .setFont(boldFont)
                .setFontSize(24)
                .setFontColor(primaryColor)
                .setMarginBottom(5);

        Paragraph companyInfo = new Paragraph("123 FoodHub Street\nDa Nang City, Vietnam\nPhone: +84 1900-1234\nEmail: info@foodhub.vn\nWebsite: www.foodhub.vn")
                .setFont(font)
                .setFontSize(10)
                .setFontColor(secondaryColor)
                .setMarginTop(0);

        logoCell.add(companyName);
        logoCell.add(companyInfo);

        // Cột phải - Thông tin hóa đơn
        Cell invoiceInfoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.TOP);

        Paragraph invoiceTitle = new Paragraph("HÓA ĐƠN THANH TOÁN")
                .setBold()
                .setFont(boldFont)
                .setFontSize(28)
                .setFontColor(primaryColor)
                .setMarginBottom(10);

        Table invoiceDetailsTable = new Table(1);
        invoiceDetailsTable.setWidth(UnitValue.createPercentValue(100));

        String[] invoiceDetails = {
                "Số hóa đơn: #" + String.format("%06d", invoice.getOrderId()),
                "Ngày thanh toán: " + (invoice.getFormattedPaymentDate() != null ? invoice.getFormattedPaymentDate() : "N/A"),
                "Trạng thái: " + invoice.getStatus()
        };

        for (String detail : invoiceDetails) {
            Cell detailCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(accentColor) // #ADD8E6 (xanh nhạt)
                    .setPadding(8)
                    .setMarginBottom(2);
            detailCell.add(new Paragraph(detail)
                    .setFont(font)
                    .setFontSize(11)
                    .setFontColor(darkGray)
                    .setBold());
            invoiceDetailsTable.addCell(detailCell);
        }

        invoiceInfoCell.add(invoiceTitle);
        invoiceInfoCell.add(invoiceDetailsTable);

        headerTable.addCell(logoCell);
        headerTable.addCell(invoiceInfoCell);
        document.add(headerTable);

        // Separator line
        document.add(new Paragraph(" ").setMarginTop(20).setMarginBottom(10));
        LineSeparator separator = new LineSeparator(new SolidLine(2f));
        separator.setStrokeColor(primaryColor); // #ff9800
        document.add(separator);
        document.add(new Paragraph(" ").setMarginTop(10).setMarginBottom(20));

        // Thông tin khách hàng và bàn
        Table customerTable = new Table(2);
        customerTable.setWidth(UnitValue.createPercentValue(100));

        // Thông tin khách hàng
        Cell customerInfoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.TOP);

        Paragraph billToLabel = new Paragraph("THÔNG TIN KHÁCH HÀNG:")
                .setBold()
                .setFont(boldFont)
                .setFontSize(12)
                .setFontColor(primaryColor)
                .setMarginBottom(8);

        Paragraph customerDetails = new Paragraph(
                invoice.getCustomerName() + "\n" +
                        invoice.getCustomerEmail() + "\n" +
                        "Số bàn: " + invoice.getTableNumber())
                .setFont(font)
                .setFontSize(11)
                .setFontColor(darkGray);

        customerInfoCell.add(billToLabel);
        customerInfoCell.add(customerDetails);

        // Thông tin thanh toán
        Cell paymentInfoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.TOP);

        Paragraph paymentLabel = new Paragraph("THÔNG TIN THANH TOÁN:")
                .setBold()
                .setFont(boldFont)
                .setFontSize(12)
                .setFontColor(primaryColor)
                .setMarginBottom(8);

        Paragraph paymentDetails = new Paragraph(
                "Phương thức: " + invoice.getPaymentMethod() + "\n" +
                        "Mã giao dịch: " + (invoice.getTransactionId() != null ? invoice.getTransactionId() : "N/A"))
                .setFont(font)
                .setFontSize(11)
                .setFontColor(darkGray);

        paymentInfoCell.add(paymentLabel);
        paymentInfoCell.add(paymentDetails);

        customerTable.addCell(customerInfoCell);
        customerTable.addCell(paymentInfoCell);
        document.add(customerTable);

        document.add(new Paragraph(" ").setMarginTop(20).setMarginBottom(20));

        // Bảng chi tiết món ăn với thiết kế đẹp
        Table itemsTable = new Table(new float[]{3, 1, 2, 2});
        itemsTable.setWidth(UnitValue.createPercentValue(100));

        // Header của bảng
        String[] headers = {"Tên món ăn", "Số lượng", "Đơn giá (VND)", "Tổng (VND)"};
        for (String header : headers) {
            Cell headerCell = new Cell()
                    .setBackgroundColor(new DeviceRgb(255, 235, 59)) // #ffeb3b (vàng nhạt)
                    .setFontColor(ColorConstants.BLACK)
                    .setBold()
                    .setFont(boldFont)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(12);
            headerCell.add(new Paragraph(header).setFontSize(11));
            itemsTable.addHeaderCell(headerCell);
        }

        // Dữ liệu bảng
        BigDecimal subtotal = BigDecimal.ZERO;
        boolean isEvenRow = false;

        for (Map<String, Object> item : invoice.getOrderItems()) {
            Color rowColor = isEvenRow ? lightGray : ColorConstants.WHITE;

            // Item Name
            Cell nameCell = new Cell()
                    .setBackgroundColor(rowColor)
                    .setPadding(10)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
            nameCell.add(new Paragraph(item.get("itemName").toString())
                    .setFont(font)
                    .setFontSize(10)
                    .setFontColor(darkGray));
            itemsTable.addCell(nameCell);

            // Quantity
            Cell qtyCell = new Cell()
                    .setBackgroundColor(rowColor)
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
            qtyCell.add(new Paragraph(item.get("quantity").toString())
                    .setFont(font)
                    .setFontSize(10)
                    .setFontColor(darkGray));
            itemsTable.addCell(qtyCell);

            // Unit Price
            Cell priceCell = new Cell()
                    .setBackgroundColor(rowColor)
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
            BigDecimal unitPrice = (BigDecimal) item.get("price");
            priceCell.add(new Paragraph(String.format("%,.0f", unitPrice))
                    .setFont(font)
                    .setFontSize(10)
                    .setFontColor(darkGray));
            itemsTable.addCell(priceCell);

            // Total
            Cell totalCell = new Cell()
                    .setBackgroundColor(rowColor)
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
            BigDecimal itemTotal = (BigDecimal) item.get("total");
            subtotal = subtotal.add(itemTotal);
            totalCell.add(new Paragraph(String.format("%,.0f", itemTotal))
                    .setFont(font)
                    .setFontSize(10)
                    .setFontColor(darkGray)
                    .setBold());
            itemsTable.addCell(totalCell);

            isEvenRow = !isEvenRow;
        }

        document.add(itemsTable);

        // Tổng tiền
        Table totalTable = new Table(new float[]{3, 1});
        totalTable.setWidth(UnitValue.createPercentValue(100));
        totalTable.setMarginTop(20);

        // Empty cell
        Cell emptyCell = new Cell()
                .setBorder(Border.NO_BORDER);
        totalTable.addCell(emptyCell);

        // Total amount cell
        Cell totalAmountCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(primaryColor) // #ff9800
                .setPadding(15);

        Paragraph totalAmountText = new Paragraph("TỔNG CỘNG")
                .setBold()
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);

        Paragraph totalAmountValue = new Paragraph(String.format("%,.0f VND", invoice.getAmount()))
                .setBold()
                .setFont(boldFont)
                .setFontSize(18)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER);

        totalAmountCell.add(totalAmountText);
        totalAmountCell.add(totalAmountValue);
        totalTable.addCell(totalAmountCell);

        document.add(totalTable);

        // Footer
        document.add(new Paragraph(" ").setMarginTop(30));

        Paragraph thankYou = new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ của FoodHub!")
                .setBold()
                .setFont(boldFont)
                .setFontSize(16)
                .setFontColor(primaryColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);

        Paragraph footer = new Paragraph("Đây là hóa đơn tự động tạo, không cần chữ ký.")
                .setFont(font)
                .setFontSize(9)
                .setFontColor(secondaryColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();

        document.add(thankYou);
        document.add(footer);

        // Đóng tài liệu
        document.close();

        // Trả về response với file PDF
        byte[] pdfBytes = baos.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Disposition", "attachment; filename=invoice_" + orderId + ".pdf");
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(pdfBytes.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}