package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.CodigoVerificacion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.CodigoVerificacionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class VerificacionService {

    private final CodigoVerificacionRepository verificacionRepo;
    private final ICorreoService correoService; // ✅ siempre la interfaz
    private final Random random = new Random();

    // Tiempo válido del código: 10 minutos
    private static final Duration VALIDEZ = Duration.ofMinutes(10);

    // 👉 Spring usará este único constructor
    public VerificacionService(CodigoVerificacionRepository repo, ICorreoService correoService) {
        this.verificacionRepo = repo;
        this.correoService = correoService;
    }

    /**
     * Genera un código de 6 dígitos, lo guarda en la base de datos y lo envía por correo.
     */
    public String generarCodigoPara(String usuario) {
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        CodigoVerificacion verificacion = new CodigoVerificacion(usuario, codigo);
        verificacionRepo.save(verificacion);

        if (usuario.contains("@")) {
            correoService.enviarCodigo(usuario, codigo); // ✅ nunca será null
        } else {
            System.out.println("Envío por SMS aún no implementado: " + usuario + " → Código: " + codigo);
        }

        return codigo;
    }

    public boolean verificarCodigo(String usuario, String codigoIngresado) {
        Optional<CodigoVerificacion> posibleCodigo =
                verificacionRepo.findByEmailAndCodigoAndVerificadoFalse(usuario, codigoIngresado);

        if (posibleCodigo.isEmpty()) {
            return false;
        }

        CodigoVerificacion codigo = posibleCodigo.get();
        if (codigo.getGeneradoEl().plus(VALIDEZ).isBefore(LocalDateTime.now())) {
            return false;
        }

        codigo.setVerificado(true);
        verificacionRepo.save(codigo);
        return true;
    }

    public boolean tieneCodigoPendiente(String usuario) {
        return verificacionRepo.findTopByEmailAndVerificadoFalseOrderByGeneradoElDesc(usuario)
                .filter(c -> c.getGeneradoEl().plus(VALIDEZ).isAfter(LocalDateTime.now()))
                .isPresent();
    }
}
