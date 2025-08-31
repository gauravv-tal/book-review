package com.bookreview.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${email.enabled:false}")
    private boolean emailEnabled;

    @Value("${email.sender:no-reply@example.com}")
    private String sender;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSignupEmail(String to, String name) {
        if (!emailEnabled) {
            log.debug("Email disabled. Skipping signup email to {}", to);
            return;
        }
        String subject = "Welcome to Book Review!";
        String html = "<h1>Welcome, " + escape(name) + "!</h1>" +
                "<p>Your account (" + escape(to) + ") has been created successfully.</p>" +
                "<p>Happy reading and reviewing!</p>";
        String text = "Welcome, " + name + "!\nYour account (" + to + ") has been created successfully.\nHappy reading and reviewing!";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, html); // plain text and HTML
            mailSender.send(message);
            log.info("Sent signup email to {} via SMTP", to);
        } catch (Exception e) {
            log.warn("Failed to send signup email to {} via SMTP: {}", to, e.getMessage());
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
