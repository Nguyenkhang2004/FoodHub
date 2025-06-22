package com.example.FoodHub.service;

import com.example.FoodHub.dto.response.InvoiceResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.admin-email}")
    private String adminEmail;

    public void sendWelcomeEmail(String to, String username, String password) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Chào mừng bạn đến với FOODHUB!");

        String htmlContent = buildWelcomeEmailTemplate(username, password);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    @Async
    public void sendWelcomeEmailAsync(String to, String username, String password) {
        try {
            log.info("Bắt đầu gửi email chào mừng đến {} tại {}", to, Instant.now());
            sendWelcomeEmail(to, username, password);
            log.info("Kết thúc gửi email chào mừng đến {} tại {}", to, Instant.now());
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email chào mừng đến {}: {}", to, e.getMessage());
        }
    }

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Mã OTP xác thực tài khoản FOODHUB");

        String htmlContent = buildOtpEmailTemplate(otp);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    @Async
    public void sendOtpEmailAsync(String to, String otp) {
        try {
            log.info("Bắt đầu gửi email OTP đến {} tại {}", to, Instant.now());
            sendOtpEmail(to, otp);
            log.info("Kết thúc gửi email OTP đến {} tại {}", to, Instant.now());
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email OTP đến {}: {}", to, e.getMessage());
        }
    }

    public void sendFeedbackEmail(String customerEmail, String username, String message) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(adminEmail);
        helper.setFrom(customerEmail);
        helper.setSubject("Phản hồi từ khách hàng: " + username);
        String htmlContent = buildFeedbackEmailTemplate(username, customerEmail, message);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }

    @Async
    public void sendFeedbackEmailAsync(String customerEmail, String username, String message) {
        try {
            log.info("Bắt đầu gửi email phản hồi từ {} tại {}", customerEmail, Instant.now());
            sendFeedbackEmail(customerEmail, username, message);
            log.info("Kết thúc gửi email phản hồi từ {} tại {}", customerEmail, Instant.now());
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email phản hồi từ {}: {}", customerEmail, e.getMessage());
        }
    }

    private String buildWelcomeEmailTemplate(String username, String password) {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Chào mừng đến với FOODHUB</title>" +
                "</head>" +
                "<body style='margin:0;padding:0;background-color:#f4f4f9;font-family:Arial,Helvetica,sans-serif;color:#333;line-height:1.6'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f9;padding:20px'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1)'>" +
                "<tr><td style='background:linear-gradient(to right,#02052f,#D4A017);color:#ffffff;text-align:center;padding:40px'>" +
                "<span style='font-size:40px;display:block;margin-bottom:10px'>🍽️</span>" +
                "<h1 style='font-size:24px;font-weight:bold;margin:0'>Chào mừng đến với FOODHUB!</h1>" +
                "<p style='font-size:16px;margin-top:10px;opacity:0.9'>Cộng đồng ẩm thực tuyệt vời của bạn!</p>" +
                "</td></tr>" +
                "<tr><td style='padding:30px'>" +
                "<p style='font-size:16px;color:#333;text-align:center;margin-bottom:20px'>Cảm ơn bạn đã gia nhập FOODHUB! Chúng tôi rất vui khi bạn trở thành một phần của hành trình ẩm thực này.</p>" +
                "<div style='background-color:#f9fafb;border-left:4px solid #D4A017;border-radius:6px;padding:20px;margin:20px 0'>" +
                "<h3 style='font-size:18px;color:#333;font-weight:bold;text-align:center;margin-bottom:15px'>Thông tin tài khoản của bạn</h3>" +
                "<table width='100%' cellpadding='10' style='font-size:14px'>" +
                "<tr><td style='width:30%;font-weight:bold;color:#555'>Tên tài khoản:</td><td style='background:#f1f3f5;padding:8px;border-radius:4px'>" + username + "</td></tr>" +
                "<tr><td style='font-weight:bold;color:#555'>Mật khẩu:</td><td style='background:#f1f3f5;padding:8px;border-radius:4px'>" + password + "</td></tr>" +
                "</table>" +
                "</div>" +
                "<div style='background-color:#fef3c7;border:1px solid #fde68a;border-radius:6px;padding:15px;margin:20px 0'>" +
                "<p style='font-size:14px;color:#78350f;margin:0'>🔒 <strong>Lưu ý bảo mật:</strong> Vui lòng không chia sẻ thông tin đăng nhập với bất kỳ ai để đảm bảo an toàn tài khoản.</p>" +
                "</div>" +
                "<div style='text-align:center;margin:20px 0'>" +
                "<a href='http://localhost:8080/login.html' style='display:inline-block;background:linear-gradient(to right,#D4A017,#F7E6A3);color:#02052f;padding:12px 24px;text-decoration:none;border-radius:25px;font-weight:bold;font-size:14px'>Bắt đầu khám phá FOODHUB</a>" +
                "</div>" +
                "</td></tr>" +
                "<tr><td style='background-color:#02052f;color:#F7E6A3;text-align:center;padding:20px;font-size:12px'>" +
                "<p style='margin:0'>Cần hỗ trợ? Liên hệ qua email hoặc hotline.</p>" +
                "<p style='margin-top:10px;font-style:italic'>Trân trọng, Đội ngũ FOODHUB</p>" +
                "</td></tr>" +
                "</table>" +
                "</td></tr>" +
                "</table>" +
                "</body>" +
                "</html>";
    }

    private String buildOtpEmailTemplate(String otp) {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Xác thực tài khoản FOODHUB</title>" +
                "</head>" +
                "<body style='margin:0;padding:0;background-color:#f4f4f9;font-family:Arial,Helvetica,sans-serif;color:#333;line-height:1.6'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f9;padding:20px'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1)'>" +
                "<tr><td style='background:linear-gradient(to right,#02052f,#D4A017);color:#ffffff;text-align:center;padding:40px'>" +
                "<span style='font-size:40px;display:block;margin-bottom:10px'>🔑</span>" +
                "<h1 style='font-size:24px;font-weight:bold;margin:0'>Xác thực tài khoản FOODHUB</h1>" +
                "<p style='font-size:16px;margin-top:10px;opacity:0.9'>Mã OTP của bạn</p>" +
                "</td></tr>" +
                "<tr><td style='padding:30px'>" +
                "<p style='font-size:16px;color:#333;text-align:center;margin-bottom:20px'>Vui lòng sử dụng mã OTP dưới đây để xác thực tài khoản. Mã này có hiệu lực trong 5 phút.</p>" +
                "<div style='background-color:#f9fafb;border-left:4px solid #D4A017;border-radius:6px;padding:20px;margin:20px 0;text-align:center'>" +
                "<h3 style='font-size:18px;color:#333;font-weight:bold;margin-bottom:15px'>Mã OTP của bạn</h3>" +
                "<div style='font-family:monospace;font-size:28px;color:#02052f;background:#F7E6A3;padding:10px 20px;border-radius:6px;display:inline-block;letter-spacing:3px'>" + otp + "</div>" +
                "</div>" +
                "<div style='background-color:#fef3c7;border:1px solid #fde68a;border-radius:6px;padding:15px;margin:20px 0'>" +
                "<p style='font-size:14px;color:#78350f;margin:0'>🔒 <strong>Lưu ý bảo mật:</strong> Không chia sẻ mã OTP này với bất kỳ ai để đảm bảo an toàn tài khoản.</p>" +
                "</div>" +
                "<p style='font-size:14px;color:#6b7280;text-align:center'>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>" +
                "</td></tr>" +
                "<tr><td style='background-color:#02052f;color:#F7E6A3;text-align:center;padding:20px;font-size:12px'>" +
                "<p style='margin:0'>Cần hỗ trợ? Liên hệ qua email hoặc hotline.</p>" +
                "<p style='margin-top:10px;font-style:italic'>Trân trọng, Đội ngũ FOODHUB</p>" +
                "</td></tr>" +
                "</table>" +
                "</td></tr>" +
                "</table>" +
                "</body>" +
                "</html>";
    }

    public void sendInvoiceEmail(String to, InvoiceResponse invoiceResponse) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        log.info("Sending invoice email to: {}", to);

        helper.setTo(to);
        helper.setSubject("Hóa đơn thanh toán - Order #" + (invoiceResponse.getOrderId() != null ? invoiceResponse.getOrderId() : "N/A"));

        String htmlContent = buildInvoiceEmailTemplate(invoiceResponse);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    @Async
    public void sendInvoiceEmailAsync(String to, InvoiceResponse invoiceResponse) {
        try {
            log.info("Bắt đầu gửi email hóa đơn đến {} tại {}", to, Instant.now());
            sendInvoiceEmail(to, invoiceResponse);
            log.info("Kết thúc gửi email hóa đơn đến {} tại {}", to, Instant.now());
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email hóa đơn đến {}: {}", to, e.getMessage());
        }
    }

    private String buildInvoiceEmailTemplate(InvoiceResponse invoiceResponse) {
        DecimalFormat df = new DecimalFormat("#,###");
        String orderId = String.valueOf(invoiceResponse.getOrderId() != null ? invoiceResponse.getOrderId() : "N/A");
        ZonedDateTime nowInVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String formattedPaymentTime = nowInVietnam.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String tableNumber = invoiceResponse.getTableNumber() != null ? invoiceResponse.getTableNumber() : "N/A";
        String customerName = invoiceResponse.getCustomerName() != null ? invoiceResponse.getCustomerName() : "N/A";
        String amount = invoiceResponse.getAmount() != null ? df.format(invoiceResponse.getAmount()) + "₫" : "N/A";
        String paymentMethod = invoiceResponse.getPaymentMethod() != null ? invoiceResponse.getPaymentMethod() : "N/A";
        String status = invoiceResponse.getStatus() != null ? invoiceResponse.getStatus() : "N/A";
        String transactionId = invoiceResponse.getTransactionId() != null ? invoiceResponse.getTransactionId() : "N/A";

        List<Map<String, Object>> orderItems = invoiceResponse.getOrderItems() != null ? invoiceResponse.getOrderItems() : List.of();
        String itemsHtml = orderItems.stream()
                .map(item -> {
                    String itemName = String.valueOf(item.getOrDefault("itemName", "N/A"));
                    String quantity = String.valueOf(item.getOrDefault("quantity", "N/A"));
                    String price = item.get("price") != null ? df.format(((Number) item.get("price")).doubleValue()) + "₫" : "N/A";
                    String total = (item.get("price") != null && item.get("quantity") != null) ?
                            df.format(((Number) item.get("price")).doubleValue() * ((Number) item.get("quantity")).doubleValue()) + "₫" : "N/A";
                    return String.format(
                            "<tr style='border-bottom:1px solid #e5e7eb'>" +
                                    "<td style='padding:10px;color:#333'>%s</td>" +
                                    "<td style='padding:10px;color:#333;text-align:center'>%s</td>" +
                                    "<td style='padding:10px;color:#333;text-align:right'>%s</td>" +
                                    "<td style='padding:10px;color:#333;text-align:right'>%s</td>" +
                                    "</tr>",
                            itemName, quantity, price, total
                    );
                })
                .collect(Collectors.joining());

        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Hóa đơn từ FOODHUB</title>" +
                "</head>" +
                "<body style='margin:0;padding:0;background-color:#f4f4f9;font-family:Arial,Helvetica,sans-serif;color:#333;line-height:1.6'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f9;padding:20px'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1)'>" +
                "<tr><td style='background:linear-gradient(to right,#02052f,#D4A017);color:#ffffff;text-align:center;padding:40px'>" +
                "<h1 style='font-size:24px;font-weight:bold;margin:0'>Hóa đơn từ FOODHUB</h1>" +
                "<p style='font-size:16px;margin-top:10px;opacity:0.9'>Cảm ơn bạn đã lựa chọn chúng tôi!</p>" +
                "</td></tr>" +
                "<tr><td style='padding:30px'>" +
                "<div style='margin-bottom:20px'>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Số hóa đơn:</strong> #" + orderId + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Ngày thanh toán:</strong> " + formattedPaymentTime + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Bàn:</strong> " + tableNumber + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Khách hàng:</strong> " + customerName + "</p>" +
                "</div>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='border-collapse:collapse;margin:20px 0'>" +
                "<thead><tr style='background-color:#F7E6A3;color:#333'>" +
                "<th style='padding:10px;text-align:left;font-weight:bold'>Tên món ăn</th>" +
                "<th style='padding:10px;text-align:center;font-weight:bold'>Số lượng</th>" +
                "<th style='padding:10px;text-align:right;font-weight:bold'>Giá đơn vị</th>" +
                "<th style='padding:10px;text-align:right;font-weight:bold'>Tổng tiền</th>" +
                "</tr></thead>" +
                "<tbody>" + itemsHtml + "</tbody>" +
                "</table>" +
                "<div style='font-weight:bold;margin-top:20px'>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Tổng cộng:</strong> " + amount + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Phương thức:</strong> " + paymentMethod + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Trạng thái:</strong> " + status + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Mã giao dịch:</strong> " + transactionId + "</p>" +
                "</div>" +
                "</td></tr>" +
                "<tr><td style='background-color:#02052f;color:#F7E6A3;text-align:center;padding:20px;font-size:12px'>" +
                "<p style='margin:0'>FOODHUB - Chuyên món lẩu và nướng</p>" +
                "<p style='margin-top:10px;font-style:italic'>Trân trọng, Đội ngũ FOODHUB</p>" +
                "</td></tr>" +
                "</table>" +
                "</td></tr>" +
                "</table>" +
                "</body>" +
                "</html>";
    }

    private String buildFeedbackEmailTemplate(String username, String customerEmail, String message) {
        ZonedDateTime nowInVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String formattedTime = nowInVietnam.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Phản hồi từ khách hàng FOODHUB</title>" +
                "</head>" +
                "<body style='margin:0;padding:0;background-color:#f4f4f9;font-family:Arial,Helvetica,sans-serif;color:#333;line-height:1.6'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f9;padding:20px'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1)'>" +
                "<tr><td style='background:linear-gradient(to right,#02052f,#D4A017);color:#ffffff;text-align:center;padding:40px'>" +
                "<span style='font-size:40px;display:block;margin-bottom:10px'>📧</span>" +
                "<h1 style='font-size:24px;font-weight:bold;margin:0'>Phản hồi từ khách hàng FOODHUB</h1>" +
                "<p style='font-size:16px;margin-top:10px;opacity:0.9'>Ý kiến từ " + username + "</p>" +
                "</td></tr>" +
                "<tr><td style='padding:30px'>" +
                "<p style='font-size:16px;color:#333;text-align:center;margin-bottom:20px'>Chúng tôi nhận được phản hồi từ khách hàng vào lúc " + formattedTime + ".</p>" +
                "<div style='background-color:#f9fafb;border-left:4px solid #D4A017;border-radius:6px;padding:20px;margin:20px 0'>" +
                "<h3 style='font-size:18px;color:#333;font-weight:bold;margin-bottom:15px'>Thông tin khách hàng</h3>" +
                "<table width='100%' cellpadding='10' style='font-size:14px'>" +
                "<tr><td style='width:30%;font-weight:bold;color:#555'>Tên tài khoản:</td><td style='background:#f1f3f5;padding:8px;border-radius:4px'>" + username + "</td></tr>" +
                "<tr><td style='font-weight:bold;color:#555'>Email:</td><td style='background:#f1f3f5;padding:8px;border-radius:4px'>" + customerEmail + "</td></tr>" +
                "</table>" +
                "</div>" +
                "<div style='background-color:#f9fafb;border-left:4px solid #D4A017;border-radius:6px;padding:20px;margin:20px 0'>" +
                "<h3 style='font-size:18px;color:#333;font-weight:bold;margin-bottom:15px'>Nội dung phản hồi</h3>" +
                "<p style='font-size:14px;color:#333'>" + message.replace("\n", "<br>") + "</p>" +
                "</div>" +
                "<div style='background-color:#fef3c7;border:1px solid #fde68a;border-radius:6px;padding:15px;margin:20px 0'>" +
                "<p style='font-size:14px;color:#78350f;margin:0'>🔒 <strong>Lưu ý:</strong> Đây là phản hồi từ khách hàng. Vui lòng xem xét và trả lời sớm nhất có thể.</p>" +
                "</div>" +
                "</td></tr>" +
                "<tr><td style='background-color:#02052f;color:#F7E6A3;text-align:center;padding:20px;font-size:12px'>" +
                "<p style='margin:0'>FOODHUB - Chuyên món lẩu và nướng</p>" +
                "<p style='margin-top:10px;font-style:italic'>Trân trọng, Đội ngũ FOODHUB</p>" +
                "</td></tr>" +
                "</table>" +
                "</td></tr>" +
                "</table>" +
                "</body>" +
                "</html>";
    }
}