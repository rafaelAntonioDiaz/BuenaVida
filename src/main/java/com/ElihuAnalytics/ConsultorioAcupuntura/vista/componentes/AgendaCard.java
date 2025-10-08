package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AgendaCard extends VerticalLayout {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final Duration DURACION_CITA = Duration.ofHours(1); // Duración predeterminada: 1 hora

    private final Paciente paciente;
    private final SesionService sesionService;
    private final NotificacionService notificacionService;

    private DateTimePicker fechaHoraPicker;
    private TextArea motivoField;
    private TextField lugarField;
    private ComboBox<Sesion> citasCombo;

    public AgendaCard(Paciente paciente, SesionService sesionService, NotificacionService notificacionService) {
        this.paciente = paciente;
        this.sesionService = sesionService;
        this.notificacionService = notificacionService;

        setWidth("400px");
        setPadding(true);
        setSpacing(true);

        H3 titulo = new H3("Agendar cita");
        fechaHoraPicker = new DateTimePicker("Fecha y hora");
        fechaHoraPicker.setStep(Duration.ofMinutes(30));
        fechaHoraPicker.setMin(LocalDateTime.now());
        motivoField = new TextArea("Motivo");
        motivoField.setMaxLength(500);
        lugarField = new TextField("Lugar (opcional)");
        lugarField.setMaxLength(255);

        Button agendar = new Button("Agendar", e -> agendarCita());
        agendar.getElement().getThemeList().add("primary");

        citasCombo = new ComboBox<>("Citas programadas");
        citasCombo.setItemLabelGenerator(s -> s.getFecha().format(FORMATO_FECHA) + " " +
                s.getFecha().format(FORMATO_HORA) + " - " + s.getMotivo());
        citasCombo.setWidthFull();
        actualizarCitas();

        Button reprogramar = new Button("Reprogramar", e -> reprogramarCita());
        reprogramar.getElement().getThemeList().add("secondary");
        reprogramar.setEnabled(false);
        citasCombo.addValueChangeListener(e -> reprogramar.setEnabled(e.getValue() != null));

        HorizontalLayout botones = new HorizontalLayout(agendar, reprogramar);
        botones.setWidthFull();
        botones.setJustifyContentMode(JustifyContentMode.END);

        add(titulo, fechaHoraPicker, motivoField, lugarField, citasCombo, botones);
    }

    private void agendarCita() {
        LocalDateTime inicio = fechaHoraPicker.getValue();
        String txtMotivo = motivoField.getValue();
        String txtLugar = lugarField.getValue();

        if (inicio == null || inicio.isBefore(LocalDateTime.now())) {
            Notification.show("Selecciona una fecha futura válida.");
            return;
        }
        if (txtMotivo.isBlank()) {
            Notification.show("El motivo es obligatorio.");
            return;
        }

        if (!sesionService.estaDisponible(inicio, DURACION_CITA)) {
            Notification.show("El horario seleccionado no está disponible.");
            return;
        }

        Sesion sesion = new Sesion();
        sesion.setFecha(inicio);
        sesion.setMotivo(txtMotivo);
        sesion.setEstado(Sesion.EstadoSesion.PROGRAMADA);
        sesion.setPaciente(paciente);
        sesion.setLugar(txtLugar.isBlank() ? null : txtLugar);

        try {
            sesionService.guardarSesion(sesion);
            notificacionService.enviarNotificacionProgramacionMedico(sesion);
            Notification.show("Sesión programada y médico notificado: " +
                    inicio.format(FORMATO_FECHA) + " " + inicio.format(FORMATO_HORA));
            actualizarCitas();
            limpiarFormulario();
        } catch (Exception ex) {
            Notification.show("Sesión programada, pero no se pudo notificar al médico: " + ex.getMessage());
        }
    }

    private void reprogramarCita() {
        Sesion sesion = citasCombo.getValue();
        if (sesion == null) {
            Notification.show("Selecciona una cita para reprogramar.");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Reprogramar cita");

        DateTimePicker nuevaFechaPicker = new DateTimePicker("Nueva fecha y hora");
        nuevaFechaPicker.setStep(Duration.ofMinutes(30));
        nuevaFechaPicker.setMin(LocalDateTime.now());
        nuevaFechaPicker.setValue(sesion.getFecha());

        Button guardar = new Button("Guardar", e -> {
            LocalDateTime nuevaFecha = nuevaFechaPicker.getValue();
            if (nuevaFecha == null || nuevaFecha.isBefore(LocalDateTime.now())) {
                Notification.show("Selecciona una fecha futura válida.");
                return;
            }

            try {
                Optional<Sesion> reprogramada = sesionService.reprogramarSesion(sesion.getId(), nuevaFecha, DURACION_CITA);
                if (reprogramada.isPresent()) {
                    notificacionService.enviarNotificacionProgramacionMedico(reprogramada.get());
                    Notification.show("Cita reprogramada y médico notificado: " +
                            nuevaFecha.format(FORMATO_FECHA) + " " + nuevaFecha.format(FORMATO_HORA));
                    actualizarCitas();
                    dialog.close();
                } else {
                    Notification.show("No se pudo reprogramar la cita: no encontrada o no válida.");
                }
            } catch (Exception ex) {
                Notification.show("Error al reprogramar la cita: " + ex.getMessage());
            }
        });
        guardar.getElement().getThemeList().add("primary");

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.getElement().getThemeList().add("tertiary");

        HorizontalLayout botones = new HorizontalLayout(guardar, cancelar);
        dialog.add(nuevaFechaPicker, botones);
        dialog.open();
    }

    private void actualizarCitas() {
        LocalDate hoy = LocalDate.now();
        List<Sesion> citas = sesionService.obtenerSesionesPorPacienteYDia(paciente.getId(), hoy);
        citasCombo.setItems(citas.stream()
                .filter(s -> s.getEstado() == Sesion.EstadoSesion.PROGRAMADA || s.getEstado() == Sesion.EstadoSesion.CONFIRMADA)
                .sorted(Comparator.comparing(Sesion::getFecha))
                .collect(Collectors.toList()));
    }

    private void limpiarFormulario() {
        fechaHoraPicker.clear();
        motivoField.clear();
        lugarField.clear();
    }
}