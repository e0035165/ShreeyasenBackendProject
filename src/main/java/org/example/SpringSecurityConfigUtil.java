package org.example;


import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import org.entity.CustomUserDetails;
import org.entity.Role;
import org.services.CustomUserDetailsService;
import org.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.utilities.RsaService;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class SpringSecurityConfigUtil {
    @Autowired
    private CustomUserDetailsService service;

    @Autowired
    private RsaService rsa_service;

    @Autowired
    private RoleService roleService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("Password Encoder initialized");
        return new BCryptPasswordEncoder();
    }



    @Bean
    public AuthenticationProvider getAuthProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        CustomUserDetails adminDetail = new CustomUserDetails();
        adminDetail.setUsername("Admin");
        adminDetail.setPassword(encoder.encode("Admin@95"));
        adminDetail.setEmail("websitemaster591@gmail.com");
        adminDetail.setActivated(true);
        String jwt = rsa_service.jwtEncrypt(Map.of("username","Admin","password","Admin@95","email","websitemaster591@gmail.com"));

        List<Role> allRoles = roleService.getAllRoles();
        adminDetail.setRoles(allRoles);
        service.addUser(adminDetail);
        provider.setPasswordEncoder(encoder);
        provider.setUserDetailsService(service);
        System.out.println("Authorization Provider initialized");
        System.out.println("Bearer "+jwt);
        return provider;
    }



    @Bean
    @Primary
    public CorsConfigurationSource getCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5200","http://localhost:5400"
        ,"http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // Allowed headers
        configuration.setExposedHeaders(List.of("Authorization")); // Exposed headers
        configuration.setAllowCredentials(true); // Allow credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all paths
        return source;
    }



    @Value("${spring.mail.username}")
    private String adminEmail;

    @Value("${spring.mail.port}")
    private Integer adminPort;

    @Value("${spring.mail.password}")
    private String adminPassword;

    @Value("${mail.store.protocol}")
    private String mailStoreProtocol;

    @Value("${mail.imap.host}")
    private String mailImapsHost;

    @Value("${mail.imap.port}")
    private Integer mailImapsPort;

    private Session session;

    @Bean
    public JavaMailSenderImpl getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(adminPort);
        mailSender.setUsername(adminEmail);
        mailSender.setPassword(adminPassword);
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        return mailSender;
    }




    @Bean
    @Order(2)
    public Properties getReceivedProperties() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "");
        props.put("mail.imaps.host", mailImapsHost);
        props.put("mail.imaps.port", mailImapsPort);
        return props;
    }

}
