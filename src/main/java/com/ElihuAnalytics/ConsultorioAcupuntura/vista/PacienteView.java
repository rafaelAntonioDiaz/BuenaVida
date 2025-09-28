package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.AutenticacionServicio;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.FileStorageService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.PacienteService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.util.Broadcaster;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.*;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "paciente", layout = LayoutPrincipal.class)
@PageTitle("Perfil del Paciente")
@RolesAllowed("PACIENTE")
public class PacienteView extends VerticalLayout {
    private final UI ui;
    // Para poder cancelar la suscripción cuando se cierre la vista
    private Registration broadcasterRegistration;
    private final HistoriaClinicaService historiaClinicaService;
    private final AutenticacionServicio autenticacionServicio;
    private final FileStorageService fileStorageService;
    private final SesionService sesionService;
    private final PacienteRepository pacienteRepositorio;
    private final PacienteService pacienteService;

    @Autowired
    public PacienteView(AutenticacionServicio auth,
                        HistoriaClinicaService historiaClinicaService,
                        FileStorageService fileStorageService,
                        SesionService sesionService,
                        PacienteRepository pacienteRepositorio,
                        PacienteService pacienteService) {
        this.ui = UI.getCurrent();
        this.autenticacionServicio = auth;
        this.historiaClinicaService = historiaClinicaService;
        this.fileStorageService = fileStorageService;
        this.sesionService = sesionService;
        this.pacienteRepositorio = pacienteRepositorio;
        this.pacienteService = pacienteService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        autenticacionServicio.getUsuarioAutenticado().ifPresentOrElse(usuario -> {
            if (usuario.getRol() == Rol.PACIENTE) {
                try {
                    Paciente pacienteActual = pacienteService.buscarPorUsuarioId(usuario.getId());
                    HistoriaClinica hc = historiaClinicaService.obtenerOCrearPorPacienteId(pacienteActual.getId());
                    construirDashboard(pacienteActual, hc);
                } catch (Exception ex) {
                    Notification.show("Error al cargar la información del paciente: " + ex.getMessage());
                    mostrarMensajeNoAutenticado();
                }
            } else {
                mostrarMensajeNoAutenticado();
            }
        }, this::mostrarMensajeNoAutenticado);

        // 🚀 Lanzamos la notificación dummy a los 5 segundos de entrar aquí
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Broadcaster.broadcast("📅 Tu cita fue confirmada por el médico ✅");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void construirDashboard(Paciente paciente, HistoriaClinica hc) {
        Div grid = new Div();
        grid.setWidthFull();
        grid.addClassName("paciente-grid");

        Component bienvenida = new BienvenidaCard(paciente);
        bienvenida.addClassName("card");
        bienvenida.getElement().getStyle().set("grid-area", "welcome");

        Component perfil = new PerfilFotoCard(paciente, fileStorageService, pacienteService);
        perfil.addClassName("card");
        perfil.getElement().getStyle().set("grid-area", "profile");

        Component planClinico = new PlanClinicoCard(hc);
        planClinico.addClassName("card");
        planClinico.getElement().getStyle().set("grid-area", "planClinico");

        Component agenda = new AgendaCard(paciente, sesionService);
        agenda.addClassName("card");
        agenda.getElement().getStyle().set("grid-area", "schedule");

        Component estados = new EstadosSaludCard(hc, historiaClinicaService, fileStorageService);
        estados.addClassName("card");
        estados.getElement().getStyle().set("grid-area", "estados");

        Component antecedentes = new AntecedentesCard(hc, historiaClinicaService, fileStorageService);
        antecedentes.addClassName("card");
        antecedentes.getElement().getStyle().set("grid-area", "antecedentes");

        grid.add(bienvenida, perfil, planClinico, agenda, estados, antecedentes);

        VerticalLayout wrapper = new VerticalLayout(grid);
        wrapper.setWidthFull();
        wrapper.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        wrapper.addClassName("paciente-view");

        add(wrapper);
    }

    private void mostrarMensajeNoAutenticado() {
        removeAll();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(
                new H3("No se ha autenticado ningún paciente"),
                new Button("Ir al Login", e -> UI.getCurrent().navigate("login"))
        );
    }

    /**
     * Método que se ejecuta cuando se adjunta la vista al UI (cuando el paciente abre la página).
     * Aquí registramos este paciente como "oyente" de notificaciones.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        // Nos guardamos el registration
        broadcasterRegistration = Broadcaster.register(this::mostrarConfirmacion);
    }

    /**
     * Cuando el paciente abandona la vista, eliminamos su listener
     * para evitar fugas de memoria.
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    /**
     * Método que recibe una confirmación desde el Broadcaster.
     * Muestra un aviso en pantalla (notificación de Vaadin + notificación nativa del navegador).
     */
    public void mostrarConfirmacion(String mensaje) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> {
                // Notificación dentro de la aplicación (Vaadin)
                Notification.show(mensaje, 5000, Notification.Position.MIDDLE);

                // Notificación nativa del navegador (tipo WhatsApp, Gmail, etc.)
                ui.getPage().executeJs(
                        "if (window.Notification && Notification.permission === 'granted') {" +
                                " new Notification($0);" +
                                "} else if (window.Notification && Notification.permission !== 'denied') {" +
                                " Notification.requestPermission().then(p => { if(p==='granted'){ new Notification($0);} });" +
                                "}", mensaje
                );
            });
        }
    }
}