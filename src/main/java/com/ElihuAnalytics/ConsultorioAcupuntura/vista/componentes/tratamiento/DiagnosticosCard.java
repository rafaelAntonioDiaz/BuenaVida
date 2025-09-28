package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.Optional;

public class DiagnosticosCard extends Div {

    private final Long historiaId;
    private final HistoriaClinicaService service;

    public DiagnosticosCard(Long historiaId, HistoriaClinicaService service) {
        this.historiaId = historiaId;
        this.service = service;

        addClassName("card");
        add(new H3("Diagnóstico tradicional"));

        pintar();
    }

    private void pintar() {
        removeAll();
        add(new H3("Diagnóstico tradicional"));

        Optional<HistoriaClinica> hcOpt = service.obtenerPorPacienteIdDeHistoria(historiaId);
        String diag = hcOpt.map(HistoriaClinica::getDiagnosticoTradicional).orElse("No registrado");

        Paragraph pDiag = new Paragraph(diag);
        Button editar = new Button("Editar", e -> abrirEditor(diag));

        add(pDiag, editar);
    }

    private void abrirEditor(String valorActual) {
        TextArea ta = new TextArea("Editar diagnóstico");
        ta.setWidthFull();
        ta.setValue(Optional.ofNullable(valorActual).orElse(""));

        Button guardar = new Button("Guardar", e -> {
            String nuevo = ta.getValue().trim();
            try {
                service.actualizarDiagnosticoTradicional(historiaId, nuevo);
                Notification.show("Diagnóstico actualizado");
                pintar();
            } catch (Exception ex) {
                Notification.show("No se pudo guardar el diagnóstico");
            }
        });

        Button cancelar = new Button("Cancelar", e -> pintar());

        add(ta, new HorizontalLayout(guardar, cancelar));
    }
}
