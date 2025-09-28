package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.util.AdjuntosHelper;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.AntecedenteRelevante;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.FileStorageService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Card de "Antecedentes relevantes".
 * Extraído fielmente desde PacienteView (crearSeccionAntecedentes),
 * con subida y eliminación de adjuntos (más nuevo arriba).
 */
public class AntecedentesCard extends Div {

    private final HistoriaClinicaService historiaClinicaService;
    private final FileStorageService fileStorageService;
    private final HistoriaClinica hc;
    private final AntecedenteRelevante antRef;
    private final AdjuntosHelper adjuntosHelper;

    public AntecedentesCard(HistoriaClinica hc,
                            HistoriaClinicaService historiaClinicaService,
                            FileStorageService fileStorageService) {
        this.hc = hc;
        this.historiaClinicaService = historiaClinicaService;
        this.fileStorageService = fileStorageService;
        this.adjuntosHelper = new AdjuntosHelper(fileStorageService);

        // Autoajuste y estilo compacto (usa la clase .card del tema + compact)
        addClassName("card");
        addClassName("card--compact");
        setWidthFull();
        getStyle().set("min-width", "0");
        // Compactar controles dentro de la card
        getElement().getStyle().set("--lumo-text-field-size", "var(--lumo-size-s)");
        getElement().getStyle().set("--lumo-button-size", "var(--lumo-size-s)");

        // Título
        H3 titulo = new H3("Antecedentes relevantes");
        titulo.getStyle().set("margin-top", "0");
        add(titulo);

        // Asegurar existencia del objeto de antecedentes
        AntecedenteRelevante ant = hc.getAntecedenteRelevante();
        if (ant == null) {
            ant = new AntecedenteRelevante();
            hc.setAntecedenteRelevante(ant);
            historiaClinicaService.guardar(hc);
        }
        this.antRef = ant;

        if (antRef.getDescripcionesPorRuta() == null) antRef.setDescripcionesPorRuta(new LinkedHashMap<>());
        if (antRef.getRutasArchivos() == null) antRef.setRutasArchivos(new ArrayList<>());

        // Descripción con fallback de texto
        Paragraph pDescripcion = new Paragraph(
                Optional.ofNullable(antRef.getDescripcion()).filter(s -> !s.isBlank())
                        .orElse("No se han registrado antecedentes aún.")
        );
        pDescripcion.getStyle().set("margin", "0");

        // Botón para editar antecedentes (diálogo inline) - compacto
        Button editar = new Button("Editar antecedentes", e -> abrirDialogoEditarAntecedentes(pDescripcion));
        editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        add(pDescripcion, editar);

        // Lista de adjuntos
        VerticalLayout listaAdjuntos = new VerticalLayout();
        listaAdjuntos.setPadding(false);
        listaAdjuntos.setSpacing(false);
        listaAdjuntos.setWidthFull();
        listaAdjuntos.getStyle().set("gap", "var(--lumo-space-s)");
        add(new H5("Documentos adjuntos"), listaAdjuntos);
        refrescarListaAdjuntosAntecedentes(listaAdjuntos);

        // Upload de adjuntos con descripción (compacto)
        Upload upload = adjuntosHelper.crearUpload(this, (ruta, originalName, descripcionArchivo) -> {
            antRef.getDescripcionesPorRuta().put(ruta, descripcionArchivo);
            if (!antRef.getRutasArchivos().contains(ruta)) antRef.getRutasArchivos().add(ruta);
            historiaClinicaService.guardar(hc);
            refrescarListaAdjuntosAntecedentes(listaAdjuntos);
            Notification.show("Adjunto agregado");
        }, () -> Notification.show("Subida cancelada"));
        upload.getElement().getStyle().set("--lumo-button-size", "var(--lumo-size-s)");
        add(upload);
    }

    private void abrirDialogoEditarAntecedentes(Paragraph pDescripcion) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Editar antecedentes relevantes");

        TextArea area = new TextArea("Descripción");
        area.setWidthFull();
        area.addThemeVariants(com.vaadin.flow.component.textfield.TextAreaVariant.LUMO_SMALL);
        area.setValue(Optional.ofNullable(antRef.getDescripcion()).orElse(""));
        area.setMaxLength(5000);
        area.setMaxHeight("12rem");

