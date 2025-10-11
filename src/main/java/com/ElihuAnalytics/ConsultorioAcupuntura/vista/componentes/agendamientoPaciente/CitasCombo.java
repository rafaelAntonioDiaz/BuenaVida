package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente para el ComboBox de citas programadas y reprogramación.
 */
public class CitasCombo extends HorizontalLayout {

    private static final Logger log = LoggerFactory.getLogger(CitasCombo.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final Long pacienteId;
    private final SesionService sesionService;
    private final NotificacionService notificacionService;
    private final ComboBox<Sesion> citasCombo;
    private final ReprogramarDialog reprogramarDialog;

    public CitasCombo(Long pacienteId, SesionService sesionService, NotificacionService notificacionService) {
        this.pacienteId = pacienteId;
        this.sesionService = sesionService;
        this.notificacionService = notificacionService;
        this.reprogramarDialog = new ReprogramarDialog(sesionService, notificacionService);

        setWidthFull();
        setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        citasCombo = new ComboBox<>("Citas programadas");
        citasCombo.setItemLabelGenerator(s -> s.getFecha().format(FORMATO_FECHA) + " " +
                s.getFecha().format(FORMATO_HORA) + " - " + s.getMotivo());
        citasCombo.setWidthFull();
        actualizarCitas();

        Button reprogramar = new Button("Reprogramar", e -> reprogramarDialog.open(citasCombo.getValue(), this::actualizarCitas));
        reprogramar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        reprogramar.setEnabled(false);
        citasCombo.addValueChangeListener(e -> reprogramar.setEnabled(e.getValue() != null));

        add(citasCombo, reprogramar);
    }

    public void actualizarCitas() {
        YearMonth mesActual = YearMonth.now();
        List<Sesion> citas = sesionService.obtenerSesionesPorPacienteYMes(pacienteId, mesActual);
        citasCombo.setItems(citas.stream()
                .filter(s -> s.getEstado() == Sesion.EstadoSesion.PROGRAMADA || s.getEstado() == Sesion.EstadoSesion.CONFIRMADA)
                .filter(s -> !s.getFecha().isBefore(YearMonth.now().atDay(1).atStartOfDay()))
                .sorted(Comparator.comparing(Sesion::getFecha))
                .collect(Collectors.toList()));
        citasCombo.clear();
        log.debug("Citas actualizadas para pacienteId={}: {} citas encontradas", pacienteId, citas.size());
    }
}