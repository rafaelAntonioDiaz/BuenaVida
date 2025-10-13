package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.UsuarioRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.CodigoVerificacionRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route("verificar")
@PageTitle("Verificación de Código")
@AnonymousAllowed
@CssImport(value = "./styles/global-theme.css")
@CssImport(value = "./styles/vaadin-components.css")
@CssImport(value = "./styles/vaadin-overrides.css")
public class VerificarCodigoView extends VerticalLayout {

    private final UsuarioRepository usuarioRepository;
    private final CodigoVerificacionRepository codigoVerificacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public VerificarCodigoView(
            UsuarioRepository usuarioRepository,
            CodigoVerificacionRepository codigoVerificacionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.codigoVerificacionRepository = codigoVerificacionRepository;
        this.passwordEncoder = passwordEncoder;

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setSizeFull();

        H1 titulo = new H1("Verificación de Cuenta");

        EmailField emailField = new EmailField("Correo electrónico");
        emailField.setPlaceholder("usuario@correo.com");

        TextField codigoField = new TextField("Código de verificación");
        codigoField.setPlaceholder("123456");

        PasswordField passwordField = new PasswordField("Crear contraseña");
        passwordField.setPlaceholder("Mínimo 6 caracteres");

        ComboBox<Rol> rolComboBox = new ComboBox<>("Tipo de cuenta");
        rolComboBox.setItems(Rol.PACIENTE, Rol.MEDICO);
        rolComboBox.setItemLabelGenerator(Rol::name);
        rolComboBox.setPlaceholder("Seleccione un rol");

        Button verificarBtn = new Button("Verificar y Crear Cuenta");

        verificarBtn.addClickListener(event -> {
            String email = emailField.getValue().trim();
            String codigo = codigoField.getValue().trim();
            String clave = passwordField.getValue().trim();
            Rol rolSeleccionado = rolComboBox.getValue();

            if (email.isEmpty() || codigo.isEmpty() || clave.length() < 6 || rolSeleccionado == null) {
                Notification.show("Completa todos los campos correctamente.");
                return;
            }

            codigoVerificacionRepository.findByEmailAndCodigoAndVerificadoFalse(email, codigo)
                    .ifPresentOrElse(codigoVerificacion -> {

                        if (usuarioRepository.findByUsername(email).isPresent()) {
                            Notification.show("⚠️ Ya existe una cuenta con ese correo.");
                            return;
                        }

                        codigoVerificacion.setVerificado(true);
                        codigoVerificacionRepository.save(codigoVerificacion);

                        Usuario nuevoUsuario = new Usuario();
                        nuevoUsuario.setUsername(email);
                        nuevoUsuario.setPassword(passwordEncoder.encode(clave));
                        nuevoUsuario.setRol(rolSeleccionado);
                        nuevoUsuario.setActivo(rolSeleccionado == Rol.PACIENTE); // los médicos deben ser activados por el admin

                        usuarioRepository.save(nuevoUsuario);

                        if (rolSeleccionado == Rol.MEDICO) {
                            Notification.show("✅ Solicitud enviada. Un administrador debe aprobar tu cuenta.");
                        } else {
                            Notification.show("✅ Cuenta creada correctamente. Ya puedes iniciar sesión.");
                        }

                    }, () -> Notification.show("❌ Código incorrecto o ya usado."));
        });

        add(titulo, emailField, codigoField, passwordField, rolComboBox, verificarBtn);
    }
}
