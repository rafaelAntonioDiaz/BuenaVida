package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente que muestra una lista de citas programadas con opción de reprogramar.
 * Reemplaza al CitasCombo anterior con una interfaz más visual e intuitiva.
 */
public class CitasLista extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(CitasLista.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final Long pacienteId;
    private final SesionService sesionService;
    private final NotificacionService notificacionService;
    private final ReprogramarDialog reprogramarDialog;
    private final VerticalLayout contenedorCitas;

    /**
     * Constructor del componente de lista de citas.
     *
     * @param pacienteId ID del paciente
     * @param sesionService Servicio para gestionar sesiones
     * @param notificacionService Servicio para enviar notificaciones
     */
    public CitasLista(Long pacienteId, SesionService sesionService, NotificacionService notificacionService) {
        this.pacienteId = pacienteId;
        this.sesionService = sesionService;
        this.notificacionService = notificacionService;
        this.reprogramarDialog = new ReprogramarDialog(sesionService, notificacionService);

        setWidthFull();
        setPadding(false);
        setSpacing(true);

        // Título de la sección
        H4 titulo = new H4("Próximas citas");
        titulo.getStyle()
                .set("margin-top", "20px")
                .set("margin-bottom", "10px")
                .set("color", "var(--lumo-secondary-text-color)");

        // Contenedor para las tarjetas de citas
        contenedorCitas = new VerticalLayout();
        contenedorCitas.setWidthFull();
        contenedorCitas.setPadding(false);
        contenedorCitas.setSpacing(true);

        add(titulo, contenedorCitas);
        actualizarCitas();
    }

    /**
     * Actualiza la lista de citas obteniendo las sesiones programadas y confirmadas
     * del paciente que sean futuras (desde hoy en adelante).
     */
    public void actualizarCitas() {
        contenedorCitas.removeAll();

        // Obtener todas las citas futuras del paciente
        LocalDateTime ahora = LocalDateTime.now();
        List<Sesion> citas = sesionService.obtenerSesionesPorPacienteYMes(pacienteId,
                java.time.YearMonth.now());

        // Filtrar solo citas programadas/confirmadas y futuras
        List<Sesion> citasFuturas = citas.stream()
                .filter(s -> s.getEstado() == Sesion.EstadoSesion.PROGRAMADA ||
                        s.getEstado() == Sesion.EstadoSesion.CONFIRMADA)
                .filter(s -> s.getFecha().isAfter(ahora))
                .sorted(Comparator.comparing(Sesion::getFecha))
                .collect(Collectors.toList());

        if (citasFuturas.isEmpty()) {
            // Mostrar mensaje cuando no hay citas
            mostrarMensajeSinCitas();
        } else {
            // Crear una tarjeta por cada cita
            citasFuturas.forEach(this::crearTarjetaCita);
        }

        log.debug("Citas actualizadas para pacienteId={}: {} citas futuras encontradas",
                pacienteId, citasFuturas.size());
    }

    /**
     * Muestra un mensaje amigable cuando no hay citas programadas.
     */
    private void mostrarMensajeSinCitas() {
        Div mensaje = new Div();
        mensaje.setText("No tienes citas programadas");
        mensaje.getStyle()
                .set("padding", "20px")
                .set("text-align", "center")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "8px");
        contenedorCitas.add(mensaje);
    }

    /**
     * Crea una tarjeta visual para una cita individual.
     *
     * @param sesion La sesión/cita a mostrar
     */
    private void crearTarjetaCita(Sesion sesion) {
        // Contenedor principal de la tarjeta
        Div tarjeta = new Div();
        tarjeta.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "12px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("gap", "12px");

        // Sección izquierda: información de la cita
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);
        infoLayout.getStyle().set("flex", "1");

        // Fecha y hora con icono
        HorizontalLayout fechaHoraLayout = new HorizontalLayout();
        fechaHoraLayout.setSpacing(true);
        fechaHoraLayout.setAlignItems(Alignment.CENTER);

        Icon iconoCalendario = VaadinIcon.CALENDAR.create();
        iconoCalendario.setSize("16px");
        iconoCalendario.getStyle().set("color", "var(--lumo-primary-color)");

        Span fechaHora = new Span(sesion.getFecha().format(FORMATO_FECHA) +
                " a las " + sesion.getFecha().format(FORMATO_HORA));
        fechaHora.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--lumo-body-text-color)");

        fechaHoraLayout.add(iconoCalendario, fechaHora);

        // Motivo de la cita
        Span motivo = new Span(sesion.getMotivo());
        motivo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.9em")
                .set("margin-top", "4px");

        // Lugar (si existe)
        if (sesion.getLugar() != null && !sesion.getLugar().isBlank()) {
            HorizontalLayout lugarLayout = new HorizontalLayout();
            lugarLayout.setSpacing(true);
            lugarLayout.setAlignItems(Alignment.CENTER);

            Icon iconoLugar = VaadinIcon.MAP_MARKER.create();
            iconoLugar.setSize("14px");
            iconoLugar.getStyle().set("color", "var(--lumo-secondary-text-color)");

            Span lugar = new Span(sesion.getLugar());
            lugar.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "0.85em");

            lugarLayout.add(iconoLugar, lugar);
            infoLayout.add(fechaHoraLayout, motivo, lugarLayout);
        } else {
            infoLayout.add(fechaHoraLayout, motivo);
        }

        // Badge de estado
        Span badge = crearBadgeEstado(sesion.getEstado());

        // Botón de reprogramar
        Button btnReprogramar = new Button("Reprogramar");
        btnReprogramar.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        btnReprogramar.setIcon(VaadinIcon.CLOCK.create());
        btnReprogramar.addClickListener(e -> {
            reprogramarDialog.open(sesion, this::actualizarCitas);
        });

        // Layout para botones y badge
        HorizontalLayout accionesLayout = new HorizontalLayout();
        accionesLayout.setSpacing(true);
        accionesLayout.setAlignItems(Alignment.CENTER);
        accionesLayout.add(badge, btnReprogramar);

        // Ensamblar la tarjeta
        tarjeta.add(infoLayout, accionesLayout);
        contenedorCitas.add(tarjeta);
    }

    /**
     * Crea un badge visual para mostrar el estado de la sesión.
     *
     * @param estado Estado de la sesión
     * @return Span con el badge estilizado
     */
    private Span crearBadgeEstado(Sesion.EstadoSesion estado) {
        Span badge = new Span(estado.toString());
        badge.getStyle()
                .set("font-size", "0.75em")
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-weight", "600")
                .set("text-transform", "uppercase");

        // Colores según el estado
        switch (estado) {
            case PROGRAMADA:
                badge.getStyle()
                        .set("background", "var(--lumo-primary-color-10pct)")
                        .set("color", "var(--lumo-primary-color)");
                break;
            case CONFIRMADA:
                badge.getStyle()
                        .set("background", "var(--lumo-success-color-10pct)")
                        .set("color", "var(--lumo-success-color)");
                break;
            default:
                badge.getStyle()
                        .set("background", "var(--lumo-contrast-10pct)")
                        .set("color", "var(--lumo-secondary-text-color)");
        }

        return badge;
    }
}