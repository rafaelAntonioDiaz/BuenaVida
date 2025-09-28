package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Route("demo-notificaciones")
@RolesAllowed("MEDICO")
public class DemoNotificacionesView extends VerticalLayout {

    private final NotificacionService notificacionService;
    private final SesionService sesionService;

    private Div logContainer;
    private Button testProgramadaBtn;
    private Span statusSpan;
    private int contadorSegundos = 10;

    public DemoNotificacionesView(NotificacionService notificacionService,
                                  SesionService sesionService) {
        this.notificacionService = notificacionService;
        this.sesionService = sesionService;

        initUI();
        cargarJavaScript();
    }

    private void initUI() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("800px");
        getStyle().set("margin", "0 auto");

        add(new H2("🔔 Demo Sistema de Notificaciones Nativas"));

        // Información sobre notificaciones nativas
        Div info = new Div();
        info.add("Las notificaciones nativas son GRATIS y aparecen directamente en tu sistema operativo. " +
                "No requieren servicios externos ni costos adicionales.");
        info.getStyle()
                .set("background", "var(--lumo-success-color-10pct)")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("border-left", "4px solid var(--lumo-success-color)");
        add(info);

        // Sección de pruebas inmediatas
        add(new H3("Pruebas Inmediatas"));

        Button testCompleto = new Button("🚀 Test Notificación Completa");
        testCompleto.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        testCompleto.addClickListener(e -> testearNotificacionCompleta());

        Button testNativa = new Button("💻 Test Solo Nativa");
        testNativa.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        testNativa.addClickListener(e -> testearSoloNativa());

        HorizontalLayout botonesInmediatos = new HorizontalLayout(testCompleto, testNativa);
        botonesInmediatos.setSpacing(true);
        add(botonesInmediatos);

        // Sección de pruebas programadas
        add(new H3("Pruebas Programadas"));

        testProgramadaBtn = new Button("⏰ Programar para 10 segundos");
        testProgramadaBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        testProgramadaBtn.addClickListener(e -> testearNotificacionProgramada());
        testProgramadaBtn.setId("testProgramadaBtn"); // Añadir ID único

