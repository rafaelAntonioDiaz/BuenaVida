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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Componente card para mostrar y gestionar la foto de perfil del paciente.
 * Permite subir, cambiar y eliminar la foto de perfil usando UploadHandler.
 */
public class PerfilFotoCard extends Div {

    private static final Logger log = LoggerFactory.getLogger(PerfilFotoCard.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final Paciente paciente;
    private final FileStorageService fileStorageService;
    private final PacienteService pacienteService;
    private final Avatar avatar;
    private final Upload upload;

    public PerfilFotoCard(Paciente paciente,
                          FileStorageService fileStorageService,
                          PacienteService pacienteService) {
        this.paciente = paciente;
        this.fileStorageService = fileStorageService;
        this.pacienteService = pacienteService;

        // Estilos del card base
        getStyle()
                .set("padding", "var(--lumo-space-l)")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "0.25rem");

        // Contenedor del avatar con controles
        Div avatarBox = new Div();
        avatarBox.getStyle()
                .set("position", "relative")
                .set("width", "128px")
                .set("height", "128px")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        // Avatar principal
        avatar = new Avatar(paciente.getNombres() + " " + paciente.getApellidos());
        avatar.setWidth("128px");
        avatar.setHeight("128px");
        avatar.getElement().setAttribute("aria-label", "Foto de perfil");

        // Cargar foto guardada si existe
        if (paciente.getRutaFotoPerfil() != null && !paciente.getRutaFotoPerfil().isBlank()) {
            String rutaLimpia = limpiarRutaWeb(paciente.getRutaFotoPerfil());
            log.info("Cargando foto de perfil: pacienteId={}, ruta={}", paciente.getId(), rutaLimpia);
            avatar.setImage(agregarCacheBuster(rutaLimpia));
        }

        // Configurar upload con UploadHandler
        upload = new Upload();
        upload.setDropAllowed(false);
        upload.setMaxFiles(1);
        upload.setAutoUpload(true);
        upload.setAcceptedFileTypes("image/jpeg", "image/jpg", "image/png", "image/webp");
        upload.setMaxFileSize((int) MAX_FILE_SIZE);

        // Botón de cámara minimalista
        Icon camIcon = VaadinIcon.CAMERA.create();
        camIcon.setSize("18px");
        Button btnCambiar = new Button(camIcon);
        btnCambiar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        btnCambiar.getStyle().set("padding", "2px");
        btnCambiar.getElement().setAttribute("aria-label", "Cambiar foto");
        btnCambiar.getElement().setProperty("title", "Cambiar foto");

        upload.setUploadButton(btnCambiar);

        // Configurar el UploadHandler para manejar la subida
        upload.setUploadHandler(new UploadHandler() {
            @Override
            public void handleUploadRequest(UploadEvent event) throws IOException {
                final String nombreArchivo = event.getFileName();
                log.info("Iniciando subida de foto: pacienteId={}, archivo={}", paciente.getId(), nombreArchivo);

                try (InputStream inputStream = event.getInputStream()) {
                    // Guardar el archivo y obtener la ruta web
                    String rutaWeb = fileStorageService.save(inputStream, nombreArchivo);
                    log.info("Archivo guardado: pacienteId={}, rutaWeb={}", paciente.getId(), rutaWeb);

                    // Ejecutar actualización en el hilo de UI
                    getUI().ifPresent(ui -> ui.access(() -> {
                        try {
                            // Eliminar foto anterior si existe
                            String rutaAnterior = paciente.getRutaFotoPerfil();
                            if (rutaAnterior != null && !rutaAnterior.isBlank()) {
                                log.info("Eliminando foto anterior: pacienteId={}, rutaAnterior={}",
                                        paciente.getId(), rutaAnterior);
                                fileStorageService.delete(rutaAnterior);
                            }

                            // Actualizar en base de datos
                            pacienteService.actualizarRutaFotoPerfil(paciente.getId(), rutaWeb);
                            paciente.setRutaFotoPerfil(rutaWeb);

                            // Actualizar la imagen en el avatar
                            String rutaLimpia = limpiarRutaWeb(rutaWeb);
                            avatar.setImage(agregarCacheBuster(rutaLimpia));

                            // Limpiar el upload
                            upload.clearFileList();

                            // Notificación de éxito
                            mostrarNotificacion("Foto de perfil actualizada correctamente", NotificationVariant.LUMO_SUCCESS);
                            log.info("Foto de perfil actualizada exitosamente: pacienteId={}, nuevaRuta={}",
                                    paciente.getId(), rutaWeb);

                        } catch (Exception ex) {
                            log.error("Error al actualizar la foto de perfil en BD: pacienteId={}, error={}",
                                    paciente.getId(), ex.getMessage(), ex);
                            mostrarNotificacion("Error al actualizar la foto: " + ex.getMessage(),
                                    NotificationVariant.LUMO_ERROR);
                        }
                    }));

                } catch (IOException ex) {
                    log.error("Error al guardar la foto de perfil: pacienteId={}, archivo={}, error={}",
                            paciente.getId(), nombreArchivo, ex.getMessage(), ex);

                    // Notificar error en el hilo de UI
                    getUI().ifPresent(ui -> ui.access(() -> {
                        mostrarNotificacion("Error al guardar la foto: " + ex.getMessage(),
                                NotificationVariant.LUMO_ERROR);
                    }));

                    throw ex;
                }
            }
        });

        // Listener para archivos rechazados
        upload.addFileRejectedListener(event -> {
            String mensaje = "Archivo rechazado: " + event.getErrorMessage();
            log.warn("Archivo rechazado: pacienteId={}, mensaje={}", paciente.getId(), mensaje);
            mostrarNotificacion(mensaje, NotificationVariant.LUMO_ERROR);
        });

        // Botón para eliminar foto
        Icon delIcon = VaadinIcon.TRASH.create();
        delIcon.setSize("18px");
        Button btnQuitar = new Button(delIcon);
        btnQuitar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        btnQuitar.getStyle().set("padding", "2px");
        btnQuitar.getElement().setAttribute("aria-label", "Quitar foto");
        btnQuitar.getElement().setProperty("title", "Quitar foto");

        // Controles flotantes
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

        // Acción de quitar foto con confirmación
        btnQuitar.addClickListener(e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Quitar foto de perfil");
            dialog.setText("¿Estás seguro de que deseas quitar tu foto de perfil?");
            dialog.setConfirmText("Quitar");
            dialog.setCancelText("Cancelar");
            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(ev -> {
                try {
                    String rutaAnterior = paciente.getRutaFotoPerfil();

                    // Eliminar archivo físico si existe
                    if (rutaAnterior != null && !rutaAnterior.isBlank()) {
                        log.info("Eliminando foto de perfil: pacienteId={}, ruta={}", paciente.getId(), rutaAnterior);
                        fileStorageService.delete(rutaAnterior);
                    }

                    // Actualizar en base de datos
                    pacienteService.actualizarRutaFotoPerfil(paciente.getId(), null);
                    paciente.setRutaFotoPerfil(null);

                    // Quitar imagen del avatar
                    avatar.setImage(null);

                    mostrarNotificacion("Foto de perfil eliminada", NotificationVariant.LUMO_SUCCESS);
                    log.info("Foto de perfil eliminada exitosamente: pacienteId={}", paciente.getId());

                } catch (Exception ex) {
                    log.error("Error al eliminar la foto de perfil: pacienteId={}, error={}",
                            paciente.getId(), ex.getMessage(), ex);
                    mostrarNotificacion("Error al eliminar la foto: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
                }
            });

            dialog.open();
        });

        // Texto descriptivo
        Span caption = new Span("Foto de perfil");
        caption.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        // Ensamblar componentes
        avatarBox.add(avatar, controls);
        add(avatarBox, caption);
    }

