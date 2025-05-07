package com.bootcamp.ecommerce_rohit.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    @Autowired
    private  JavaMailSender mailSender;
    @Async
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
to="rohit.gupta1@tothenew.com";
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true for HTML content
        mailSender.send(message);
        System.out.println("Email sent successfully to " + to);
    }

}
