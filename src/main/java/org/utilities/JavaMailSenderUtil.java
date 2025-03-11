package org.utilities;


import com.paypal.api.payments.Image;
import com.paypal.api.payments.Invoice;
import jakarta.activation.FileDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

@Component
public class JavaMailSenderUtil {
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value(value = "${spring.mail.username}")
    private String from;


    private Session session;

    @PostConstruct
    public void init() {
        Properties properties = javaMailSender.getJavaMailProperties();
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(javaMailSender.getUsername(), javaMailSender.getPassword());
            }
        };
        session = Session.getInstance(properties,authenticator);
        System.out.println("Session authentication completed");
    }

//    @SneakyThrows
//    public void sendEmailForInvoice(String[] emails, Invoice invoice, Image img) throws MessagingException {
//        JavaMailSender sender = javaMailSender;
//
//        Message message = new MimeMessage(session);
//        message.setFrom(new InternetAddress(from));
//        InternetAddress[] toAddresses = (InternetAddress[]) Arrays.stream(emails).map(InternetAddress::new).toArray();
//        message.setRecipients(Message.RecipientType.TO,toAddresses);
//        message.setSubject("Invoice sending");
//        message.setSentDate(new Date(System.currentTimeMillis()));
//        MimeBodyPart multipartbody = new MimeBodyPart();
//        String htmlText = "<H1>Hello</H1><img src=\"cid:image\">";
//        multipartbody.setContent(htmlText,"text/html");
//        MimeBodyPart imgPart = new MimeBodyPart();
//        imgPart.
//    }
}
