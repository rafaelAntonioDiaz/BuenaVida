package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.util.FestivosColombia;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Locale;

/**
 * Componente para agendar y reprogramar citas, con calendario visual y festivos.
 */
public class AgendaCard extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AgendaCard.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("es"));
    private static final Duration DURACION_CITA = Duration.ofHours(1);

    private final Paciente paciente;
    private final SesionService sesionService;
    private final NotificacionService notificacionService;
    private final PacienteRepository pacienteRepository;

    private DateTimePicker fechaHoraPicker;
    private TextArea motivoField;
    private TextField lugarField;
    private ComboBox<Sesion> citasCombo;
    private YearMonth mesMostrado = YearMonth.now();
    private final Map<LocalDate, Integer> sesionesPorDia = new HashMap<>();
    private Div calendarioContainer;
    private Span etiquetaMes;

    @Autowired
    public AgendaCard(Paciente paciente, SesionService sesionService, NotificacionService notificacionService, PacienteRepository pacienteRepository) {
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
        log.info("Paciente inicializado: id={}, username={}", this.paciente.getId(), this.paciente.getUsername());

        setWidth("400px");
        setPadding(true);
        setSpacing(true);

        H3 titulo = new H3("Agendar cita");

        HorizontalLayout navMes = construirNavMes();
        calendarioContainer = new Div();
        calendarioContainer.setWidthFull();
        recargarSesionesMes();
        calendarioContainer.add(construirCalendarioMes());

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

        citasCombo = new ComboBox<>("Citas programadas");
        citasCombo.setItemLabelGenerator(s -> s.getFecha().format(FORMATO_FECHA) + " " +
                s.getFecha().format(FORMATO_HORA) + " - " + s.getMotivo());
        citasCombo.setWidthFull();
        actualizarCitas();

        Button reprogramar = new Button("Reprogramar", e -> reprogramarCita());
        reprogramar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        reprogramar.setEnabled(false);
        citasCombo.addValueChangeListener(e -> reprogramar.setEnabled(e.getValue() != null));

        HorizontalLayout botones = new HorizontalLayout(agendar, reprogramar);
        botones.setWidthFull();
        botones.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        add(titulo, navMes, calendarioContainer, fechaHoraPicker, motivoField, lugarField, citasCombo, botones);
    }

    private HorizontalLayout construirNavMes() {
        Button prev = new Button("‹");
        prev.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prev.addClickListener(e -> actualizarMesMostrado(mesMostrado.minusMonths(1)));

        etiquetaMes = new Span(capitalizarInicialMes(FORMATO_MES.format(mesMostrado)));
        etiquetaMes.getStyle().set("font-weight", "600");

        Button next = new Button("›");
        next.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        next.addClickListener(e -> actualizarMesMostrado(mesMostrado.plusMonths(1)));

        HorizontalLayout nav = new HorizontalLayout(prev, etiquetaMes, next);
        nav.setWidthFull();
        nav.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        nav.setSpacing(true);
        return nav;
    }

    private void actualizarMesMostrado(YearMonth nuevoMes) {
        mesMostrado = nuevoMes;
        etiquetaMes.setText(capitalizarInicialMes(FORMATO_MES.format(mesMostrado)));
        recargarSesionesMes();
        calendarioContainer.removeAll();
        calendarioContainer.add(construirCalendarioMes());
    }

    private void recargarSesionesMes() {
        sesionesPorDia.clear();
        List<Sesion> sesiones = sesionService.obtenerSesionesPorPacienteYMes(paciente.getId(), mesMostrado);
        sesiones.stream()
                .filter(s -> s.getEstado() == Sesion.EstadoSesion.PROGRAMADA || s.getEstado() == Sesion.EstadoSesion.CONFIRMADA)
                .collect(Collectors.groupingBy(s -> s.getFecha().toLocalDate(), Collectors.counting()))
                .forEach((k, v) -> sesionesPorDia.put(k, v.intValue()));
    }

    private Div construirCalendarioMes() {
        Div cont = new Div();
        cont.setWidthFull();
        cont.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(7, minmax(0, 1fr))")
                .set("gap", "6px")
                .set("box-sizing", "border-box")
                .set("max-width", "100%")
                .set("min-width", "0");

        String[] dias = {"L", "M", "X", "J", "V", "S", "D"};
        for (String d : dias) {
            Div hd = new Div();
            hd.setText(d);
            hd.getStyle()
                    .set("text-align", "center")
                    .set("font-weight", "600")
                    .set("color", "var(--lumo-secondary-text-color)");
            if ("D".equals(d)) {
                hd.getStyle().set("color", "var(--lumo-error-text-color)");
            }
            cont.add(hd);
        }

        LocalDate primero = mesMostrado.atDay(1);
        int offset = primero.getDayOfWeek().getValue() - 1;
        int diasMes = mesMostrado.lengthOfMonth();

        for (int i = 0; i < offset; i++) cont.add(crearCeldaVacia());

        for (int dia = 1; dia <= diasMes; dia++) {
            LocalDate fecha = mesMostrado.atDay(dia);
            int count = sesionesPorDia.getOrDefault(fecha, 0);
            Div celda = crearCeldaDia(fecha, count);
            cont.add(celda);
        }
        return cont;
    }

    private Div crearCeldaVacia() {
        Div d = new Div();
        d.getStyle()
                .set("min-height", "42px")
                .set("border-radius", "8px")
                .set("background", "transparent")
                .set("min-width", "0");
        return d;
    }

    private Div crearCeldaDia(LocalDate fecha, int count) {
        boolean esDomingo = fecha.getDayOfWeek() == DayOfWeek.SUNDAY;
        boolean esFestivo = FestivosColombia.esFestivo(fecha);

        Div cell = new Div();
        cell.getStyle()
                .set("min-height", "42px")
                .set("padding", "6px")
                .set("border-radius", "8px")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("box-sizing", "border-box")
                .set("min-width", "0")
                .set("overflow", "hidden");

        Div num = new Div();
        num.setText(String.valueOf(fecha.getDayOfMonth()));
        num.getStyle().set("font-weight", "600");

        if (esDomingo) {
            num.getStyle().set("color", "var(--lumo-error-text-color)");
            cell.getElement().setAttribute("aria-label", "Domingo");
        }
        if (esFestivo) {
            cell.getStyle().set("background", "var(--lumo-error-color-10pct)");
            FestivosColombia.nombreFestivo(fecha)
                    .ifPresent(nombre -> cell.getElement().setAttribute("title", "Festivo: " + nombre));
            if (!esDomingo) {
                num.getStyle().set("color", "var(--lumo-error-text-color)");
            }
        } else if (count > 0) {
            cell.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        }

        if (fecha.equals(LocalDate.now())) {
            cell.getStyle().set("border", "2px solid var(--lumo-primary-color-50pct)");
        } else {
            cell.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        }

        if (count > 0) {
            Div badge = new Div();
            badge.setText(String.valueOf(count));
            badge.getStyle()
                    .set("min-width", "18px")
                    .set("height", "18px")
                    .set("line-height", "18px")
                    .set("text-align", "center")
                    .set("font-size", "11px")
                    .set("color", "white")
                    .set("background", "var(--lumo-primary-color)")
                    .set("border-radius", "10px")
                    .set("padding", "0 4px");
            cell.add(num, badge);
        } else {
            cell.add(num);
        }

        cell.addClickListener(e -> {
            fechaHoraPicker.setValue(fecha.atTime(9, 0));
        });

        return cell;
    }

    @Transactional
    private void agendarCita() {
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
        if (paciente == null || paciente.getId() == null) {
            log.error("Paciente no válido: id=null, username={}", paciente != null ? paciente.getUsername() : "null");
            mostrarNotificacion("Error: No se pudo identificar al paciente.", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!sesionService.estaDisponible(inicio, DURACION_CITA)) {
            mostrarNotificacion("El horario seleccionado ya está ocupado o hay un conflicto con el desplazamiento.", NotificationVariant.LUMO_ERROR);
            return;
        }

        Sesion sesion = new Sesion();
        sesion.setFecha(inicio);
        sesion.setMotivo(txtMotivo);
        sesion.setEstado(Sesion.EstadoSesion.PROGRAMADA);
        sesion.setPaciente(paciente);
        sesion.setLugar(txtLugar.isBlank() ? null : txtLugar);
        sesion.setDuracion(DURACION_CITA);

        try {
            log.info("Guardando sesión: paciente_id={}, fecha={}, motivo={}", paciente.getId(), inicio, txtMotivo);
            sesionService.guardarSesion(sesion);
            mostrarNotificacion("Sesión programada: " + inicio.format(FORMATO_FECHA) + " " + inicio.format(FORMATO_HORA), NotificationVariant.LUMO_SUCCESS);
            notificacionService.enviarNotificacionProgramacionMedico(sesion);
            actualizarCitas();
            recargarSesionesMes();
            calendarioContainer.removeAll();
            calendarioContainer.add(construirCalendarioMes());
            limpiarFormulario();
        } catch (Exception ex) {
            log.error("Error al guardar la sesión: paciente_id={}, mensaje={}", paciente.getId(), ex.getMessage(), ex);
            mostrarNotificacion("Error al guardar la sesión: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            throw ex;
        }
    }

    @Transactional
    private void reprogramarCita() {
        Sesion sesion = citasCombo.getValue();
        if (sesion == null) {
            mostrarNotificacion("Selecciona una cita para reprogramar.", NotificationVariant.LUMO_ERROR);
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
            if (nuevaFecha == null) {
                mostrarNotificacion("Selecciona una fecha y hora.", NotificationVariant.LUMO_ERROR);
                return;
            }
            if (nuevaFecha.isBefore(LocalDateTime.now())) {
                mostrarNotificacion("La fecha debe ser futura.", NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                log.info("Reprogramando sesión: id={}, nuevaFecha={}", sesion.getId(), nuevaFecha);
                Optional<Sesion> reprogramada = sesionService.reprogramarSesion(sesion.getId(), nuevaFecha, DURACION_CITA);
                if (reprogramada.isPresent()) {
                    mostrarNotificacion("Cita reprogramada: " +
                            nuevaFecha.format(FORMATO_FECHA) + " " + nuevaFecha.format(FORMATO_HORA), NotificationVariant.LUMO_SUCCESS);
                    notificacionService.enviarNotificacionProgramacionMedico(reprogramada.get());
                    actualizarCitas();
                    recargarSesionesMes();
                    calendarioContainer.removeAll();
                    calendarioContainer.add(construirCalendarioMes());
                    citasCombo.clear();
                    dialog.close();
                } else {
                    mostrarNotificacion("No se pudo reprogramar: horario ocupado o cita no encontrada.", NotificationVariant.LUMO_ERROR);
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
        dialog.add(nuevaFechaPicker, botones);
        dialog.open();
    }

    private void actualizarCitas() {
        YearMonth mesActual = YearMonth.now();
        List<Sesion> citas = sesionService.obtenerSesionesPorPacienteYMes(paciente.getId(), mesActual);
        citasCombo.setItems(citas.stream()
                .filter(s -> s.getEstado() == Sesion.EstadoSesion.PROGRAMADA || s.getEstado() == Sesion.EstadoSesion.CONFIRMADA)
                .filter(s -> !s.getFecha().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Sesion::getFecha))
                .collect(Collectors.toList()));
    }

    private void limpiarFormulario() {
        fechaHoraPicker.clear();
        motivoField.clear();
        lugarField.clear();
    }

    private String capitalizarInicialMes(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        return texto.substring(0, 1).toUpperCase(Locale.of("es")) + texto.substring(1);
    }

    private void mostrarNotificacion(String mensaje, NotificationVariant variant) {
        Notification notification = new Notification(mensaje, 3000, Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}