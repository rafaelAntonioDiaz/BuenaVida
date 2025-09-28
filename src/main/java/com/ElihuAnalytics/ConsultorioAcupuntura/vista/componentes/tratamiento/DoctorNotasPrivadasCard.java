// java
package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.NotaPrivada;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotaPrivadaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Notas privadas del médico (persistentes):
 * - Crear notas (más reciente arriba).
 * - Editar la nota más reciente (desde la lista y con botón rápido).
 * - Conectada a NotaPrivadaService (MySQL).
 */
public class DoctorNotasPrivadasCard extends Div {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final HistoriaClinica hc;
    private final NotaPrivadaService service;

    private final VerticalLayout lista = new VerticalLayout();
    // Botón rápido para editar la última nota (más reciente)
    private final Button editarUltimaBtn = new Button("Editar última nota");

    public DoctorNotasPrivadasCard(HistoriaClinica hc, NotaPrivadaService service) {
        this.hc = Objects.requireNonNull(hc, "Historia clínica requerida");
        this.service = Objects.requireNonNull(service, "Servicio de notas requerido");

        addClassName("card");
        add(new H3("Notas privadas del médico"));

        // Acciones superiores: Nueva nota + Editar última
        Button nueva = new Button("Nueva nota", e -> abrirEditorNueva());

        editarUltimaBtn.setEnabled(false); // se habilita si hay al menos una nota en pintar()
        editarUltimaBtn.addClickListener(e -> abrirEditorUltima());

        HorizontalLayout accionesHeader = new HorizontalLayout(nueva, editarUltimaBtn);
        accionesHeader.setSpacing(true);
        add(accionesHeader);

        lista.setPadding(false);
        lista.setSpacing(false);
        add(lista);

        pintar();
    }

    // Render de la lista, con la más reciente de primera
    private void pintar() {
        lista.removeAll();
        List<NotaPrivada> notas = service.listarDesc(hc.getId());

        // Habilitar/Deshabilitar el botón "Editar última nota" según haya o no registros
        editarUltimaBtn.setEnabled(!notas.isEmpty());

        if (notas.isEmpty()) {
            lista.add(new Paragraph("Aún no hay notas. Crea la primera con el botón de arriba."));
            return;
        }

        for (int i = 0; i < notas.size(); i++) {
            NotaPrivada n = notas.get(i);
            boolean esPrimera = i == 0;

            Paragraph head = new Paragraph(FECHA_HORA.format(n.getFechaHora()));
            head.getStyle().set("font-weight", "600").set("margin", "0");

            Paragraph cuerpo = new Paragraph(Optional.ofNullable(n.getTexto()).orElse(""));
            cuerpo.getStyle().set("margin", "0");

            if (esPrimera) {
                // También mantenemos el botón de editar en la primera fila para consistencia
                Button editar = new Button("Editar", e -> abrirEditorEdicion(n));
                lista.add(new HorizontalLayout(head, editar), cuerpo);
            } else {
                lista.add(head, cuerpo);
            }
        }
    }

    // Editor inline: crear nota nueva
    private void abrirEditorNueva() {
        TextArea ta = new TextArea("Nueva nota (privada)");
        ta.setWidthFull();
        ta.setMinHeight("120px");
        ta.setPlaceholder("Escribe aquí tus notas clínicas, observaciones y reacciones del tratamiento...");

        final HorizontalLayout[] acciones = new HorizontalLayout[1];
        Button guardar = new Button("Guardar", e -> {
            String txt = Optional.ofNullable(ta.getValue()).orElse("").trim();
            if (txt.isBlank()) {
                Notification.show("La nota no puede estar vacía.");
                return;
            }
            service.crear(hc, txt);
            lista.remove(ta, acciones[0]);
            pintar();
        });
        Button cancelar = new Button("Cancelar", e -> lista.remove(ta, acciones[0]));

        acciones[0] = new HorizontalLayout(guardar, cancelar);
        acciones[0].getStyle().set("margin-top", "6px");
        lista.add(ta, acciones[0]);
    }

    // Editor inline: editar la más reciente usando el botón rápido
    private void abrirEditorUltima() {
        List<NotaPrivada> notas = service.listarDesc(hc.getId());
        if (notas.isEmpty()) {
            Notification.show("No hay notas para editar.");
            return;
        }
        // La más reciente es la primera en el orden descendente
        abrirEditorEdicion(notas.get(0));
    }

    // Editor inline: editar la más reciente (cuando se dispara desde la fila superior)
    private void abrirEditorEdicion(NotaPrivada nota) {
        TextArea ta = new TextArea("Editar nota (más reciente)");
        ta.setWidthFull();
        ta.setMinHeight("120px");
        ta.setValue(Optional.ofNullable(nota.getTexto()).orElse(""));

        final HorizontalLayout[] acciones = new HorizontalLayout[1];
        Button guardar = new Button("Guardar", e -> {
            String txt = Optional.ofNullable(ta.getValue()).orElse("").trim();
            if (txt.isBlank()) {
                Notification.show("La nota no puede estar vacía.");
                return;
            }
            service.actualizar(nota.getId(), txt);
            lista.remove(ta, acciones[0]);
            pintar();
        });
        Button cancelar = new Button("Cancelar", e -> lista.remove(ta, acciones[0]));

        acciones[0] = new HorizontalLayout(guardar, cancelar);
        acciones[0].getStyle().set("margin-top", "6px");
        lista.add(ta, acciones[0]);
    }
}