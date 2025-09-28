package com.ElihuAnalytics.ConsultorioAcupuntura.controlador;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionNativaService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final NotificacionNativaService notificacionNativaService;
    private final SesionService sesionService;

    public TestController(NotificacionNativaService notificacionNativaService,
                          SesionService sesionService) {
        this.notificacionNativaService = notificacionNativaService;
        this.sesionService = sesionService;
    }

    @GetMapping("/notificacion/{id}")
    public String testNotificacion(@PathVariable Long id) {
        Sesion sesion = sesionService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada con id: " + id));
        notificacionNativaService.enviarNotificacionNativaCita(sesion);
        return "✅ Notificación enviada para sesión " + id;
    }
}
