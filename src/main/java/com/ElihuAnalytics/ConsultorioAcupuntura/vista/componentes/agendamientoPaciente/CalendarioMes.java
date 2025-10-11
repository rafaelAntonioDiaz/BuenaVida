package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.util.FestivosColombia;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Locale;

/**
 * Componente para el calendario visual con navegación de meses.
 */
public class CalendarioMes extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(CalendarioMes.class);
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("es"));

    private final Long pacienteId;
    private final SesionService sesionService;
    private final DateTimePicker fechaHoraPicker;
    private YearMonth mesMostrado = YearMonth.now();
    private final Map<LocalDate, Integer> sesionesPorDia = new HashMap<>();
    private final Div calendarioContainer;
    private final Span etiquetaMes = new Span(capitalizarInicialMes(FORMATO_MES.format(YearMonth.now())));

    public CalendarioMes(Long pacienteId, SesionService sesionService, DateTimePicker fechaHoraPicker) {
        this.pacienteId = pacienteId;
        this.sesionService = sesionService;
        this.fechaHoraPicker = fechaHoraPicker;

        setPadding(true);
        setSpacing(true);

        HorizontalLayout navMes = construirNavMes();
        calendarioContainer = new Div();
        calendarioContainer.setWidthFull();
        recargarSesionesMes();
        calendarioContainer.add(construirCalendarioMes());

        add(navMes, calendarioContainer);
    }

    private HorizontalLayout construirNavMes() {
        Button prev = new Button("‹");
        prev.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prev.addClickListener(e -> actualizarMesMostrado(mesMostrado.minusMonths(1)));

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

    public void recargarSesionesMes() {
        sesionesPorDia.clear();
        List<Sesion> sesiones = sesionService.obtenerSesionesPorPacienteYMes(pacienteId, mesMostrado);
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

    private String capitalizarInicialMes(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        return texto.substring(0, 1).toUpperCase(Locale.of("es")) + texto.substring(1);
    }
}