package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.select.Select; // Necesario
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate; // Necesario
import java.time.LocalDateTime;
import java.time.LocalTime; // Necesario
import java.time.format.DateTimeFormatter; // Necesario
import java.util.*;

import com.vaadin.flow.data.provider.ListDataProvider; // <-- Añadir este import
/**
 * Formulario para agendar una nueva cita.
 * Recibe la fecha de CalendarioMes y actualiza las horas disponibles.
 */
public class AgendaForm extends FormLayout {

    private static final Logger log = LoggerFactory.getLogger(AgendaForm.class);

    private final Paciente paciente;
    private final SesionService sesionService;
    private final NotificacionService notificacionService;
    // Mantenemos PacienteRepository por si el merge es necesario
    private final PacienteRepository pacienteRepository;

    // --- Campos UI ---
    // Ya NO tenemos DatePicker aquí
    private final Select<LocalTime> horaPicker = new Select<>();
    private final TextField lugar = new TextField("Lugar");
    private final TextArea motivo = new TextArea("Motivo");
    private final Button btnAgendar = new Button("Agendar Cita");

    // --- Estado interno ---
    private LocalDate fechaSeleccionada; // Guarda la fecha recibida de CalendarioMes

    private final Binder<Sesion> binder = new Binder<>(Sesion.class);
    private Runnable onAgendarSuccessCallback = () -> {};

    public AgendaForm(Paciente paciente, SesionService sesionService, NotificacionService notificacionService, PacienteRepository pacienteRepository) {
        this.paciente = paciente;
        this.sesionService = sesionService;
        this.notificacionService = notificacionService;
        this.pacienteRepository = pacienteRepository;

        //addClassName("agenda-form");

        configurarCampos();
        configurarBinder();
        configurarBotonAgendar();

        // El DatePicker ya no se añade aquí
        add(horaPicker, lugar, motivo, btnAgendar);
        setResponsiveSteps(new ResponsiveStep("0", 1));

        // Deshabilitar hora y botón inicialmente
        horaPicker.setEnabled(false);
        btnAgendar.setEnabled(false);
    }

