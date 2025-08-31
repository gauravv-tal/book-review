package com.bookreview.service;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        // Point Spring Mail to GreenMail's in-memory SMTP server
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.username=",
        "spring.mail.password=",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false",
        "spring.mail.test-connection=false",
        // Enable our email service and set a sender
        "email.enabled=true",
        "email.sender=test@local"
})
class EmailServiceIntegrationTest {

    // Spin up GreenMail SMTP for the duration of each test
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withPerMethodLifecycle(true);

    @Autowired
    private EmailService emailService;

    @Test
    void sendSignupEmail_sendsEmailOverSmtp() throws Exception {
        // When
        String to = "gaurav.vishwanath@gmail.com";
        emailService.sendSignupEmail(to, "Test User");

        // Then - wait for exactly one message (max 5 seconds)
        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();
        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received).hasSize(1);
        MimeMessage msg = received[0];
        assertThat(msg.getAllRecipients()[0].toString()).isEqualTo(to);
        assertThat(msg.getSubject()).contains("Welcome to Book Review");
    }
}
