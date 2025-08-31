package com.bookreview.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {
    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setup() {
        mailSender = Mockito.mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);
    }

    private static void setField(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendSignupEmail_disabled_doesNotSend() {
        setField(emailService, "emailEnabled", false);
        emailService.sendSignupEmail("to@example.com", "Alice");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendSignupEmail_enabled_sends() {
        setField(emailService, "emailEnabled", true);
        setField(emailService, "sender", "no-reply@test.com");
        MimeMessage mime = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);

        emailService.sendSignupEmail("to@example.com", "Alice");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
