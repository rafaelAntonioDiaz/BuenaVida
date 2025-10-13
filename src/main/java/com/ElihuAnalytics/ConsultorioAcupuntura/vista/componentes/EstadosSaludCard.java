package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.SeguimientoSalud;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.FileStorageService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
import com.vaadin.flow.component.UI;
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
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.util.AdjuntosHelper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Card de "Estados de salud" (seguimientos).
 * - Orden: más reciente arriba (desc), más antiguo abajo.
 * - Fecha con hora.
 * - Edición/adjuntos: solo el más reciente y solo durante las primeras 48 horas.
 */
public class EstadosSaludCard extends Div {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final HistoriaClinicaService historiaClinicaService;
    private final FileStorageService fileStorageService;
    private final HistoriaClinica hc;
    private final AdjuntosHelper adjuntosHelper;

    public EstadosSaludCard(HistoriaClinica hc,
                            HistoriaClinicaService historiaClinicaService,
                            FileStorageService fileStorageService) {
        this.hc = hc;
        this.historiaClinicaService = historiaClinicaService;
        this.fileStorageService = fileStorageService;
        this.adjuntosHelper = new AdjuntosHelper(fileStorageService);

        addClassName("card");
        addClassName("card--compact");
        setWidthFull();
        getStyle().set("min-width", "0");
        getElement().getStyle().set("--lumo-text-field-size", "var(--lumo-size-s)");
        getElement().getStyle().set("--lumo-button-size", "var(--lumo-size-s)");

        H3 titulo = new H3("Cómo he estado");
        titulo.getStyle().set("margin-top", "0");
        add(titulo);

        TextArea areaNuevo = new TextArea("Nuevo estado");
        areaNuevo.setWidthFull();
        areaNuevo.addThemeVariants(com.vaadin.flow.component.textfield.TextAreaVariant.LUMO_SMALL);
        areaNuevo.setMaxLength(10000);
        areaNuevo.setPlaceholder(
                "Describe cómo te sientes, síntomas, cambios, apetito, idas al baño, sueño y todo lo que se te ocurra incluso los sueños."
        );
        areaNuevo.setMaxHeight("12rem");

        Button crearBtn = new Button("Guardar estado", e -> {
            String desc = Optional.ofNullable(areaNuevo.getValue()).orElse("").trim();
            if (desc.isBlank()) {
                Notification.show("Por favor escribe una descripción.");
                return;
            }
            SeguimientoSalud seg = new SeguimientoSalud();
            seg.setDescripcion(desc);
            seg.setHistoriaClinica(hc);
            hc.getSeguimientos().add(seg);
            historiaClinicaService.guardar(hc);
            Notification.show("Estado creado");
            areaNuevo.clear();
            UI.getCurrent().getPage().reload();
        });
        crearBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        VerticalLayout composer = new VerticalLayout(areaNuevo, crearBtn);
        composer.setPadding(false);
        composer.setSpacing(false);
        composer.setWidthFull();
        composer.getStyle().set("gap", "var(--lumo-space-s)");
        add(composer);

        VerticalLayout lista = new VerticalLayout();
        lista.setPadding(false);
        lista.setSpacing(false);
        lista.setWidthFull();
        lista.getStyle().set("gap", "var(--space-responsive)");

        if (hc.getSeguimientos() == null || hc.getSeguimientos().isEmpty()) {
            lista.add(new Paragraph("Aún no hay registros. Crea tu primer estado arriba."));
        } else {
            List<SeguimientoSalud> ordenados = hc.getSeguimientos().stream()
                    .sorted((a, b) -> {
                        var fa = a.getFecha();
                        var fb = b.getFecha();
                        if (fa == null && fb == null) return 0;
                        if (fa == null) return 1;
                        if (fb == null) return -1;
                        return fb.compareTo(fa);
                    })
                    .collect(Collectors.toList());

            boolean esPrimero = true;
            LocalDateTime ahora = LocalDateTime.now();

            for (SeguimientoSalud seg : ordenados) {
                if (seg.getDescripcionesPorRuta() == null) seg.setDescripcionesPorRuta(new LinkedHashMap<>());
                if (seg.getArchivos() == null) seg.setArchivos(new ArrayList<>());

                Div item = new Div();
                item.setWidthFull();
                item.getStyle()
                        .set("padding", "var(--lumo-space-s)")
                        .set("border", "1px solid var(--lumo-contrast-10pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("display", "grid")
                        .set("gap", "var(--lumo-space-s)")
                        .set("min-width", "0");

                LocalDateTime fecha = seg.getFecha();
                String fechaTexto = Optional.ofNullable(fecha)
                        .map(dt -> dt.format(FORMATO_FECHA))
                        .orElse("Sin fecha");
                H4 head = new H4(fechaTexto);
                head.getStyle().set("margin-top", "0").set("margin-bottom", "0");

                Paragraph texto = new Paragraph(Optional.ofNullable(seg.getDescripcion()).orElse("Sin descripción"));
                texto.getStyle().set("margin", "0");

                boolean editableReciente = esPrimero && fecha != null && Duration.between(fecha, ahora).toHours() < 48;

                item.add(head, texto);

                if (editableReciente) {
                    Button editar = new Button("Editar", e -> abrirDialogoEditarSeguimiento(seg, texto));
                    editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
                    item.add(editar);
                } else if (esPrimero) {
                    Paragraph aviso = new Paragraph("Este estado ya no se puede modificar (superó 48 horas).");
                    aviso.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin", "0");
                    item.add(aviso);
                }

                VerticalLayout listaAdjuntos = new VerticalLayout();
                listaAdjuntos.setPadding(false);
                listaAdjuntos.setSpacing(false);
                listaAdjuntos.setWidthFull();
                listaAdjuntos.getStyle().set("gap", "var(--lumo-space-s)");

                if (editableReciente) {
                    refrescarListaAdjuntosConEliminar(seg, listaAdjuntos);
                    Upload upload = adjuntosHelper.crearUpload(this, (ruta, originalName, descripcionArchivo) -> {
                        seg.getDescripcionesPorRuta().put(ruta, descripcionArchivo);
                        if (!seg.getArchivos().contains(ruta)) seg.getArchivos().add(ruta);
                        historiaClinicaService.guardar(hc);
                        refrescarListaAdjuntosConEliminar(seg, listaAdjuntos);
                        Notification.show("Adjunto agregado");
                    }, () -> Notification.show("Subida cancelada"));
                    upload.getElement().getStyle().set("--lumo-button-size", "var(--lumo-size-s)");
                    item.add(listaAdjuntos, new H5("Agregar adjuntos a este estado"), upload);
                } else {
                    refrescarListaAdjuntosSoloLinks(seg, listaAdjuntos);
                    item.add(listaAdjuntos);
                }

                lista.add(item);
                esPrimero = false;
            }
        }
        add(lista);
    }

    private void abrirDialogoEditarSeguimiento(SeguimientoSalud seg, Paragraph pTexto) {
        Dialog dlg = new Dialog();
        String fechaTitulo = Optional.ofNullable(seg.getFecha())
                .map(dt -> dt.format(FORMATO_FECHA))
                .orElse("Sin fecha");
        dlg.setHeaderTitle("Editar estado del " + fechaTitulo);

        TextArea area = new TextArea("Descripción");
        area.setWidthFull();
        area.addThemeVariants(com.vaadin.flow.component.textfield.TextAreaVariant.LUMO_SMALL);
        area.setValue(Optional.ofNullable(seg.getDescripcion()).orElse(""));
        area.setMaxLength(10000);
        area.setMaxHeight("12rem");

        Button guardar = new Button("Guardar", e -> {
            seg.setDescripcion(Optional.ofNullable(area.getValue()).orElse("").trim());
            historiaClinicaService.guardar(hc);
            pTexto.setText(Optional.ofNullable(seg.getDescripcion()).orElse("Sin descripción"));
            Notification.show("Estado actualizado");
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

    private void refrescarListaAdjuntosSoloLinks(SeguimientoSalud seg, VerticalLayout listaAdjuntos) {
        listaAdjuntos.removeAll();
        Map<String, String> mapa = seg.getDescripcionesPorRuta();

        if (mapa != null && !mapa.isEmpty()) {
            mapa.forEach((ruta, descripcion) -> listaAdjuntos.add(crearFilaAdjuntoSoloLink(ruta, descripcion)));
        } else if (seg.getArchivos() != null && !seg.getArchivos().isEmpty()) {
            seg.getArchivos().forEach(ruta -> listaAdjuntos.add(crearFilaAdjuntoSoloLink(ruta, "Ver archivo adjunto")));
        } else {
            listaAdjuntos.add(new Paragraph("Sin adjuntos."));
        }
    }

    private HorizontalLayout crearFilaAdjuntoSoloLink(String ruta, String descripcion) {
        Anchor enlace = new Anchor(ruta, descripcion);
        enlace.setTarget("_blank");
        enlace.addClassName("adjunto-link"); // para CSS
        enlace.getStyle()
                .remove("word-break")
                .remove("white-space");

        HorizontalLayout fila = new HorizontalLayout(enlace);
        fila.addClassName("fila-adjunto"); // para CSS
        fila.setWidthFull();
        fila.setSpacing(false);
        fila.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        fila.getStyle()
                .set("gap", "var(--lumo-space-s)")
                .set("min-width", "0");
        return fila;
    }

    private void refrescarListaAdjuntosConEliminar(SeguimientoSalud seg, VerticalLayout listaAdjuntos) {
        listaAdjuntos.removeAll();
        Map<String, String> mapa = seg.getDescripcionesPorRuta();
        if (mapa != null && !mapa.isEmpty()) {
            mapa.forEach((ruta, descripcion) -> listaAdjuntos.add(crearFilaAdjuntoConEliminar(ruta, descripcion, () -> {
                confirmarEliminar("¿Eliminar este adjunto del seguimiento?", () -> {
                    seg.getDescripcionesPorRuta().remove(ruta);
                    if (seg.getArchivos() != null) seg.getArchivos().removeIf(r -> Objects.equals(r, ruta));
                    fileStorageService.delete(ruta);
                    historiaClinicaService.guardar(hc);
                    refrescarListaAdjuntosConEliminar(seg, listaAdjuntos);
                    Notification.show("Adjunto eliminado");
                });
            })));
        } else if (seg.getArchivos() != null && !seg.getArchivos().isEmpty()) {
            seg.getArchivos().forEach(ruta -> listaAdjuntos.add(crearFilaAdjuntoConEliminar(ruta, "Ver archivo adjunto", () -> {
                confirmarEliminar("¿Eliminar este adjunto del seguimiento?", () -> {
                    seg.getArchivos().removeIf(r -> Objects.equals(r, ruta));
                    fileStorageService.delete(ruta);
                    historiaClinicaService.guardar(hc);
                    refrescarListaAdjuntosConEliminar(seg, listaAdjuntos);
                    Notification.show("Adjunto eliminado");
                });
            })));
        } else {
            listaAdjuntos.add(new Paragraph("Sin adjuntos."));
        }
    }

    private HorizontalLayout crearFilaAdjuntoConEliminar(String ruta, String descripcion, Runnable onEliminar) {
        Anchor enlace = new Anchor(ruta, descripcion);
        enlace.setTarget("_blank");
        enlace.addClassName("adjunto-link");
        enlace.getStyle()
                .remove("word-break")
                .remove("white-space");

        Button eliminar = new Button("Eliminar", e -> onEliminar.run());
        eliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);

        HorizontalLayout fila = new HorizontalLayout(enlace, eliminar);
        fila.addClassName("fila-adjunto");
        fila.setWidthFull();
        fila.setSpacing(false);
        fila.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        fila.getStyle()
                .set("gap", "var(--lumo-space-s)")
                .set("min-width", "0");
        return fila;
    }

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