        Button guardar = new Button("Guardar", e -> {
            antRef.setDescripcion(Optional.ofNullable(area.getValue()).orElse("").trim());
            historiaClinicaService.guardar(hc);
            pDescripcion.setText(Optional.ofNullable(antRef.getDescripcion()).filter(s -> !s.isBlank())
                    .orElse("No se han registrado antecedentes aún."));
            Notification.show("Antecedentes actualizados");
            dlg.close();
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Button cancelar = new Button("Cancelar", e -> dlg.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout footer = new HorizontalLayout(guardar, cancelar);
        footer.setWidthFull();
        footer.setSpacing(false);
        footer.getStyle().set("gap", "var(--lumo-space-s)");
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        dlg.add(area);
        dlg.getFooter().add(footer);
        dlg.open();
    }

    private void refrescarListaAdjuntosAntecedentes(VerticalLayout listaAdjuntos) {
        listaAdjuntos.removeAll();

        Map<String, String> mapa = antRef.getDescripcionesPorRuta();
        if (mapa != null && !mapa.isEmpty()) {
            List<Map.Entry<String, String>> entries = new ArrayList<>(mapa.entrySet());
            Collections.reverse(entries);
            entries.forEach(e -> listaAdjuntos.add(crearFilaAdjunto(e.getKey(), e.getValue(), () -> {
                confirmarEliminar("¿Eliminar este adjunto?", () -> {
                    antRef.getDescripcionesPorRuta().remove(e.getKey());
                    if (antRef.getRutasArchivos() != null) antRef.getRutasArchivos().removeIf(r -> Objects.equals(r, e.getKey()));
                    fileStorageService.delete(e.getKey());
                    historiaClinicaService.guardar(hc);
                    refrescarListaAdjuntosAntecedentes(listaAdjuntos);
                    Notification.show("Adjunto eliminado");
                });
            })));
        } else if (antRef.getRutasArchivos() != null && !antRef.getRutasArchivos().isEmpty()) {
            List<String> rutas = new ArrayList<>(antRef.getRutasArchivos());
            Collections.reverse(rutas);
            rutas.forEach(ruta -> listaAdjuntos.add(crearFilaAdjunto(ruta, "Ver archivo adjunto", () -> {
                confirmarEliminar("¿Eliminar este adjunto?", () -> {
                    antRef.getRutasArchivos().removeIf(r -> Objects.equals(r, ruta));
                    fileStorageService.delete(ruta);
                    historiaClinicaService.guardar(hc);
                    refrescarListaAdjuntosAntecedentes(listaAdjuntos);
                    Notification.show("Adjunto eliminado");
                });
            })));
        } else {
            listaAdjuntos.add(new Paragraph("No hay documentos adjuntos."));
        }
    }

    private HorizontalLayout crearFilaAdjunto(String ruta, String descripcion, Runnable onEliminar) {
        Anchor enlace = new Anchor(ruta, descripcion);
        enlace.setTarget("_blank");
        enlace.getStyle().set("word-break", "break-word").set("white-space", "normal");

        Button eliminar = new Button("Eliminar", e -> onEliminar.run());
        eliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);

        HorizontalLayout fila = new HorizontalLayout(enlace, eliminar);
        fila.setWidthFull();
        fila.setSpacing(false);
        fila.getStyle().set("gap", "var(--lumo-space-s)").set("min-width", "0");
        fila.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return fila;
    }


    // Upload genérico con confirmación de descripción
    private Upload crearUpload(TriConsumer<String, String, String> onConfirm, Runnable onCancelUpload) {
        Upload upload = new Upload();
        upload.setWidthFull();
        upload.setAutoUpload(true);
        upload.setMaxFiles(10);
        upload.setMaxFileSize(10 * 1024 * 1024); // 10 MB
        upload.setAcceptedFileTypes(".pdf", ".jpg", ".jpeg", ".png");

        upload.setUploadHandler(new UploadHandler() {
            @Override
            public void handleUploadRequest(UploadEvent event) throws IOException {
                final String originalName = event.getFileName();
                try (InputStream in = event.getInputStream()) {
                    String rutaRelativa = fileStorageService.save(in, originalName);
                    getUI().ifPresent(ui -> ui.access(() -> solicitarDescripcionArchivo(originalName, descripcion -> {
                        if (descripcion == null || descripcion.isBlank()) {
                            Notification.show("La descripción es obligatoria.");
                            fileStorageService.delete(rutaRelativa);
                            return;
                        }
                        onConfirm.accept(rutaRelativa, originalName, descripcion.trim());
                    }, () -> {
                        fileStorageService.delete(rutaRelativa);
                        onCancelUpload.run();
                    })));
                } catch (IOException ex) {
                    getUI().ifPresent(ui -> ui.access(() -> Notification.show("Error al guardar: " + originalName)));
                    throw ex;
                }
            }
        });

        upload.addFileRejectedListener(e -> Notification.show("Archivo rechazado: " + e.getErrorMessage()));
        return upload;
    }

    // Diálogo para solicitar descripción del archivo
    private void solicitarDescripcionArchivo(String nombreArchivo, java.util.function.Consumer<String> onOk, Runnable onCancel) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Descripción del archivo");

        com.vaadin.flow.component.textfield.TextField desc = new com.vaadin.flow.component.textfield.TextField("Descripción");
        desc.setWidthFull();
        desc.setPlaceholder("Ej.: Laboratorio 2025-01, RX columna AP/LAT, etc.");
        desc.setMaxLength(300);

        Button guardar = new Button("Guardar", e -> {
            String val = Optional.ofNullable(desc.getValue()).orElse("").trim();
            if (val.isBlank()) {
                Notification.show("La descripción es obligatoria.");
                return;
            }
            onOk.accept(val);
            dialog.close();
        });
        Button cancelar = new Button("Cancelar", e -> {
            onCancel.run();
            dialog.close();
        });

        HorizontalLayout acciones = new HorizontalLayout(guardar, cancelar);
        dialog.add(new VerticalLayout(new Paragraph("Archivo: " + nombreArchivo), desc));
        dialog.getFooter().add(acciones);
        dialog.open();
    }

    // Confirmación genérica
    private void confirmarEliminar(String texto, Runnable onConfirm) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar");
        dialog.setText(texto);
        dialog.setConfirmText("Eliminar");
        dialog.setCancelText("Cancelar");
        dialog.addConfirmListener(e -> onConfirm.run());
        dialog.open();
    }

    @FunctionalInterface
    private interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}