package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Componente principal para agendar y reprogramar citas.
 */
public class AgendaCard extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AgendaCard.class);

    private final Paciente paciente;
    private final SesionService sesionService;
    private final NotificacionService notificacionService;
    private final PacienteRepository pacienteRepository;
    private final AgendaForm agendaForm;
    private final CalendarioMes calendarioMes;
    private final CitasCombo citasCombo;

    @Autowired
    public AgendaCard(Paciente paciente, SesionService sesionService, NotificacionService notificacionService,
                      PacienteRepository pacienteRepository) {
        this.sesionService = sesionService;
        this.notificacionService = notificacionService;
        this.pacienteRepository = pacienteRepository;

        if (paciente == null || paciente.getId() == null) {
            log.error("Paciente inválido recibido: id={}, username={}",
                    paciente != null ? paciente.getId() : "null",
                    paciente != null ? paciente.getUsername() : "null");
            throw new IllegalStateException("Paciente inválido: ID nulo o paciente no proporcionado");
        }
        Optional<Paciente> pacienteVerificado = pacienteRepository.findById(paciente.getId());
        if (!pacienteVerificado.isPresent()) {
            log.error("Paciente no encontrado en la base de datos: id={}, username={}",
                    paciente.getId(), paciente.getUsername());
            throw new IllegalStateException("Paciente no registrado en la base de datos");
        }
        this.paciente = pacienteVerificado.get();
        log.info("Paciente inicializado en AgendaCard: id={}, username={}", this.paciente.getId(), this.paciente.getUsername());

        setWidth("400px");
        setPadding(true);
        setSpacing(true);

        H3 titulo = new H3("Agendar cita");

        agendaForm = new AgendaForm(paciente, sesionService, notificacionService, pacienteRepository);
        calendarioMes = new CalendarioMes(paciente.getId(), sesionService, agendaForm.getFechaHoraPicker());
        citasCombo = new CitasCombo(paciente.getId(), sesionService, notificacionService);

        // Registrar callback para actualizar calendario y citas tras agendamiento
        agendaForm.onAgendarSuccess(() -> {
            calendarioMes.recargarSesionesMes();
            citasCombo.actualizarCitas();
        });

        add(titulo, calendarioMes, agendaForm, citasCombo);
    }
}