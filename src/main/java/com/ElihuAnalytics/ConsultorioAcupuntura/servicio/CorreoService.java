package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.ICorreoService;

@Service
@Profile("!test") // 👉 este bean NO se carga cuando el perfil activo es "test"
public class CorreoService implements ICorreoService {

    private final JavaMailSender mailSender;

    public CorreoService(JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }
    @Override
    public void enviarCodigo(String destino, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destino);
        mensaje.setSubject("Código de verificación - Consultorio Acupuntura");
        mensaje.setText("Tu código de verificación es: " + codigo + "\n\nEste código es válido por 10 minutos.");

        mailSender.send(mensaje);
        System.out.println("✅ Código enviado a: " + destino);

    }
}
