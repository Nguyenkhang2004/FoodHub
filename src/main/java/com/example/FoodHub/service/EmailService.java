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
                "<tr><td style='background-color:#0F172B;color:#ffffff;text-align:center;padding:30px'>" +
                "<span style='font-size:40px;display:block;margin-bottom:10px'>🍽️</span>" +
                "<h1 style='font-size:22px;font-weight:bold;margin:0'>Chào mừng đến với FOODHUB!</h1>" +
                "<p style='font-size:14px;margin-top:10px;opacity:0.9'>Khám phá hành trình ẩm thực tuyệt vời</p>" +
                "</td></tr>" +
                "<tr><td style='padding:30px'>" +
                "<p style='font-size:16px;color:#333;text-align:left;margin-bottom:20px'>Xin chào <strong>" + username + "</strong>,<br>Cảm ơn bạn đã gia nhập FOODHUB! Chúng tôi rất vui khi bạn trở thành một phần của cộng đồng ẩm thực của chúng tôi.</p>" +
                "<div style='background-color:#f9fafb;border-left:4px solid #FEA116;border-radius:6px;padding:20px;margin:20px 0'>" +
                "<h3 style='font-size:18px;color:#333;font-weight:bold;margin-bottom:15px'>Thông tin tài khoản</h3>" +
                "<table width='100%' cellpadding='10' style='font-size:14px'>" +
                "<tr><td style='width:30%;font-weight:bold;color:#555'>Tên tài khoản:</td><td style='background:#f1f3f5;padding:8px;border-radius:4px'>" + username + "</td></tr>" +
                "<tr><td style='font-weight:bold;color:#555'>Mật khẩu:</td><td style='background:#f1f3f5;padding:8px;border-radius:4px'>" + password + "</td></tr>" +
                "</table>" +
                "</div>" +
                "<div style='background-color:#fef3c7;border:1px solid #FEA116;border-radius:6px;padding:15px;margin:20px 0'>" +
                "<p style='font-size:14px;color:#333;margin:0'>🔒 <strong>Lưu ý bảo mật:</strong> Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu và không chia sẻ thông tin đăng nhập với bất kỳ ai.</p>" +
                "</div>" +
                "<div style='text-align:center;margin:20px 0'>" +
                "<a href='http://localhost:8080/login.html' style='display:inline-block;background-color:#FEA116;color:#0F172B;padding:12px 24px;text-decoration:none;border-radius:25px;font-weight:bold;font-size:14px'>Đăng nhập ngay</a>" +
                "</div>" +
                "</td></tr>" +
                "<tr><td style='background-color:#0F172B;color:#ffffff;text-align:center;padding:20px;font-size:12px'>" +
                "<p style='margin:0'>Cần hỗ trợ? Liên hệ qua <a href='mailto:info@foodhub.com' style='color:#FEA116;text-decoration:underline'>info@foodhub.com</a> hoặc hotline (123) 456-7890.</p>" +
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
                "<tr><td style='background-color:#0F172B;color:#ffffff;text-align:center;padding:30px'>" +
                "<span style='font-size:40px;display:block;margin-bottom:10px'>🔑</span>" +
                "<h1 style='font-size:22px;font-weight:bold;margin:0'>Mã OTP xác thực</h1>" +
                "<p style='font-size:14px;margin-top:10px;opacity:0.9'>FOODHUB</p>" +
                "</td></tr>" +
                "<tr><td style='padding:30px'>" +
                "<p style='font-size:16px;color:#333;text-align:left;margin-bottom:20px'>Vui lòng sử dụng mã OTP dưới đây để xác thực tài khoản của bạn. Mã này có hiệu lực trong 5 phút.</p>" +
                "<div style='background-color:#f9fafb;border-left:4px solid #FEA116;border-radius:6px;padding:20px;margin:20px 0;text-align:center'>" +
                "<h3 style='font-size:18px;color:#333;font-weight:bold;margin-bottom:15px'>Mã OTP</h3>" +
                "<div style='font-family:monospace;font-size:28px;color:#0F172B;background:#FEA116;padding:10px 20px;border-radius:6px;display:inline-block;letter-spacing:3px'>" + otp + "</div>" +
                "</div>" +
                "<div style='background-color:#fef3c7;border:1px solid #FEA116;border-radius:6px;padding:15px;margin:20px 0'>" +
                "<p style='font-size:14px;color:#333;margin:0'>🔒 <strong>Lưu ý bảo mật:</strong> Không chia sẻ mã OTP này với bất kỳ ai để đảm bảo an toàn tài khoản.</p>" +
                "</div>" +
                "<p style='font-size:14px;color:#6b7280;text-align:left'>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này hoặc liên hệ hỗ trợ.</p>" +
                "</td></tr>" +
                "<tr><td style='background-color:#0F172B;color:#ffffff;text-align:center;padding:20px;font-size:12px'>" +
                "<p style='margin:0'>Cần hỗ trợ? Liên hệ qua <a href='mailto:info@foodhub.com' style='color:#FEA116;text-decoration:underline'>info@foodhub.com</a> hoặc hotline (123) 456-7890.</p>" +
                "<p style='margin-top:10px;font-style:italic'>Trân trọng, Đội ngũ FOODHUB</p>" +
                "</td></tr>" +
                "</table>" +
                "</td></tr>" +
                "</table>" +
                "</body>" +
                "</html>";
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
                "<tr><td style='background-color:#0F172B;color:#ffffff;text-align:center;padding:30px'>" +
                "<span style='font-size:40px;display:block;margin-bottom:10px'>🧾</span>" +
                "<h1 style='font-size:22px;font-weight:bold;margin:0'>Hóa đơn từ FOODHUB</h1>" +
                "<p style='font-size:14px;margin-top:10px;opacity:0.9'>Cảm ơn bạn đã đặt hàng!</p>" +
                "</td></tr>" +
                "<tr><td style='padding:30px'>" +
                "<div style='margin-bottom:20px'>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Số hóa đơn:</strong> #" + orderId + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Ngày thanh toán:</strong> " + formattedPaymentTime + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Bàn:</strong> " + tableNumber + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Khách hàng:</strong> " + customerName + "</p>" +
                "</div>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='border-collapse:collapse;margin:20px 0'>" +
                "<thead><tr style='background-color:#f9fafb;color:#333'>" +
                "<th style='padding:10px;text-align:left;font-weight:bold;border-bottom:2px solid #FEA116'>Tên món ăn</th>" +
                "<th style='padding:10px;text-align:center;font-weight:bold;border-bottom:2px solid #FEA116'>Số lượng</th>" +
                "<th style='padding:10px;text-align:right;font-weight:bold;border-bottom:2px solid #FEA116'>Giá đơn vị</th>" +
                "<th style='padding:10px;text-align:right;font-weight:bold;border-bottom:2px solid #FEA116'>Tổng tiền</th>" +
                "</tr></thead>" +
                "<tbody>" + itemsHtml + "</tbody>" +
                "</table>" +
                "<div style='font-weight:bold;margin-top:20px'>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Tổng cộng:</strong> " + amount + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Phương thức:</strong> " + paymentMethod + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Trạng thái:</strong> " + status + "</p>" +
                "<p style='font-size:14px;margin:5px 0'><strong>Mã giao dịch:</strong> " + transactionId + "</p>" +
                "</div>" +
                "<div style='text-align:center;margin:20px 0'>" +
                "<a href='http://localhost:8080/orderHistory.html' style='display:inline-block;background-color:#FEA116;color:#0F172B;padding:12px 24px;text-decoration:none;border-radius:25px;font-weight:bold;font-size:14px'>Xem lịch sử đơn hàng</a>" +
                "</div>" +
                "</td></tr>" +
                "<tr><td style='background-color:#0F172B;color:#ffffff;text-align:center;padding:20px;font-size:12px'>" +
                "<p style='margin:0'>FOODHUB - Chuyên món lẩu và nướng</p>" +
                "<p style='margin-top:10px;font-style:italic'>Liên hệ: <a href='mailto:info@foodhub.com' style='color:#FEA116;text-decoration:underline'>info@foodhub.com</a> | (123) 456-7890</p>" +
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
                "<tr><td style='background-color:#0F172B;color:#ffffff;text-align:center;padding:30px'>" +
                "<span style='font-size:40px;display:block;margin-bottom:10px'>📧</span>" +
                "<h1 style='font-size:22px;font-weight:bold;margin:0'>Phản hồi từ khách hàng</h1>" +
                "<p style='font-size:14px;margin-top:10px;opacity:0.9'>Từ " + username + "</p>" +
                "</td></tr>" +
                "<tr><td style='padding:30px'>" +
                "<p style='font-size:16px;color:#333;text-align:left;margin-bottom:20px'>Phản hồi được gửi vào lúc " + formattedTime + ".</p>" +
                "<div style='background-color:#f9fafb;border-left:4px solid #FEA116;border-radius:6px;padding:20px;margin:20px 0'>" +
                "<h3 style='font-size:18px;color:#333;font-weight:bold;margin-bottom:15px'>Thông tin khách hàng</h3>" +
                "<table width='100%' cellpadding='10' style='font-size:14px'>" +
                "<tr><td style='width:30%;font-weight:bold;color:#555'>Tên tài khoản:</td><td style='background:#f1f3f5;padding:8px;border-radius:4px'>" + username + "</td></tr>" +
                "<tr><td style='font-weight:bold;color:#555'>Email:</td><td style='background:#f1f3f5;padding:8px;border-radius:4px'>" + customerEmail + "</td></tr>" +
                "</table>" +
                "</div>" +
                "<div style='background-color:#f9fafb;border-left:4px solid #FEA116;border-radius:6px;padding:20px;margin:20px 0'>" +
                "<h3 style='font-size:18px;color:#333;font-weight:bold;margin-bottom:15px'>Nội dung phản hồi</h3>" +
                "<p style='font-size:14px;color:#333'>" + message.replace("\n", "<br>") + "</p>" +
                "</div>" +
                "<div style='background-color:#fef3c7;border:1px solid #FEA116;border-radius:6px;padding:15px;margin:20px 0'>" +
                "<p style='font-size:14px;color:#333;margin:0'>🔒 <strong>Lưu ý:</strong> Vui lòng xem xét và trả lời phản hồi này sớm nhất có thể.</p>" +
                "</div>" +
                "</td></tr>" +
                "<tr><td style='background-color:#0F172B;color:#ffffff;text-align:center;padding:20px;font-size:12px'>" +
                "<p style='margin:0'>FOODHUB - Chuyên món lẩu và nướng</p>" +
                "<p style='margin-top:10px;font-style:italic'>Liên hệ: <a href='mailto:info@foodhub.com' style='color:#FEA116;text-decoration:underline'>info@foodhub.com</a> | (123) 456-7890</p>" +
                "</td></tr>" +
                "</table>" +
                "</td></tr>" +
                "</table>" +
                "</body>" +
                "</html>";
    }
}