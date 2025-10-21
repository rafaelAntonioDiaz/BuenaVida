package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Componente principal para agendar y reprogramar citas.
 * Contiene:
 *  - Un botón para desplegar el formulario de agendamiento.
 *  - Un calendario del mes actual.
 *  - Una lista de citas próximas.
 *
 * La funcionalidad se mantiene idéntica a la versión anterior:
 * se usa AgendaForm, CalendarioMes y CitasLista con sus callbacks.
 */
@CssImport(value = "./styles/global-theme.css")
@CssImport(value = "./styles/vaadin-components.css")
@CssImport(value = "./styles/vaadin-overrides.css")
public class AgendaCard extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AgendaCard.class);

    private final Paciente paciente;
    private final SesionService sesionService;
    private final NotificacionService notificacionService;
    private final PacienteRepository pacienteRepository;

    private final AgendaForm agendaForm;
    private final CalendarioMes calendarioMes;
    private final CitasLista citasLista;

    @Autowired
    public AgendaCard(Paciente paciente,
                      SesionService sesionService,
                      NotificacionService notificacionService,
                      PacienteRepository pacienteRepository) {

        this.sesionService = sesionService;
        this.notificacionService = notificacionService;
        this.pacienteRepository = pacienteRepository;

        // --- Validación Paciente (se mantiene) ---
        if (paciente == null || paciente.getId() == null) {
            log.error("Paciente inválido");
            throw new IllegalStateException("Paciente inválido");
        }
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(paciente.getId());
        if (pacienteOpt.isEmpty()) {
            log.error("Paciente ID {} no en DB", paciente.getId());
            throw new IllegalStateException("Paciente no registrado");
        }
        this.paciente = pacienteOpt.get();
        log.info("AgendaCard inicializada para Paciente ID {}",
        this.paciente.getId());
        // --- Fin Validación ---

        setWidth("100%"); // Hacemos que ocupe más ancho
        setMaxWidth("500px"); // Limitamos el ancho máximo
        setPadding(true);
        setSpacing(true);
        addClassName("card");
        addClassName("agenda-card");

        H3 titulo = new H3("Agendar Cita");
        titulo.addClassName("seccion-titulo");

        Button btnToggleAgenda = new Button("Mostrar/Ocultar Agendamiento");
        btnToggleAgenda.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // --- INICIO DE LA CORRECCIÓN ---
        // 1. Crear AgendaForm PRIMERO
        agendaForm = new AgendaForm(this.paciente, sesionService, notificacionService, pacienteRepository);

        // 2. Crear CalendarioMes, pasando los MÉTODOS de AgendaForm como listeners
        calendarioMes = new CalendarioMes(this.paciente.getId(), sesionService, agendaForm::setFechaSeleccionada, agendaForm::actualizarHorasDisponibles);

        // 3. Crear CitasLista (sin cambios)
        citasLista = new CitasLista(this.paciente.getId(), sesionService, notificacionService);
        // --- FIN DE LA CORRECCIÓN ---

        // 🔹 Registrar callback de actualización
        agendaForm.onAgendarSuccess(() -> {
            calendarioMes.recargarSesionesMes();
            citasLista.actualizarCitas();
            Notification.show("Cita agendada, hasta entonces.");
        });

        // 🔹 Contenedor colapsable para el formulario y calendario
        Div contenedorAgenda = new Div(calendarioMes, agendaForm);
        contenedorAgenda.addClassName("agenda-contenedor");
        contenedorAgenda.setVisible(false);

        // 🔹 Lógica de mostrar/ocultar con animación CSS
        btnToggleAgenda.addClickListener(e -> {
            boolean visible = !contenedorAgenda.isVisible();
            contenedorAgenda.setVisible(visible);

            if (visible) {
                contenedorAgenda.getElement().setAttribute("visible", "");
            } else {
                contenedorAgenda.getElement().removeAttribute("visible");
            }
        });


        // 🔹 Lista de próximas citas (siempre visible)
        citasLista.addClassName("lista-citas");

        // 🔹 Ensamblar layout
        add(titulo, btnToggleAgenda, contenedorAgenda, citasLista);
    }
}