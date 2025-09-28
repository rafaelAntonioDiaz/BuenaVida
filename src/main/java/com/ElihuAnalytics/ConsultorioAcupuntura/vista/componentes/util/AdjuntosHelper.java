package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.util;

import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.FileStorageService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Utilidades de UI para manejo de adjuntos: crear upload, pedir descripción,
 * confirmar eliminación y construir filas de adjunto.
 * Código extraído fielmente desde PacienteView para mantener el comportamiento.
 */
public class AdjuntosHelper {

    private final FileStorageService fileStorageService;

    public AdjuntosHelper(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Crea un Upload con guardado en FileStorageService y solicitud de descripción tras subir.
     * - onConfirm.accept(rutaGuardada, nombreOriginal, descripcion)
     * - onCancelUpload.run() si el usuario cancela o la descripción está vacía
     */
    public Upload crearUpload(Component context,
                              TriConsumer<String, String, String> onConfirm,
                              Runnable onCancelUpload) {
        Upload upload = new Upload();
        upload.setWidthFull();
        upload.setAutoUpload(true);
        upload.setMaxFiles(10);
        upload.setMaxFileSize(10 * 1024 * 1024);
        upload.setAcceptedFileTypes(".pdf", ".jpg", ".jpeg", ".png");

        upload.setUploadHandler(new UploadHandler() {
            @Override
            public void handleUploadRequest(UploadEvent event) throws IOException {
                final String originalName = event.getFileName();
                try (InputStream in = event.getInputStream()) {
                    String rutaRelativa = fileStorageService.save(in, originalName);
                    context.getUI().ifPresent(ui -> ui.access(() -> solicitarDescripcionArchivo(originalName, descripcion -> {
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
                    context.getUI().ifPresent(ui -> ui.access(() -> Notification.show("Error al guardar: " + originalName)));
                    throw ex;
                }
            }
        });

        upload.addFileRejectedListener(e -> Notification.show("Archivo rechazado: " + e.getErrorMessage()));
        return upload;
    }

    /**
     * Diálogo para pedir la descripción del archivo subido.
     */
    public static void solicitarDescripcionArchivo(String nombreArchivo, Consumer<String> onOk, Runnable onCancel) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Descripción del archivo");

        TextField desc = new TextField("Descripción");
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

    /**
     * Diálogo de confirmación simple.
     */
    public static void confirmarEliminar(String texto, Runnable onConfirm) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar");
        dialog.setText(texto);
        dialog.setConfirmText("Eliminar");
        dialog.setCancelText("Cancelar");
        dialog.addConfirmListener(e -> onConfirm.run());
        dialog.open();
    }

    /**
     * Fila de adjunto con enlace y botón Eliminar.
     */
    public static HorizontalLayout crearFilaAdjunto(String ruta, String descripcion, Runnable onEliminar) {
        Anchor enlace = new Anchor(ruta, descripcion);
        enlace.setTarget("_blank");
        Button eliminar = new Button("Eliminar", e -> onEliminar.run());
        eliminar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);

        HorizontalLayout fila = new HorizontalLayout(enlace, eliminar);
        fila.setWidthFull();
        fila.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return fila;
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}