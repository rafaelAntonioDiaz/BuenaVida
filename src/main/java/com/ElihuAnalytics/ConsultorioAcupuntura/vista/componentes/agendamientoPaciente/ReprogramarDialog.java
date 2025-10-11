package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Componente para el diálogo de reprogramación de citas.
 */
public class ReprogramarDialog {

    private static final Logger log = LoggerFactory.getLogger(ReprogramarDialog.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final Duration DURACION_CITA = Duration.ofHours(1);

    private final SesionService sesionService;
    private final NotificacionService notificacionService;
    private final Dialog dialog;

    public ReprogramarDialog(SesionService sesionService, NotificacionService notificacionService) {
        this.sesionService = sesionService;
        this.notificacionService = notificacionService;
        this.dialog = new Dialog();
        this.dialog.setHeaderTitle("Reprogramar cita");
    }

    @Transactional
    public void open(Sesion sesion, Runnable onSuccess) {
        if (sesion == null) {
            mostrarNotificacion("Selecciona una cita para reprogramar.", NotificationVariant.LUMO_ERROR);
            return;
        }

        DateTimePicker nuevaFechaPicker = new DateTimePicker("Nueva fecha y hora");
        nuevaFechaPicker.setStep(Duration.ofMinutes(30));
        nuevaFechaPicker.setMin(LocalDateTime.now());
        nuevaFechaPicker.setValue(sesion.getFecha());

        Button guardar = new Button("Guardar", e -> {
            LocalDateTime nuevaFecha = nuevaFechaPicker.getValue();
            if (nuevaFecha == null) {
                mostrarNotificacion("Selecciona una fecha y hora.", NotificationVariant.LUMO_ERROR);
                return;
            }
            if (nuevaFecha.isBefore(LocalDateTime.now())) {
                mostrarNotificacion("La fecha debe ser futura.", NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                log.info("Reprogramando sesión: id={}, paciente_id={}, username={}, nuevaFecha={}",
                        sesion.getId(), sesion.getPaciente().getId(), sesion.getPaciente().getUsername(), nuevaFecha);
                Optional<Sesion> reprogramada = sesionService.reprogramarSesion(sesion.getId(), nuevaFecha, DURACION_CITA);
                if (reprogramada.isPresent()) {
                    mostrarNotificacion("Cita reprogramada: " +
                                    nuevaFecha.format(FORMATO_FECHA) + " " + nuevaFecha.format(FORMATO_HORA),
                            NotificationVariant.LUMO_SUCCESS);
                    notificacionService.enviarNotificacionProgramacionMedico(reprogramada.get());
                    onSuccess.run();
                    dialog.close();
                } else {
                    mostrarNotificacion("No se pudo reprogramar: horario ocupado o cita no encontrada.",
                            NotificationVariant.LUMO_ERROR);
                }
            } catch (IllegalStateException ex) {
                log.warn("No se pudo reprogramar: {}", ex.getMessage());
                mostrarNotificacion("No se pudo reprogramar: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                log.error("Error al reprogramar la cita: {}", ex.getMessage(), ex);
                mostrarNotificacion("Error al reprogramar la cita: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout botones = new HorizontalLayout(guardar, cancelar);
        dialog.removeAll();
        dialog.add(nuevaFechaPicker, botones);
        dialog.open();
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variant) {
        Notification notification = new Notification(mensaje, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}