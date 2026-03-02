package com.trackmint.app.service;

import com.trackmint.app.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("TrackMint - Reset Your Password");

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = """
                    <div style="font-family: Arial, sans-serif;
                                max-width: 600px;
                                margin: 0 auto;">
                        <div style="background-color: #10B981;
                                    padding: 20px;
                                    text-align: center;">
                            <h1 style="color: white; margin: 0;">TrackMint</h1>
                        </div>
                        <div style="padding: 30px;
                                    background-color: #f9f9f9;">
                            <h2 style="color: #333;">Reset Your Password</h2>
                            <p style="color: #666;">
                                You requested to reset your password.
                                Click the button below to reset it.
                            </p>
                            <p style="color: #666;">
                                This link will expire in
                                <strong>15 minutes</strong>.
                            </p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s"
                                   style="background-color: #10B981;
                                          color: white;
                                          padding: 15px 30px;
                                          text-decoration: none;
                                          border-radius: 5px;
                                          font-size: 16px;">
                                    Reset Password
                                </a>
                            </div>
                            <p style="color: #999; font-size: 12px;">
                                If you did not request this
                                please ignore this email.
                                Your password will not be changed.
                            </p>
                        </div>
                        <div style="background-color: #333;
                                    padding: 15px;
                                    text-align: center;">
                            <p style="color: #999;
                                      margin: 0;
                                      font-size: 12px;">
                                © 2025 TrackMint. All rights reserved.
                            </p>
                        </div>
                    </div>
                    """.formatted(resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw AppException.emailSendFailed();
        }
    }
}