    private void configurarCampos() {
        horaPicker.setLabel("Hora Disponible"); // Select usa setLabel
        horaPicker.setRequiredIndicatorVisible(true); // Indica que es requerido
        horaPicker.setPlaceholder("Seleccione una hora");
        horaPicker.setItemLabelGenerator(time -> { // Funciona igual
            if (time == null) return "";
            try { return time.format(DateTimeFormatter.ofPattern("hh:mm a", new Locale("es", "CO"))); }
            catch (Exception e) { return time.toString(); }
        });
        horaPicker.setItems(List.of()); // Items vacíos inicialmente
        horaPicker.addValueChangeListener(e -> {
            btnAgendar.setEnabled(fechaSeleccionada != null && e.getValue() != null);
        });

        lugar.setRequiredIndicatorVisible(true);
        lugar.setPlaceholder("Ej: Domicilio Calle X #Y-Z");

        motivo.setRequiredIndicatorVisible(true);
        motivo.setPlaceholder("Breve descripción del motivo de la consulta");
        motivo.setMinHeight("100px");

        btnAgendar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    private void configurarBinder() {
        binder.forField(lugar).asRequired("Lugar es requerido").bind(Sesion::getLugar, Sesion::setLugar);
        binder.forField(motivo).asRequired("Motivo es requerido").bind(Sesion::getMotivo, Sesion::setMotivo);
        binder.setBean(new Sesion());
    }

    private void configurarBotonAgendar() {
        btnAgendar.addClickListener(event -> guardarCita());
    }

    private void guardarCita() {
        // 1. Validar selecciones de fecha y hora (UI)
        LocalTime horaSeleccionada = horaPicker.getValue();
        if (fechaSeleccionada == null || horaSeleccionada == null) {
            Notification.show("Seleccione una fecha y hora válidas.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        // 2. Validar campos de texto (UI)
        String valorLugar = lugar.getValue();
        String valorMotivo = motivo.getValue();
        if (valorLugar == null || valorLugar.isBlank()) {
            lugar.setInvalid(true); // Marca el campo como inválido
            lugar.setErrorMessage("El lugar es obligatorio");
            Notification.show("Por favor, ingrese el lugar de la cita.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        } else {
            lugar.setInvalid(false); // Limpia error si ahora es válido
        }
        if (valorMotivo == null || valorMotivo.isBlank()) {
            motivo.setInvalid(true); // Marca el campo como inválido
            motivo.setErrorMessage("El motivo es obligatorio");
            Notification.show("Por favor, ingrese el motivo de la cita.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        } else {
            motivo.setInvalid(false); // Limpia error si ahora es válido
        }


        // --- INICIO DE LA CORRECCIÓN ---
        // 3. Crear y poblar el objeto Sesion MANUALMENTE
        Sesion nuevaSesion = new Sesion();
        try {
            LocalDateTime fechaHoraCita = LocalDateTime.of(fechaSeleccionada, horaSeleccionada);
            nuevaSesion.setFecha(fechaHoraCita);
            nuevaSesion.setPaciente(this.paciente);
            nuevaSesion.setDuracion(Duration.ofHours(1)); // O tu constante
            nuevaSesion.setLugar(valorLugar);     // Usar valor leído del campo
            nuevaSesion.setMotivo(valorMotivo);   // Usar valor leído del campo
            // El estado PROGRAMADA lo asigna el servicio

            log.info("Intentando guardar nueva sesión: Paciente ID={}, FechaHora={}, Lugar={}, Motivo={}",
                    nuevaSesion.getPaciente().getId(), nuevaSesion.getFecha(), nuevaSesion.getLugar(), nuevaSesion.getMotivo());

            // 4. Llamar al servicio para guardar
            sesionService.guardarSesion(nuevaSesion);

            // --- FIN DE LA CORRECCIÓN ---

            log.info("Sesión guardada exitosamente.");
            Notification.show("Cita programada con éxito. Recibirás una notificación.", 4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            limpiarFormulario();
            onAgendarSuccessCallback.run();

            // Eliminamos ValidationException ya que no usamos binder.writeBean
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("Error al guardar la sesión (ej. horario no disponible): {}", e.getMessage(), e);
            Notification.show("Error al guardar la cita: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            actualizarHorasDisponibles(sesionService.getHorasDisponibles(fechaSeleccionada));
        } catch (Exception e) {
            // Captura genérica para otros errores (ej. ConstraintViolationException si falla en el servicio)
            log.error("Error inesperado al guardar la sesión: {}", e.getMessage(), e);
            // Mostrar mensaje genérico o específico si puedes identificar ConstraintViolationException
            if (e.getCause() instanceof jakarta.validation.ConstraintViolationException) {
                Notification.show("Error de validación al guardar. Verifique los datos.", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                Notification.show("Ocurrió un error inesperado al agendar.", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
    /** Limpia el formulario y resetea el estado interno */
    private void limpiarFormulario() {
        binder.setBean(new Sesion()); // Limpia binder
        horaPicker.clear();
        lugar.clear();
        motivo.clear();
        fechaSeleccionada = null; // Resetea fecha
        actualizarHorasDisponibles(Collections.emptyList()); // Limpia y deshabilita hora
    }


    /**
     * Método público llamado por CalendarioMes para establecer la fecha seleccionada.
     */
    public void setFechaSeleccionada(LocalDate fecha) {
        this.fechaSeleccionada = fecha;
        log.debug("Fecha seleccionada en AgendaForm: {}", fecha);
        // Habilitar/deshabilitar botón Agendar basado en si hay HORA seleccionada
        btnAgendar.setEnabled(fecha != null && horaPicker.getValue() != null);
        // Podrías mostrar la fecha seleccionada en algún sitio si quieres
    }

    /**
     * Método público llamado por CalendarioMes para actualizar las horas disponibles.
     * CORREGIDO: Añade UI.push() para forzar la actualización.
     */
    /**
     * Método público llamado por CalendarioMes para actualizar las horas disponibles.
     * CORREGIDO: Simplificado, verifica estado y prueba toString().
     */
    /**
     * Método público llamado por CalendarioMes para actualizar las horas disponibles.
     * CORREGIDO: Eliminado UI.push(), simplificado label generator.
     */
    public void actualizarHorasDisponibles(List<LocalTime> horas) {
        // Asegurar lista no nula y ordenar
        List<LocalTime> horasParaMostrar = (horas != null) ? new ArrayList<>(horas) : Collections.emptyList();
        Collections.sort(horasParaMostrar);
        log.debug("AgendaForm.actualizarHorasDisponibles llamado con {} horas: {}", horasParaMostrar.size(), horasParaMostrar);

        LocalTime horaSeleccionadaAntes = horaPicker.getValue();

        // --- INICIO DE LA CORRECCIÓN ---

        // 1. Limpiar valor anterior
        horaPicker.clear();

        // 2. Establecer formateador ULTRA SIMPLE (temporal)
        horaPicker.setItemLabelGenerator(Object::toString); // Solo llama a toString()

        // 3. Establecer los items
        horaPicker.setItems(horasParaMostrar);

        // --- FIN DE LA CORRECCIÓN ---

        // Log después de setItems
        log.debug("ComboBox horaPicker items establecidos con {} horas. isEmpty()? -> {}", horasParaMostrar.size(), horaPicker.isEmpty());


        // Restaurar selección si aplica
        if (horaSeleccionadaAntes != null && horasParaMostrar.contains(horaSeleccionadaAntes)) {
            // Re-seleccionar SOLO si el valor es válido y está en la lista
            try {
                horaPicker.setValue(horaSeleccionadaAntes);
                log.debug("Valor anterior {} restaurado en ComboBox.", horaSeleccionadaAntes);
            } catch (IllegalArgumentException e) {
                // Esto puede pasar si setValue falla porque el item no se reconoce
                log.warn("No se pudo restaurar el valor anterior {}: {}", horaSeleccionadaAntes, e.getMessage());
                horaPicker.clear(); // Asegurar que quede limpio si falla la restauración
            }
        } else {
            log.debug("ComboBox valor no restaurado (no había valor anterior o no está en la nueva lista).");
        }

        // Habilitar/deshabilitar
        boolean hayHoras = !horasParaMostrar.isEmpty();
        horaPicker.setEnabled(hayHoras);
        horaPicker.setPlaceholder(hayHoras ? "Seleccione una hora" : (fechaSeleccionada == null ? "Seleccione fecha primero" : "No hay horas disponibles"));
        // El botón Agendar depende de si hay fecha Y hora seleccionada AHORA
        btnAgendar.setEnabled(fechaSeleccionada != null && horaPicker.getValue() != null);
        log.debug("ComboBox habilitado (estado final): {}, Botón Agendar habilitado: {}", horaPicker.isEnabled(), btnAgendar.isEnabled());

        // ELIMINAMOS UI.push()
    }    public void onAgendarSuccess(Runnable callback) {
        this.onAgendarSuccessCallback = callback;
    }

    // Ya no necesitamos getFechaHoraPicker()
}