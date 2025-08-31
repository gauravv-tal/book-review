package com.bookreview.controller;

import com.bookreview.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloWorldController {


    private final EmailService emailService;

    public HelloWorldController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/hello")
    public String hello() {

        return "Hello, World!";
    }


}
