package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.util.FestivosColombia;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Componente de calendario mensual visual.
 * Al seleccionar un día, obtiene las horas disponibles y notifica a los listeners.
 */
@CssImport("./styles/calendario-mes.css")
public class CalendarioMes extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(CalendarioMes.class);

    // --- ACTUALIZACIÓN: Definir Locale como una constante estática ---
    private static final Locale LOCALE_COLOMBIA = Locale.of("es", "CO");

    private final Long pacienteId;
    private final SesionService sesionService;
    private YearMonth mesActual;
    private LocalDate diaSeleccionado;

    // --- Listeners para comunicar con AgendaForm ---
    private final Consumer<LocalDate> onDateSelected;
    private final Consumer<List<LocalTime>> onHoursAvailable;

    // Componentes UI
    private final Span tituloMes = new Span();
    private final Div gridDias = new Div();
    private final Button btnMesAnterior;
    private final Button btnMesSiguiente;

    private Map<LocalDate, Set<Sesion.EstadoSesion>> sesionesDelMes = Collections.emptyMap(); // Para marcar días con citas

    public CalendarioMes(Long pacienteId, SesionService sesionService, Consumer<LocalDate> onDateSelected, Consumer<List<LocalTime>> onHoursAvailable) {
        this.pacienteId = pacienteId;
        this.sesionService = sesionService;
        this.onDateSelected = onDateSelected; // Guardar listener de fecha
        this.onHoursAvailable = onHoursAvailable; // Guardar listener de horas
        this.mesActual = YearMonth.now();

        setPadding(false);
        setSpacing(true); // Espacio entre header y grid
        addClassName("calendario-mes"); // Aplica tus estilos CSS

        // --- Header con Navegación ---
        btnMesAnterior = new Button(VaadinIcon.ARROW_LEFT.create(), e -> cambiarMes(-1));
        btnMesSiguiente = new Button(VaadinIcon.ARROW_RIGHT.create(), e -> cambiarMes(1));
        btnMesAnterior.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        btnMesSiguiente.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        tituloMes.addClassName("calendario-titulo-mes");
        HorizontalLayout header = new HorizontalLayout(btnMesAnterior, tituloMes, btnMesSiguiente);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        // --- Grid de Días ---
        gridDias.addClassName("calendario-grid-dias");

        add(header, gridDias);
        setWidth("100%"); // Ocupa el ancho disponible en AgendaCard

        // Carga inicial
        recargarSesionesMes(); // Carga las sesiones para marcar días
        pintarCalendario(); // Dibuja el calendario inicial
    }

    private void cambiarMes(int delta) {
        mesActual = mesActual.plusMonths(delta);
        diaSeleccionado = null; // Deseleccionar día al cambiar de mes
        onDateSelected.accept(null); // Notificar fecha nula
        onHoursAvailable.accept(Collections.emptyList()); // Notificar horas vacías
        recargarSesionesMes(); // Cargar sesiones del nuevo mes
        pintarCalendario(); // Redibujar
    }

    /**
     * Carga las sesiones del mes actual para el paciente
     * y las agrupa por día para saber qué días marcar.
     */
    public void recargarSesionesMes() {
        try {
            List<Sesion> sesiones = sesionService.obtenerSesionesPorPacienteYMes(pacienteId, mesActual);
            // Agrupa las sesiones por fecha y obtiene los estados presentes en cada fecha
            sesionesDelMes = sesiones.stream()
                    .filter(s -> s.getFecha() != null) // Evita sesiones sin fecha
                    .collect(Collectors.groupingBy(
                            s -> s.getFecha().toLocalDate(),
                            Collectors.mapping(Sesion::getEstado, Collectors.toSet()) // Guarda un Set de Estados por día
                    ));
            log.debug("Sesiones cargadas para {}: {} días con citas.", mesActual, sesionesDelMes.size());
        } catch (Exception e) {
            log.error("Error al cargar sesiones para el mes {}: {}", mesActual, e.getMessage(), e);
            sesionesDelMes = Collections.emptyMap();
            // Considera mostrar notificación de error
        }
        // No es necesario repintar aquí, se hace al cambiar mes o seleccionar día
    }


    private void pintarCalendario() {
        // --- ACTUALIZACIÓN: Usar la constante estática LOCALE_COLOMBIA ---
        tituloMes.setText(mesActual.format(DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_COLOMBIA)));
        gridDias.removeAll();

        // Añadir encabezados de días de la semana (Lu, Ma, Mi...)
        HorizontalLayout weekHeader = new HorizontalLayout();
        weekHeader.addClassName("calendario-week-header");
        // Empezando desde Lunes (DayOfWeek.MONDAY)
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = DayOfWeek.MONDAY.plus(i);
            // --- ACTUALIZACIÓN: Usar la constante estática LOCALE_COLOMBIA ---
            Span dayLabel = new Span(day.getDisplayName(TextStyle.SHORT, LOCALE_COLOMBIA));
            dayLabel.addClassName("calendario-weekday");
            weekHeader.add(dayLabel);
        }
        gridDias.add(weekHeader);


        LocalDate primerDiaMes = mesActual.atDay(1);
        int diaSemanaPrimerDia = primerDiaMes.getDayOfWeek().getValue(); // 1 (Lunes) a 7 (Domingo)
        int offset = (diaSemanaPrimerDia == 1) ? 0 : diaSemanaPrimerDia - 1; // Ajuste para empezar en Lunes

        // Añadir celdas vacías al inicio si el mes no empieza en Lunes
        for (int i = 0; i < offset; i++) {
            Div emptyCell = new Div();
            emptyCell.addClassName("calendario-dia-vacio");
            gridDias.add(emptyCell);
        }

        // Añadir días del mes
        int diasEnMes = mesActual.lengthOfMonth();
        for (int dia = 1; dia <= diasEnMes; dia++) {
            LocalDate fechaDia = mesActual.atDay(dia);
            Div cellDia = new Div();
            cellDia.addClassName("calendario-dia");
            cellDia.setText(String.valueOf(dia));

            // Marcar día actual, seleccionado, festivo, fin de semana, con citas
            if (fechaDia.equals(LocalDate.now())) {
                cellDia.addClassName("hoy");
            }
            if (fechaDia.equals(diaSeleccionado)) {
                cellDia.addClassName("seleccionado");
            }
            if (FestivosColombia.esFestivo(fechaDia)) {
                cellDia.addClassName("festivo");
            }
            DayOfWeek dow = fechaDia.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                cellDia.addClassName("finde");
            }
            // Marcar si hay sesiones ESE DÍA
            Set<Sesion.EstadoSesion> estadosEnDia = sesionesDelMes.get(fechaDia);
            if (estadosEnDia != null && !estadosEnDia.isEmpty()) {
                cellDia.addClassName("con-cita");
                // Podrías añadir clases específicas por estado si quieres (ej. "cita-confirmada", "cita-programada")
                // if (estadosEnDia.contains(EstadoSesion.CONFIRMADA)) cellDia.addClassName("cita-confirmada");
            }


            // --- Lógica de Selección y Obtención de Horas ---
            // Solo permitir seleccionar días válidos (no pasados, no domingos, no festivos)
            if (fechaDia.isBefore(LocalDate.now()) || dow == DayOfWeek.SUNDAY || FestivosColombia.esFestivo(fechaDia)) {
                cellDia.addClassName("deshabilitado");
            } else {
                // Habilitar click solo en días válidos
                cellDia.addClickListener(e -> seleccionarDia(fechaDia));
            }

            gridDias.add(cellDia);
        }
        // Habilitar/deshabilitar botones de navegación
        btnMesAnterior.setEnabled(!mesActual.equals(YearMonth.now())); // No ir a meses pasados desde hoy
    }

    /**
     * Se ejecuta al hacer clic en un día válido.
     * Marca el día, notifica la fecha seleccionada y obtiene/notifica las horas disponibles.
     */
    private void seleccionarDia(LocalDate fecha) {
        log.debug("Día seleccionado: {}", fecha);
        diaSeleccionado = fecha;
        pintarCalendario(); // Repinta para mostrar la selección

        // 1. Notificar la fecha seleccionada a AgendaForm
        if (onDateSelected != null) {
            onDateSelected.accept(fecha);
        }

        // 2. Obtener y notificar las horas disponibles
        List<LocalTime> horasDisponibles = Collections.emptyList();
        try {
            horasDisponibles = sesionService.getHorasDisponibles(fecha);
            log.debug("Horas disponibles para {} obtenidas: {}", fecha, horasDisponibles.size());
        } catch (Exception e) {
            log.error("Error al obtener horas disponibles para {}: {}", fecha, e.getMessage(), e);
            // Considera notificar al usuario
        }
        if (onHoursAvailable != null) {
            onHoursAvailable.accept(horasDisponibles);
        }
    }
}