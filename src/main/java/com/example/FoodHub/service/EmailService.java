package com.example.FoodHub.service;

import com.example.FoodHub.dto.response.InvoiceResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private String buildWelcomeEmailTemplate(String username, String password) {
        return "<!DOCTYPE html>" +
                "<html lang=\"vi\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Chào mừng đến với FOODHUB</title>" +
                "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa; color: #333; line-height: 1.6; }" +
                ".email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                ".header { background: linear-gradient(135deg, #02052f, #D4A017); color: white; padding: 40px 20px; text-align: center; position: relative; }" +
                ".header::before { content: '🍽️'; font-size: 48px; display: block; margin-bottom: 15px; }" +
                ".header h1 { font-size: 28px; font-weight: 700; margin-bottom: 10px; text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2); }" +
                ".header p { font-size: 16px; opacity: 0.9; }" +
                ".content { padding: 40px 30px; }" +
                ".welcome-message { font-size: 18px; color: #2c3e50; margin-bottom: 30px; text-align: center; }" +
                ".credentials-box { background: linear-gradient(135deg, #f8f9fa, #e9ecef); border-left: 4px solid #D4A017; border-radius: 8px; padding: 25px; margin: 30px 0; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05); }" +
                ".credentials-title { font-size: 20px; color: #2c3e50; margin-bottom: 20px; font-weight: 600; text-align: center; }" +
                ".credential-item { display: flex; align-items: center; margin-bottom: 15px; padding: 12px; background: white; border-radius: 6px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); }" +
                ".credential-item:last-child { margin-bottom: 0; }" +
                ".credential-icon { font-size: 20px; margin-right: 12px; width: 24px; text-align: center; }" +
                ".credential-label { font-weight: 600; color: #495057; margin-right: 10px; min-width: 120px; }" +
                ".credential-value { font-family: 'Courier New', monospace; background: #f8f9fa; padding: 8px 12px; border-radius: 4px; color: #2c3e50; font-weight: 500; border: 1px solid #e9ecef; }" +
                ".security-notice { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px; margin: 30px 0; position: relative; }" +
                ".security-notice::before { content: '🔒'; font-size: 24px; position: absolute; top: 20px; left: 20px; }" +
                ".security-notice p { margin-left: 40px; color: #856404; font-weight: 500; }" +
                ".cta-section { text-align: center; margin: 40px 0; }" +
                ".cta-button { display: inline-block; background: linear-gradient(135deg, #D4A017, #F7E6A3); color: #02052f; padding: 15px 30px; text-decoration: none; border-radius: 50px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 15px rgba(212, 160, 23, 0.3); transition: all 0.3s ease; }" +
                ".footer { background: #02052f; color: #F7E6A3; padding: 30px; text-align: center decyzji;" +
                ".footer p { margin-bottom: 10px; }" +
                ".footer .signature { font-style: italic; margin-top: 20px; color: #bdc3c7; }" +
                ".divider { height: 2px; background: linear-gradient(90deg, #D4A017, #F7E6A3); margin: 30px 0; border-radius: 1px; }" +
                "@media (max-width: 600px) { .email-container { margin: 0; border-radius: 0; } .content { padding: 30px 20px; } .header { padding: 30px 20px; } .credential-item { flex-direction: column; align-items: flex-start; } .credential-label { margin-bottom: 5px; min-width: auto; } }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"email-container\">" +
                "<div class=\"header\">" +
                "<h1>FOODHUB</h1>" +
                "<p>Chào mừng bạn đến với cộng đồng ẩm thực tuyệt vời!</p>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p class=\"welcome-message\">" +
                "Cảm ơn bạn đã tới với chúng tôi! Chúng tôi rất vui mừng khi bạn trở thành thành viên mới của FOODHUB." +
                "</p>" +
                "<div class=\"divider\"></div>" +
                "<div class=\"credentials-box\">" +
                "<h3 class=\"credentials-title\">Thông tin tài khoản của bạn</h3>" +
                "<div class=\"credential-item\">" +
                "<span class=\"credential-icon\">👤</span>" +
                "<span class=\"credential-label\">Tên tài khoản:</span>" +
                "<span class=\"credential-value\">" + username + "</span>" +
                "</div>" +
                "<div class=\"credential-item\">" +
                "<span class=\"credential-icon\">🔑</span>" +
                "<span class=\"credential-label\">Mật khẩu:</span>" +
                "<span class=\"credential-value\">" + password + "</span>" +
                "</div>" +
                "</div>" +
                "<div class=\"security-notice\">" +
                "<p><strong>Lưu ý bảo mật:</strong> Vui lòng không chia sẻ thông tin đăng nhập này với bất kỳ ai để đảm bảo an toàn tài khoản của bạn.</p>" +
                "</div>" +
                "<div class=\"cta-section\">" +
                "<a href=\"http://localhost:8080/login.html\" class=\"cta-button\">Bắt đầu khám phá FOODHUB</a>" +
                "</div>" +
                "<div class=\"divider\"></div>" +
                "<p style=\"text-align: center; color: #6c757d; font-size: 16px;\">" +
                "Chúc bạn có những trải nghiệm tuyệt vời cùng FOODHUB! 🌟" +
                "</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p><strong>Cần hỗ trợ?</strong></p>" +
                "<p>Liên hệ với chúng tôi qua email hoặc hotline để được giúp đỡ nhanh nhất.</p>" +
                "<p class=\"signature\">Trân trọng,<br><strong>Đội ngũ FOODHUB</strong></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildOtpEmailTemplate(String otp) {
        return "<!DOCTYPE html>" +
                "<html lang=\"vi\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Xác thực tài khoản FOODHUB</title>" +
                "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa; color: #333; line-height: 1.6; }" +
                ".email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1); overflow: hidden; }" +
                ".header { background: linear-gradient(135deg, #02052f, #D4A017); color: white; padding: 40px 20px; text-align: center; position: relative; }" +
                ".header::before { content: '🔑'; font-size: 48px; display: block; margin-bottom: 15px; }" +
                ".header h1 { font-size: 28px; font-weight: 700; margin-bottom: 10px; text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2); }" +
                ".header p { font-size: 16px; opacity: 0.9; }" +
                ".content { padding: 40px 30px; }" +
                ".otp-box { background: linear-gradient(135deg, #f8f9fa, #e9ecef); border-left: 4px solid #D4A017; border-radius: 8px; padding: 25px; margin: 30px 0; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05); text-align: center; }" +
                ".otp-code { font-family: 'Courier New', monospace; font-size: 32px; color: #2c3e50; background: #F7E6A3; padding: 15px 25px; border-radius: 8px; display: inline-block; margin: 20px 0; letter-spacing: 5px; }" +
                ".security-notice { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px; margin: 30px 0; position: relative; }" +
                ".security-notice::before { content: '🔒'; font-size: 24px; position: absolute; top: 20px; left: 20px; }" +
                ".security-notice p { margin-left: 40px; color: #856404; font-weight: 500; }" +
                ".footer { background: #02052f; color: #F7E6A3; padding: 30px; text-align: center; }" +
                ".footer p { margin-bottom: 10px; }" +
                ".footer .signature { font-style: italic; margin-top: 20px; color: #bdc3c7; }" +
                ".divider { height: 2px; background: linear-gradient(90deg, #D4A017, #F7E6A3); margin: 30px 0; border-radius: 1px; }" +
                "@media (max-width: 600px) { .email-container { margin: 0; border-radius: 0; } .content { padding: 30px 20px; } .header { padding: 30px 20px; } .otp-code { font-size: 24px; letter-spacing: 3px; } }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"email-container\">" +
                "<div class=\"header\">" +
                "<h1>FOODHUB</h1>" +
                "<p>Xác thực tài khoản của bạn</p>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p style=\"font-size: 18px; color: #2c3e50; margin-bottom: 30px; text-align: center;\">" +
                "Vui lòng sử dụng mã OTP dưới đây để xác thực tài khoản của bạn. Mã này có hiệu lực trong 5 phút." +
                "</p>" +
                "<div class=\"divider\"></div>" +
                "<div class=\"otp-box\">" +
                "<h3 style=\"font-size: 20px; color: #2c3e50; margin-bottom: 20px; font-weight: 600;\">Mã OTP của bạn</h3>" +
                "<div class=\"otp-code\">" + otp + "</div>" +
                "</div>" +
                "<div class=\"security-notice\">" +
                "<p><strong>Lưu ý bảo mật:</strong> Không chia sẻ mã OTP này với bất kỳ ai để đảm bảo an toàn tài khoản của bạn.</p>" +
                "</div>" +
                "<div class=\"divider\"></div>" +
                "<p style=\"text-align: center; color: #6c757d; font-size: 16px;\">" +
                "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này." +
                "</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p><strong>Cần hỗ trợ?</strong></p>" +
                "<p>Liên hệ với chúng tôi qua email hoặc hotline để được giúp đỡ nhanh nhất.</p>" +
                "<p class=\"signature\">Trân trọng,<br><strong>Đội ngũ FOODHUB</strong></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }



    //================================m====hóa đơn====================================================================


    // Sử dụng InvoiceResponse
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

// Lấy thời gian hiện tại theo múi giờ Việt Nam
        ZonedDateTime nowInVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String formattedPaymentTime = nowInVietnam.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String tableNumber = invoiceResponse.getTableNumber() != null ? invoiceResponse.getTableNumber() : "N/A";
        String customerName = invoiceResponse.getCustomerName() != null ? invoiceResponse.getCustomerName() : "N/A";
        String amount = invoiceResponse.getAmount() != null ?
                df.format(invoiceResponse.getAmount()) + "₫" : "N/A";
        String paymentMethod = invoiceResponse.getPaymentMethod() != null ? invoiceResponse.getPaymentMethod() : "N/A";
        String status = invoiceResponse.getStatus() != null ? invoiceResponse.getStatus() : "N/A";
        String transactionId = invoiceResponse.getTransactionId() != null ? invoiceResponse.getTransactionId() : "N/A";

        List<Map<String, Object>> orderItems = invoiceResponse.getOrderItems() != null ? invoiceResponse.getOrderItems() : List.of();
        String itemsHtml = orderItems.stream()
                .map(item -> {
                    String itemName = String.valueOf(item.getOrDefault("itemName", "N/A"));
                    String quantity = String.valueOf(item.getOrDefault("quantity", "N/A"));
                    String price = item.get("price") != null ?
                            df.format(((Number) item.get("price")).doubleValue()) + "₫" : "N/A";
                    String total = (item.get("price") != null && item.get("quantity") != null) ?
                            df.format(((Number) item.get("price")).doubleValue() * ((Number) item.get("quantity")).doubleValue()) + "₫" : "N/A";
                    return String.format(
                            "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                            itemName, quantity, price, total
                    );
                })
                .collect(Collectors.joining());

        return "<!DOCTYPE html>" +
                "<html lang=\"vi\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Hóa đơn từ FoodHub</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: #fff; padding: 20px; border: 1px solid #e0e0e0; }" +
                ".header { text-align: center; background-color: #ff9800; color: #fff; padding: 10px; }" +
                ".details p { margin: 5px 0; color: #333; }" +
                ".item-table { width: 100%; border-collapse: collapse; margin: 10px 0; }" +
                ".item-table th, .item-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }" +
                ".item-table th { background-color: #ffeb3b; color: #000; }" +
                ".total { font-weight: bold; margin-top: 10px; }" +
                ".footer { text-align: center; color: #757575; font-size: 12px; margin-top: 20px; }" +
                "@media (max-width: 600px) { .container { margin: 0; border-radius: 0; } .item-table { font-size: 14px; } }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\"><h3>Hóa đơn từ FoodHub</h3></div>" +
                "<div class=\"details\">" +
                "<p><strong>Số hóa đơn:</strong> #" + orderId + "</p>" +
                "<p><strong>Ngày thanh toán:</strong> " + formattedPaymentTime + "</p>" +
                "<p><strong>Bàn:</strong> " + tableNumber + "</p>" +
                "<p><strong>Khách hàng:</strong> " + customerName + "</p>" +
                "</div>" +
                "<table class=\"item-table\">" +
                "<thead><tr><th>Tên món ăn</th><th>Số lượng</th><th>Giá đơn vị</th><th>Tổng tiền</th></tr></thead>" +
                "<tbody>" + itemsHtml + "</tbody>" +
                "</table>" +
                "<div class=\"total\">" +
                "<p><strong>Tổng cộng:</strong> " + amount + "</p>" +
                "<p><strong>Phương thức:</strong> " + paymentMethod + "</p>" +
                "<p><strong>Trạng thái:</strong> " + status + "</p>" +
                "<p><strong>Mã giao dịch:</strong> " + transactionId + "</p>" +
                "</div>" +
                "<div class=\"footer\">FoodHub - Chuyên món lẩu và nướng<br>Cảm ơn quý khách!</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}