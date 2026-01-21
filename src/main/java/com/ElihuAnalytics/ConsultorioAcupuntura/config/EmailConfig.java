package com.ElihuAnalytics.ConsultorioAcupuntura.config;

import jakarta.annotation.PostConstruct;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Bean(name = "miSenderPersonalizado") // Nombre único
    @Primary
    public JavaMailSender miSenderPersonalizado() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // -----------------------------------------------------------
        // CONFIGURACIÓN EXPLÍCITA (Copia exacta de tu script exitoso)
        // -----------------------------------------------------------
        mailSender.setHost("sandbox.smtp.mailtrap.io");
        mailSender.setPort(2525); // O 2525 si ese usaste en el script

        // Escribe aquí tus credenciales REALES (las que dieron éxito en el script)
        mailSender.setUsername("f169ad9121a9f4");
        mailSender.setPassword("6482da0d794573");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true"); // Veremos la conversación SMTP en logs

        // Forzamos compatibilidad (igual que en el script)
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.auth.mechanisms", "PLAIN");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("f169ad9121a9f4", "6482da0d794573");
            }
        });

        // Le inyectamos nuestra sesión manual al sender de Spring
        mailSender.setSession(session);
        return mailSender;
    }

    @PostConstruct
    public void logConfig() {
        System.out.println("============================================");
        System.out.println("🔧 EMAIL CONFIG CARGADO MANUALMENTE");
        System.out.println("🔧 Host: sandbox.smtp.mailtrap.io");
        System.out.println("🔧 Puerto: 2525");
        System.out.println("🔧 Auth: PLAIN / TLSv1.2");
        System.out.println("============================================");
    }
}