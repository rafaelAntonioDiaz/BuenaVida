package com.ElihuAnalytics.ConsultorioAcupuntura.config;


import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class PruebaSMTP {
    public static void main(String[] args) {
        // DATOS "DUROS" (Escríbelos aquí para la prueba)
        String username = "f169ad9121a9f4";
        String password = "6482da0d794573";

        System.out.println("--- INICIANDO PRUEBA DE CONEXIÓN SMTP PURA ---");

        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        prop.put("mail.smtp.port", "2525"); // Probemos puerto 2525 (a veces 587 falla)
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("test@tuconsultorio.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("tu_correo@gmail.com"));
            message.setSubject("Prueba Manual SMTP");
            message.setText("Si lees esto, las credenciales ESTÁN BIEN y el problema es Spring Boot.");

            System.out.println("Intentando conectar y enviar...");
            Transport.send(message);

            System.out.println("✅ ¡ÉXITO TOTAL! El correo se envió. El problema está en la configuración de Spring.");
        } catch (AuthenticationFailedException e) {
            System.out.println("❌ ERROR 401 REAL: El servidor rechazó usuario/clave explícitamente.");
            e.printStackTrace();
        } catch (MessagingException e) {
            System.out.println("❌ ERROR DE CONEXIÓN: Algo bloquea la salida.");
            e.printStackTrace();
        }
    }
}