        statusSpan = new Span();
        statusSpan.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)")
                .set("margin-left", "var(--lumo-space-m)");
        statusSpan.setId("statusSpan"); // Añadir ID único

        HorizontalLayout programadaLayout = new HorizontalLayout(testProgramadaBtn, statusSpan);
        programadaLayout.setAlignItems(Alignment.CENTER);
        add(programadaLayout);

        // Contenedor de logs
        add(new H3("📋 Log de Actividad"));
        logContainer = new Div();
        logContainer.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("max-height", "300px")
                .set("overflow-y", "auto")
                .set("font-family", "monospace")
                .set("font-size", "0.875rem");

        add(logContainer);

        // Botón para limpiar logs
        Button limpiarBtn = new Button("🗑️ Limpiar Log");
        limpiarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        limpiarBtn.addClickListener(e -> limpiarLog());
        add(limpiarBtn);

        agregarLog("🟢 Sistema iniciado - Notificaciones nativas listas");
    }

    private void cargarJavaScript() {
        getUI().ifPresent(ui -> {
            ui.getPage().addJavaScript("/js/notification-service.js");
            agregarLog("📱 JavaScript cargado");

            // Solicitar permisos automáticamente
            ui.getPage().executeJs(
                    "setTimeout(() => {" +
                            "  if (window.requestNotificationPermission) {" +
                            "    window.requestNotificationPermission();" +
                            "  }" +
                            "}, 1000);"
            );
        });
    }

    private void testearNotificacionCompleta() {
        agregarLog("🚀 Iniciando test completo...");

        Sesion sesionTest = crearSesionPrueba(LocalDateTime.now().plusHours(2));
        agregarLog("📅 Sesión creada para: " + sesionTest.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        try {
            notificacionService.enviarRecordatorioPaciente(sesionTest);
            agregarLog("✅ Notificación completa enviada");

            Notification.show("✅ Notificación completa enviada", 3000, Notification.Position.TOP_CENTER);
        } catch (Exception e) {
            agregarLog("❌ Error: " + e.getMessage());
            Notification.show("❌ Error al enviar notificación", 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void testearSoloNativa() {
        agregarLog("💻 Enviando notificación nativa inmediata...");

        getUI().ifPresent(ui -> {
            ui.getPage().executeJs(
                    "if (window.showAppointmentNotification) {" +
                            "  console.log('Enviando notificación nativa...');" +
                            "  window.showAppointmentNotification(" +
                            "    '999', " +
                            "    '26/08/2024 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "', " +
                            "    'Paciente Test', " +
                            "    'Consulta de prueba - Inmediata'" +
                            "  );" +
                            "} else {" +
                            "  console.error('showAppointmentNotification no disponible');" +
                            "  alert('Error: Función de notificación no disponible');" +
                            "}"
            );
        });

        agregarLog("📤 Comando de notificación enviado");
        Notification.show("💻 Notificación nativa enviada", 2000, Notification.Position.BOTTOM_CENTER);
    }

    private void testearNotificacionProgramada() {
        agregarLog("⏰ Programando notificación para 10 segundos...");

        // Resetear contador
        contadorSegundos = 10;

        // Cambiar estado inicial del botón y span
        testProgramadaBtn.setEnabled(false);
        testProgramadaBtn.setText("⏳ Programada...");
        statusSpan.setText("⏱️ Esperando " + contadorSegundos + " segundos...");

        Sesion sesionTest = crearSesionPrueba(LocalDateTime.now().plusSeconds(10));

        // Iniciar el contador visual
        iniciarContadorVisual();

        // Programar la notificación
        programarNotificacion(sesionTest);
    }

    private void iniciarContadorVisual() {
        agregarLog("⏱️ Iniciando contador regresivo de 10 segundos...");

        getUI().ifPresent(ui -> {
            ui.getPage().executeJs(
                    "let contador = 10;" +
                            "const intervalo = setInterval(() => {" +
                            "  contador--;" +
                            "  const statusElement = document.getElementById('statusSpan');" +
                            "  if (statusElement && contador > 0) {" +
                            "    statusElement.textContent = '⏱️ Esperando ' + contador + ' segundos...';" +
                            "    // Log cada 3 segundos para no saturar" +
                            "    if (contador === 7 || contador === 4 || contador === 1) {" +
                            "      $0.$server.agregarLogDesdeJS('⏳ Faltan ' + contador + ' segundos...');" +
                            "    }" +
                            "  } else if (statusElement && contador === 0) {" +
                            "    statusElement.textContent = '🚀 ¡Enviando notificación!';" +
                            "    $0.$server.agregarLogDesdeJS('🚀 ¡Enviando notificación ahora!');" +
                            "  }" +
                            "  if (contador <= 0) {" +
                            "    clearInterval(intervalo);" +
                            "  }" +
                            "}, 1000);", getElement()
            );
        });
    }

    private void programarNotificacion(Sesion sesionTest) {
        agregarLog("📋 Programando notificación JavaScript...");

        getUI().ifPresent(ui -> {
            ui.getPage().executeJs(
                    "console.log('Programando notificación para 10 segundos...');" +
                            "setTimeout(() => {" +
                            "  console.log('Ejecutando notificación programada...');" +
                            "  $0.$server.agregarLogDesdeJS('🔔 Ejecutando notificación programada...');" +
                            "  if (window.showAppointmentNotification) {" +
                            "    window.showAppointmentNotification(" +
                            "      '" + sesionTest.getId() + "'," +
                            "      '" + sesionTest.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "'," +
                            "      '" + sesionTest.getPaciente().getNombres() + "'," +
                            "      '" + sesionTest.getMotivo() + "'" +
                            "    );" +
                            "    console.log('Notificación programada ejecutada');" +
                            "    $0.$server.agregarLogDesdeJS('✅ Notificación nativa enviada exitosamente');" +
                            "    " +
                            "    // Actualizar el estado después de enviar" +
                            "    const statusElement = document.getElementById('statusSpan');" +
                            "    if (statusElement) {" +
                            "      statusElement.textContent = '✅ Notificación enviada!';" +
                            "    }" +
                            "  } else {" +
                            "    $0.$server.agregarLogDesdeJS('❌ Error: showAppointmentNotification no disponible');" +
                            "  }" +
                            "}, 10000);", getElement()
            );
        });

        // Reactivar botón después de 12 segundos
        programarReactivacionBoton();
    }

    private void programarReactivacionBoton() {
        getUI().ifPresent(ui -> {
            ui.getPage().executeJs(
                    "setTimeout(() => {" +
                            "  const btn = document.getElementById('testProgramadaBtn');" +
                            "  const status = document.getElementById('statusSpan');" +
                            "  " +
                            "  $0.$server.agregarLogDesdeJS('🔄 Reactivando botón para nueva prueba');" +
                            "  " +
                            "  if (btn) {" +
                            "    btn.disabled = false;" +
                            "    btn.textContent = '⏰ Programar para 10 segundos';" +
                            "  }" +
                            "  " +
                            "  if (status) {" +
                            "    status.textContent = '✅ Completado - Listo para nueva prueba';" +
                            "    setTimeout(() => {" +
                            "      if (status) status.textContent = '';" +
                            "      $0.$server.agregarLogDesdeJS('🏁 Proceso completado. Sistema listo.');" +
                            "    }, 3000);" +
                            "  }" +
                            "}, 12000);", getElement()
            );
        });
    }

    // Método para recibir logs desde JavaScript
    @com.vaadin.flow.component.ClientCallable
    public void agregarLogDesdeJS(String mensaje) {
        getUI().ifPresent(ui -> ui.access(() -> {
            agregarLog(mensaje);
        }));
    }

    private void agregarLog(String mensaje) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Div logEntry = new Div();
        logEntry.setText("[" + timestamp + "] " + mensaje);
        logEntry.getStyle().set("margin-bottom", "0.25rem");

        logContainer.add(logEntry);

        // Auto-scroll al final
        getUI().ifPresent(ui -> {
            ui.getPage().executeJs(
                    "setTimeout(() => {" +
                            "  const logs = document.querySelectorAll('div');" +
                            "  const logContainer = Array.from(logs).find(div => " +
                            "    div.style.fontFamily === 'monospace'" +
                            "  );" +
                            "  if (logContainer) logContainer.scrollTop = logContainer.scrollHeight;" +
                            "}, 100)"
            );
        });
    }

    private void limpiarLog() {
        logContainer.removeAll();
        agregarLog("🧹 Log limpiado");
    }

    private Sesion crearSesionPrueba(LocalDateTime fecha) {
        Sesion sesion = new Sesion();
        sesion.setId(System.currentTimeMillis());
        sesion.setFecha(fecha);
        sesion.setMotivo("Consulta de prueba - " + fecha.format(DateTimeFormatter.ofPattern("HH:mm")));
        sesion.setEstado(Sesion.EstadoSesion.PROGRAMADA);

        Paciente paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNombres("Paciente Demo");
        sesion.setPaciente(paciente);

        return sesion;
    }
}