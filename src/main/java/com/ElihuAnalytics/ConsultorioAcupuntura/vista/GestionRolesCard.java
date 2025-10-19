package com.ElihuAnalytics.ConsultorioAcupuntura.vista; // O com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.UsuarioAdminService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente reutilizable (Card) para gestionar los roles de los usuarios.
 */
@Component // Para que Spring pueda inyectar dependencias
@Scope("prototype") // Cada vez que se use, será una nueva instancia
public class GestionRolesCard extends VerticalLayout {

    private final UsuarioAdminService usuarioAdminService;
    private Grid<Usuario> gridPacientes = new Grid<>(Usuario.class, false);

    @Autowired
    public GestionRolesCard(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;

        addClassName("admin-card"); // Reutilizamos el estilo CSS
        H3 tituloPacientes = new H3("Gestión de Pacientes y Roles");

        configurarGrid();

        add(tituloPacientes, gridPacientes);
        setWidthFull(); // Ocupa el ancho disponible
        setVisible(false); // Oculto por defecto
    }

    private void configurarGrid() {
        gridPacientes.addColumn(Usuario::getNombres).setHeader("Nombre").setSortable(true);
        gridPacientes.addColumn(Usuario::getUsername).setHeader("Correo/Usuario").setSortable(true);
        gridPacientes.addColumn(u -> u.getRol().name()).setHeader("Rol Actual").setSortable(true);

        gridPacientes.addComponentColumn(usuario -> {
            if (usuario.getRol() == Rol.PACIENTE) {
                Button promoteBtn = new Button("Hacer Médico", VaadinIcon.ARROW_UP.create(),
                        e -> {
                            usuarioAdminService.cambiarRol(usuario.getId(), Rol.MEDICO);
                            refreshGrid(); // Actualiza este grid
                        });
                promoteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                return promoteBtn;
            } else if (usuario.getRol() == Rol.MEDICO) {
                Button demoteBtn = new Button("Hacer Paciente", VaadinIcon.ARROW_DOWN.create(),
                        e -> {
                            usuarioAdminService.cambiarRol(usuario.getId(), Rol.PACIENTE);
                            refreshGrid(); // Actualiza este grid
                        });
                demoteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                return demoteBtn;
            }
            return new Span("Admin");
        }).setHeader("Acciones");
        gridPacientes.setWidthFull();
    }

    /**
     * Carga o recarga los datos en el grid.
     * Este método debe ser llamado desde MedicoAdministradorView cuando se muestra el card.
     */
    public void refreshGrid() {
        if (isVisible()) { // Solo carga si está visible
            List<Usuario> usuarios = usuarioAdminService.listarTodosLosUsuarios();
            gridPacientes.setItems(usuarios);
        }
    }
}