    /**
     * Limpia la ruta web eliminando parámetros de cache y normalizando el formato.
     *
     * @param ruta Ruta original
     * @return Ruta limpia sin parámetros de query
     */
    private String limpiarRutaWeb(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return ruta;
        }

        // Eliminar parámetros de query (?v=...)
        String rutaLimpia = ruta;
        if (rutaLimpia.contains("?")) {
            rutaLimpia = rutaLimpia.substring(0, rutaLimpia.indexOf("?"));
        }

        // Asegurar que empiece con /
        if (!rutaLimpia.startsWith("/") && !rutaLimpia.startsWith("http")) {
            rutaLimpia = "/" + rutaLimpia;
        }

        return rutaLimpia;
    }

    /**
     * Agrega un parámetro de cache buster para forzar la recarga de la imagen.
     * Esto evita que el navegador use versiones cacheadas de la foto.
     *
     * @param url URL base de la imagen
     * @return URL con parámetro de versión timestamp
     */
    private String agregarCacheBuster(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }

        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "v=" + System.currentTimeMillis();
    }

    /**
     * Muestra una notificación toast al usuario.
     *
     * @param mensaje Texto del mensaje a mostrar
     * @param variant Tipo de notificación (SUCCESS, ERROR, WARNING, etc.)
     */
    private void mostrarNotificacion(String mensaje, NotificationVariant variant) {
        Notification notification = new Notification(mensaje, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}