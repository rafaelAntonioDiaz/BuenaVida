package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.agendamientoPaciente;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.button.Button;
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

        // Validar paciente
        if (paciente == null || paciente.getId() == null) {
            log.error("Paciente inválido recibido: id={}, username={}",
                    paciente != null ? paciente.getId() : "null",
                    paciente != null ? paciente.getUsername() : "null");
            throw new IllegalStateException("Paciente inválido: ID nulo o paciente no proporcionado");
        }

        Optional<Paciente> pacienteVerificado = pacienteRepository.findById(paciente.getId());
        if (pacienteVerificado.isEmpty()) {
            log.error("Paciente no encontrado en la base de datos: id={}, username={}",
                    paciente.getId(), paciente.getUsername());
            throw new IllegalStateException("Paciente no registrado en la base de datos");
        }

        this.paciente = pacienteVerificado.get();
        log.info("Paciente inicializado en AgendaCard: id={}, username={}",
                this.paciente.getId(), this.paciente.getUsername());

        setWidth("400px");
        setPadding(true);
        setSpacing(true);
        addClassName("card");
        addClassName("agenda-card");

        // 🔹 Título principal
        H3 titulo = new H3("Citas");
        titulo.addClassName("seccion-titulo");

        // 🔹 Botón que despliega/oculta el formulario
        Button btnToggleAgenda = new Button("Reserve su cita");
        btnToggleAgenda.addClassName("btn-primary");

        // 🔹 Crear subcomponentes (idénticos a la versión anterior)
        agendaForm = new AgendaForm(paciente, sesionService, notificacionService, pacienteRepository);
        calendarioMes = new CalendarioMes(paciente.getId(), sesionService, agendaForm.getFechaHoraPicker());
        citasLista = new CitasLista(paciente.getId(), sesionService, notificacionService);

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
