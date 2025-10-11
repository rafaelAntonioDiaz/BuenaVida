package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Componente para el formulario de agendamiento de citas.
 */
public class AgendaForm extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AgendaForm.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final Duration DURACION_CITA = Duration.ofHours(1);

    private final Paciente paciente;
    private final SesionService sesionService;
    private final NotificacionService notificacionService;
    private final PacienteRepository pacienteRepository;

    private final DateTimePicker fechaHoraPicker;
    private final TextArea motivoField;
    private final TextField lugarField;
    private Runnable onAgendarSuccessCallback;

    public AgendaForm(Paciente paciente, SesionService sesionService, NotificacionService notificacionService,
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
        log.info("Paciente inicializado en AgendaForm: id={}, username={}", this.paciente.getId(), this.paciente.getUsername());

        setPadding(true);
        setSpacing(true);

        fechaHoraPicker = new DateTimePicker("Fecha y hora");
        fechaHoraPicker.setStep(Duration.ofMinutes(30));
        fechaHoraPicker.setMin(LocalDateTime.now());

        motivoField = new TextArea("Motivo");
        motivoField.setMaxLength(500);
        motivoField.setRequired(true);

        lugarField = new TextField("Lugar (opcional)");
        lugarField.setMaxLength(255);

        Button agendar = new Button("Agendar", e -> agendarCita());
        agendar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(fechaHoraPicker, motivoField, lugarField, agendar);
    }

    @Transactional
    public void agendarCita() {
        LocalDateTime inicio = fechaHoraPicker.getValue();
        String txtMotivo = Optional.ofNullable(motivoField.getValue()).map(String::trim).orElse("");
        String txtLugar = Optional.ofNullable(lugarField.getValue()).map(String::trim).orElse("");

        if (inicio == null) {
            mostrarNotificacion("Selecciona una fecha y hora.", NotificationVariant.LUMO_ERROR);
            return;
        }
        if (inicio.isBefore(LocalDateTime.now())) {
            mostrarNotificacion("La fecha debe ser futura.", NotificationVariant.LUMO_ERROR);
            return;
        }
        if (txtMotivo.isBlank()) {
            mostrarNotificacion("El motivo es obligatorio.", NotificationVariant.LUMO_ERROR);
            return;
        }
        Optional<Paciente> pacientePersistido = pacienteRepository.findById(paciente.getId());
        if (!pacientePersistido.isPresent()) {
            log.error("Paciente no encontrado al agendar: id={}, username={}", paciente.getId(), paciente.getUsername());
            mostrarNotificacion("Error: Paciente no registrado en la base de datos.", NotificationVariant.LUMO_ERROR);
            return;
        }
        Paciente pacienteManaged = pacientePersistido.get();

        if (!sesionService.estaDisponible(inicio, DURACION_CITA)) {
            mostrarNotificacion("El horario seleccionado ya está ocupado o hay un conflicto con el desplazamiento.",
                    NotificationVariant.LUMO_ERROR);
            return;
        }

        Sesion sesion = new Sesion();
        sesion.setFecha(inicio);
        sesion.setMotivo(txtMotivo);
        sesion.setEstado(Sesion.EstadoSesion.PROGRAMADA);
        sesion.setPaciente(pacienteManaged);
        sesion.setLugar(txtLugar.isBlank() ? null : txtLugar);
        sesion.setDuracion(DURACION_CITA);

        try {
            log.info("Guardando sesión: paciente_id={}, username={}, fecha={}, motivo={}",
                    pacienteManaged.getId(), pacienteManaged.getUsername(), inicio, txtMotivo);
            sesionService.guardarSesion(sesion);
            mostrarNotificacion("Sesión programada: " + inicio.format(FORMATO_FECHA) + " " + inicio.format(FORMATO_HORA),
                    NotificationVariant.LUMO_SUCCESS);
            notificacionService.enviarNotificacionProgramacionMedico(sesion);
            limpiarFormulario();
            fireEvent(new AgendarEvent(this, true));
            if (onAgendarSuccessCallback != null) {
                onAgendarSuccessCallback.run();
            }
        } catch (Exception ex) {
            log.error("Error al guardar la sesión: paciente_id={}, username={}, mensaje={}",
                    pacienteManaged.getId(), pacienteManaged.getUsername(), ex.getMessage(), ex);
            mostrarNotificacion("Error al guardar la sesión: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            throw ex;
        }
    }

    public void limpiarFormulario() {
        fechaHoraPicker.clear();
        motivoField.clear();
        lugarField.clear();
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variant) {
        Notification notification = new Notification(mensaje, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }

    public DateTimePicker getFechaHoraPicker() {
        return fechaHoraPicker;
    }

    public void onAgendarSuccess(Runnable callback) {
        this.onAgendarSuccessCallback = callback;
    }

    // Evento para notificar agendamiento exitoso
    public static class AgendarEvent extends ComponentEvent<AgendaForm> {
        public AgendarEvent(AgendaForm source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}