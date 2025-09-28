package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.UsuarioRepository;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

@Route("registro")
@PageTitle("Registro de Usuario")
@AnonymousAllowed
public class RegistroView extends VerticalLayout {

    private final JavaMailSender mailSender;
    private final UsuarioRepository usuarioRepositorio;
    private final PacienteRepository pacienteRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistroView(JavaMailSender mailSender, UsuarioRepository usuarioRepositorio,
                         PacienteRepository pacienteRepositorio,
                        PasswordEncoder passwordEncoder) {
        this.mailSender = mailSender;
        this.usuarioRepositorio = usuarioRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
        this.passwordEncoder = passwordEncoder;

        H1 titulo = new H1("Registro de Usuario");
        TextField nombresField = new TextField("Nombres");
        TextField apellidosField = new TextField("Apellidos");
        TextField celularField = new TextField("Celular");
        EmailField emailField = new EmailField("Correo electrónico");
        emailField.setPlaceholder("usuario@dominio.com");
        emailField.setClearButtonVisible(true);

        PasswordField passwordField = new PasswordField("Contraseña");
        passwordField.setPlaceholder("********");

        TextField codigoVerificacionField = new TextField("Código de verificación");

        Button enviarCodigoBtn = new Button("Enviar código");
        Button registrarBtn = new Button("Registrar");

        // Generador de código
        final String[] codigoGenerado = new String[1];

        enviarCodigoBtn.addClickListener(e -> {
            String email = emailField.getValue();
            if (email.isBlank()) {
                Notification.show("Ingresa un correo válido.");
                return;
            }

            codigoGenerado[0] = String.valueOf(new Random().nextInt(900000) + 100000);

            try {
                SimpleMailMessage mensaje = new SimpleMailMessage();
                mensaje.setTo(email);
                mensaje.setSubject("Código de verificación");
                mensaje.setText("Tu código es: " + codigoGenerado[0]);
                mailSender.send(mensaje);

                Notification.show("Código enviado a " + email);
            } catch (Exception ex) {
                Notification.show("Error al enviar correo: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        registrarBtn.addClickListener(e -> {
            String email = emailField.getValue();
            String nombres = nombresField.getValue();
            String apellidos = apellidosField.getValue();
            String celular = celularField.getValue();
            String password = passwordField.getValue();
            String codigoIngresado = codigoVerificacionField.getValue();

            if (nombres == null || nombres.isBlank()
                    || apellidos == null || apellidos.isBlank()
                    || celular == null || celular.isBlank()
                    || email == null || email.isBlank()
                    || password == null || password.isBlank()) {
                Notification.show("Por favor completa todos los campos.");
                return;
            }



            if (!codigoIngresado.equals(codigoGenerado[0])) {
                Notification.show("Código incorrecto.");
                return;
            }

            if (usuarioRepositorio.existsByUsername(email)) {
                Notification.show("Ya existe una cuenta con ese correo.");
                return;
            }

                Paciente nuevoPaciente = new Paciente();
                nuevoPaciente.setNombres(nombres);
                nuevoPaciente.setApellidos(apellidos);
                nuevoPaciente.setCelular(celular);
                nuevoPaciente.setUsername(email);
                nuevoPaciente.setPassword(passwordEncoder.encode(password));
                nuevoPaciente.setRol(Rol.PACIENTE);
                nuevoPaciente.setActivo(true);
                pacienteRepositorio.save(nuevoPaciente);
                System.out.println("Paciente guardado: " + nuevoPaciente.toString());

            Notification.show("Registro exitoso. ");
                    UI.getCurrent().navigate("paciente");

            });

        add(
                titulo,
                new Text("Ingresa tu información para crear una cuenta."),
                nombresField,
                apellidosField,
                celularField,
                emailField,
                passwordField,
                enviarCodigoBtn,
                codigoVerificacionField,
                registrarBtn
        );


        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setSizeFull();
    }
}
