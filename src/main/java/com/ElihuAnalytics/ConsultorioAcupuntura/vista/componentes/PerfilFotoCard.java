package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.FileStorageService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.PacienteService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.IOException;
import java.io.InputStream;

public class PerfilFotoCard extends Div {

    private final Paciente paciente;
    private final FileStorageService fileStorageService;
    private final PacienteService pacienteService;
    private final Avatar avatar;

    public PerfilFotoCard(Paciente paciente,
                          FileStorageService fileStorageService,
                          PacienteService pacienteService) {
        this.paciente = paciente;
        this.fileStorageService = fileStorageService;
        this.pacienteService = pacienteService;

        // Card base (equivalente a crearCard())
        getStyle()
                .set("padding", "var(--lumo-space-l)")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("display", "flex")
                .set("flexDirection", "column")
                .set("alignItems", "center")
                .set("gap", "0.25rem");

        // Contenedor del avatar con overlay sutil
        Div avatarBox = new Div();
        avatarBox.getStyle()
                .set("position", "relative")
                .set("width", "128px")
                .set("height", "128px")
                .set("display", "inline-flex")
                .set("alignItems", "center")
                .set("justifyContent", "center");

        avatar = new Avatar(paciente.getNombres() + " " + paciente.getApellidos());
        avatar.setWidth("128px");
        avatar.setHeight("128px");
        avatar.getElement().setAttribute("aria-label", "Foto de perfil");

        // Mostrar foto guardada (normalizada y con cache-busting)
        if (paciente.getRutaFotoPerfil() != null && !paciente.getRutaFotoPerfil().isBlank()) {
            String webUrl = toWebPath(paciente.getRutaFotoPerfil());
            avatar.setImage(cacheBust(webUrl));
        }

        // Botón de cámara minimalista como botón del Upload
        Icon camIcon = new Icon(VaadinIcon.CAMERA);
        camIcon.setSize("18px");
        Button btnCambiar = new Button(camIcon);
        btnCambiar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        btnCambiar.getStyle().set("padding", "2px");
        btnCambiar.getElement().setAttribute("aria-label", "Cambiar foto");
        btnCambiar.getElement().setProperty("title", "Cambiar foto");

        // Upload minimalista (solo botón, sin lista, sin dropzone)
        Upload upload = new Upload();
        upload.setDropAllowed(false);
        upload.setMaxFiles(1);
        upload.setAutoUpload(true);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp", "image/jpg");
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setUploadButton(btnCambiar);

        // Botón papelera minimalista para quitar
        Icon delIcon = new Icon(VaadinIcon.TRASH);
        delIcon.setSize("18px");
        Button btnQuitar = new Button(delIcon);
        btnQuitar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        btnQuitar.getStyle().set("padding", "2px");
        btnQuitar.getElement().setAttribute("aria-label", "Quitar foto");
        btnQuitar.getElement().setProperty("title", "Quitar foto");

        // Controles flotantes discretos
        Div controls = new Div(upload, btnQuitar);
        controls.getStyle()
                .set("position", "absolute")
                .set("right", "-6px")
                .set("bottom", "-6px")
                .set("display", "inline-flex")
                .set("gap", "2px")
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "999px")
                .set("padding", "2px 4px")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("opacity", "0.9");

        // Manejo de subida (misma lógica de guardado/persistencia)
        upload.setUploadHandler(new UploadHandler() {
            @Override
            public void handleUploadRequest(UploadEvent event) throws IOException {
                final String original = event.getFileName();
                try (InputStream in = event.getInputStream()) {
                    String rutaRelativa = fileStorageService.save(in, original);
                    String webUrl = toWebPath(rutaRelativa);

                    getUI().ifPresent(ui -> ui.access(() -> {
                        try {
                            pacienteService.actualizarRutaFotoPerfil(paciente.getId(), webUrl);
                            paciente.setRutaFotoPerfil(webUrl);
                            avatar.setImage(cacheBust(webUrl));
                            upload.clearFileList();
                            Notification.show("Foto actualizada");
                        } catch (Exception e) {
                            Notification.show("La imagen se guardó, pero no se pudo actualizar el perfil.");
                        }
                    }));
                } catch (IOException ex) {
                    getUI().ifPresent(ui -> ui.access(() ->
                            Notification.show("Error al guardar la imagen: " + original)
                    ));
                    throw ex;
                }
            }
        });
        upload.addFileRejectedListener(e -> Notification.show("Archivo rechazado: " + e.getErrorMessage()));

        // Acción "Quitar" con confirmación
        btnQuitar.addClickListener(e -> {
            ConfirmDialog dlg = new ConfirmDialog();
            dlg.setHeader("Quitar foto");
            dlg.setText("¿Deseas quitar tu foto de perfil?");
            dlg.setConfirmText("Quitar");
            dlg.setCancelText("Cancelar");
            dlg.addConfirmListener(ev -> {
                String rutaAnterior = paciente.getRutaFotoPerfil();
                if (rutaAnterior != null && !rutaAnterior.isBlank()) {
                    fileStorageService.delete(rutaAnterior);
                }
                pacienteService.actualizarRutaFotoPerfil(paciente.getId(), null);
                paciente.setRutaFotoPerfil(null);
                avatar.setImage(null);
                Notification.show("Foto de perfil quitada");
            });
            dlg.open();
        });

        // Pie de texto sutil
        Span caption = new Span("Foto de perfil");
        caption.getStyle()
                .set("fontSize", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        // Ensamblar
        avatarBox.add(avatar, controls);
        add(avatarBox, caption);
    }

    // Helpers locales (idénticos a los usados en la vista)
    private static String toWebPath(String ruta) {
        if (ruta == null || ruta.isBlank()) return ruta;
        String r = ruta.replace("\\", "/");
        if (r.startsWith("http://") || r.startsWith("https://") || r.startsWith("data:") || r.startsWith("/")) {
            return r;
        }
        return "/" + r;
    }

    // Agrega un parámetro de versión para forzar al navegador a recargar la imagen
    private static String cacheBust(String url) {
        if (url == null || url.isBlank()) return url;
        return url + (url.contains("?") ? "&" : "?") + "v=" + System.currentTimeMillis();
    }

    // Helper: fija imagen del avatar con cache-busting para evitar caché del navegador
    private void setAvatarImage(Avatar avatar, String ruta) {
        String cacheBuster = ruta.contains("?") ? "&v=" + System.currentTimeMillis() : "?v=" + System.currentTimeMillis();
        avatar.setImage(ruta + cacheBuster);
    }

    // Helper: convierte los bytes a data URL para previsualizar inmediatamente
    private String toBase64DataUrl(byte[] bytes, String originalName) {
        String mime = (originalName != null && originalName.toLowerCase().endsWith(".png")) ? "image/png" : "image/jpeg";
        String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
        return "data:" + mime + ";base64," + base64;
    }
}