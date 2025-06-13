package com.example.FoodHub.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    JavaMailSender mailSender;

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
                ".header { background: linear-gradient(135deg, #ff6b6b, #ff8e8e); color: white; padding: 40px 20px; text-align: center; position: relative; }" +
                ".header::before { content: '🍽️'; font-size: 48px; display: block; margin-bottom: 15px; }" +
                ".header h1 { font-size: 28px; font-weight: 700; margin-bottom: 10px; text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2); }" +
                ".header p { font-size: 16px; opacity: 0.9; }" +
                ".content { padding: 40px 30px; }" +
                ".welcome-message { font-size: 18px; color: #2c3e50; margin-bottom: 30px; text-align: center; }" +
                ".credentials-box { background: linear-gradient(135deg, #f8f9fa, #e9ecef); border-left: 4px solid #ff6b6b; border-radius: 8px; padding: 25px; margin: 30px 0; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05); }" +
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
                ".cta-button { display: inline-block; background: linear-gradient(135deg, #ff6b6b, #ff8e8e); color: white; padding: 15px 30px; text-decoration: none; border-radius: 50px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3); transition: all 0.3s ease; }" +
                ".footer { background: #2c3e50; color: white; padding: 30px; text-align: center; }" +
                ".footer p { margin-bottom: 10px; }" +
                ".footer .signature { font-style: italic; margin-top: 20px; color: #bdc3c7; }" +
                ".divider { height: 2px; background: linear-gradient(90deg, #ff6b6b, #ff8e8e); margin: 30px 0; border-radius: 1px; }" +
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
                "<a href=\"#\" class=\"cta-button\">Bắt đầu khám phá FOODHUB</a>" +
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

}