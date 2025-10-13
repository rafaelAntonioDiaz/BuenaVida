package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.AntecedenteRelevante;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.Optional;
public class AntecedentesRelevantesCard extends Div {

    private final Long historiaId;
    private final HistoriaClinicaService service;
    public AntecedentesRelevantesCard(Long historiaId, HistoriaClinicaService service) {
        this.historiaId = historiaId;
        this.service = service;

        addClassNames("card");


        add(new H3("Antecedente relevante"));

        pintar();
    }

    private void pintar() {
        removeAll();
        add(new H3("Antecedente relevante"));

        Optional<HistoriaClinica> hcOpt = service.obtenerPorPacienteIdDeHistoria(historiaId);
        String ant = hcOpt.map(HistoriaClinica::getAntecedenteRelevante)
                .map(AntecedenteRelevante::getDescripcion)
                .orElse("No registrado");

        Paragraph pAnt = new Paragraph(ant);
        pAnt.addClassName("antecedente-texto"); // ✅ clave para aplicar el fix CSS

        add(pAnt);
    }

    private void abrirEditor(String valorActual) {
        TextArea ta = new TextArea("Editar antecedente");
        ta.setWidthFull();
        ta.setValue(Optional.ofNullable(valorActual).orElse(""));

        Button guardar = new Button("Guardar", e -> {
            try {
                service.actualizarAntecedente(historiaId, ta.getValue(), null);
                Notification.show("Antecedente actualizado");
                pintar();
            } catch (Exception ex) {
                Notification.show("No se pudo guardar el antecedente");
            }
        });

        Button cancelar = new Button("Cancelar", e -> pintar());

        add(ta, new HorizontalLayout(guardar, cancelar));
    }
}
