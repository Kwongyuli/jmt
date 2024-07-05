package com.example.jmt.util;

import java.util.Properties;

import org.springframework.stereotype.Component;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

// 내부에서 쓰기 위함 (내부 관계자용)
@Component
public class Mailer {
    public void sendMail(
            String to, String subject, String content, SMTPAuthenticator smtp) {

        Properties properties  = new Properties();
        properties .put("mail.smtp.host", "smtp.gmail.com");
        properties .put("mail.smtp.port", "465");
        properties .put("mail.smtp.starttls.enable", "true");
        properties .put("mail.smtp.auth", "true");
        properties .put("mail.smtp.debug", "true");
        properties .put("mail.smtp.socketFactory.port", "465");
        properties .put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties .put("mail.smtp.socketFactory.fallback", "false");

        try {
            Session session = Session.getInstance(properties, smtp);
            session.setDebug(true);
            MimeMessage message = new MimeMessage(session); // 메일의 내용을 담을 객체
            message.setSubject(subject); // 제목
            Address fromAddress = new InternetAddress("msllsese@gmail.com");
            message.setFrom(fromAddress); // 보내는 사람
            Address toAddress = new InternetAddress(to);
            message.addRecipient(Message.RecipientType.TO, toAddress); // 받는 사람
            message.setContent(content, "text/html;charset=UTF-8"); // 내용과 인코딩
            Transport.send(message); // 전송
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
