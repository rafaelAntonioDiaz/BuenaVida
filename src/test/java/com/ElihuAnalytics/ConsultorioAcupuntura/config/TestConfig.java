package com.ElihuAnalytics.ConsultorioAcupuntura.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@TestConfiguration
public class TestConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        // Retorna un stub simple para que los tests no fallen
        return new JavaMailSenderImpl();
    }
}