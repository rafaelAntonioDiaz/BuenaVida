package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

// Importa los nuevos componentes si los pusiste en otro paquete
// import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.GestionBlogCard;
// import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.GestionRolesCard;

import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.AutenticacionServicio;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired; // <-- Necesario para inyectar los cards

import java.util.Optional;

@Route(value = "medico", layout = LayoutPrincipal.class)
@PageTitle("Médico")
@RolesAllowed({"ADMINISTRADOR", "MEDICO"})
@CssImport(value = "./styles/global-theme.css")
@CssImport(value = "./styles/vaadin-components.css")
@CssImport(value = "./styles/vaadin-overrides.css")
@CssImport("./styles/medico-admin.css")
public class MedicoAdministradorView extends VerticalLayout {

    private final AutenticacionServicio auth;
    private final GestionRolesCard cardPacientes; // <-- Instancia del nuevo componente
    private final GestionBlogCard cardBlog;       // <-- Instancia del nuevo componente

    // Los servicios ya no se inyectan aquí, sino en los componentes Card

    @Autowired // <-- Importante para que Spring inyecte los componentes Card
    public MedicoAdministradorView(AutenticacionServicio auth,
                                   GestionRolesCard cardPacientes, // <-- Inyecta el Card
                                   GestionBlogCard cardBlog) {     // <-- Inyecta el Card
        this.auth = auth;
        this.cardPacientes = cardPacientes;
        this.cardBlog = cardBlog;

        addClassName("medico-admin-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // --- Saludo ---
        String saludo = auth.getUsuarioAutenticado()
                .map(u -> "Querido " + Optional.ofNullable(u.getNombres()).orElse("Doctor") + " 👋")
                .orElse("Hola, Doctor 👋");
        H2 tituloSaludo = new H2(saludo);
        tituloSaludo.getStyle().set("margin-top", "0");
        Paragraph subtitulo = new Paragraph("¿Qué vamos a hacer hoy?");

        // --- Botones de Navegación ---
        Button btnGestionarCitas = new Button("Gestionar Citas", VaadinIcon.CALENDAR_CLOCK.create(),
                e -> UI.getCurrent().navigate("medico/citas"));
        btnGestionarCitas.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnHistoriasClinicas = new Button("Actualizar Historias Clínicas", VaadinIcon.EDIT.create(),
                e -> UI.getCurrent().navigate("medico/tratamiento"));
        btnHistoriasClinicas.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout botonesNavegacion = new HorizontalLayout(btnGestionarCitas, btnHistoriasClinicas);
        botonesNavegacion.setSpacing(true);

        // --- Botones para Mostrar/Ocultar Cards ---
        Button btnTogglePacientes = new Button("Gestionar Roles", VaadinIcon.USERS.create());
        Button btnToggleBlog = new Button("Gestionar Blog", VaadinIcon.PENCIL.create());
        HorizontalLayout botonesGestion = new HorizontalLayout(btnTogglePacientes, btnToggleBlog);
        botonesGestion.setSpacing(true);

        // --- Lógica para mostrar/ocultar y refrescar los cards ---
        btnTogglePacientes.addClickListener(e -> {
            boolean visible = !cardPacientes.isVisible();
            cardPacientes.setVisible(visible);
            cardBlog.setVisible(false); // Oculta el otro card
            btnTogglePacientes.setText(visible ? "Ocultar Gestión Roles" : "Gestionar Roles");
            btnToggleBlog.setText("Gestionar Blog");
            if (visible) cardPacientes.refreshGrid(); // Llama al método público del Card
        });

        btnToggleBlog.addClickListener(e -> {
            boolean visible = !cardBlog.isVisible();
            cardBlog.setVisible(visible);
            cardPacientes.setVisible(false); // Oculta el otro card
            btnToggleBlog.setText(visible ? "Ocultar Gestión Blog" : "Gestionar Blog");
            btnTogglePacientes.setText("Gestionar Roles");
            if (visible) cardBlog.refreshGrid(); // Llama al método público del Card
        });

        // Añadir TODO al layout principal
        // Los cards ya están ocultos por defecto en sus propias clases
        add(tituloSaludo, subtitulo, botonesNavegacion, botonesGestion, cardPacientes, cardBlog);

        // Ya no necesitamos llamar a refreshGrid aquí, se hace al mostrar cada card
    }

    // --- SE HAN ELIMINADO TODOS LOS MÉTODOS PRIVADOS ---
    // (crearCard..., configurarGrid..., refreshGrid..., guardar..., eliminar...)
    // ¡La vista principal ahora es mucho más limpia!
}