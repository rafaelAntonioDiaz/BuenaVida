package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Rol;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.PacienteRepository;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.UsuarioRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

/**
 * Vista de Vaadin para el registro de usuarios.
 * Permite ingresar datos, enviar un código de verificación via SendGrid API y registrar un paciente.
 */
@Route("registro")
@PageTitle("Registro de Usuario")
@AnonymousAllowed
public class RegistroView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(RegistroView.class);

    private final UsuarioRepository usuarioRepositorio;
    private final PacienteRepository pacienteRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Value("${sendgrid.api.key}")  // API key de SendGrid desde variables de entorno
    private String sendgridApiKey;

    @Value("${sendgrid.from.email:clubwebapp2025@gmail.com}")  // Email remitente verificado
    private String fromEmail;

    public RegistroView(
            UsuarioRepository usuarioRepositorio,
            PacienteRepository pacienteRepositorio,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
        this.passwordEncoder = passwordEncoder;

        // Componentes de la UI
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

        // Acción para enviar el código
        enviarCodigoBtn.addClickListener(e -> {
            String email = emailField.getValue();
            if (email == null || email.isBlank()) {
                Notification.show("Ingresa un correo válido.", 5000, Notification.Position.MIDDLE);
                return;
            }

            // Genera un código de 6 dígitos
            codigoGenerado[0] = String.valueOf(new Random().nextInt(900000) + 100000);

            try {
                // Configura el cliente SendGrid
                SendGrid sg = new SendGrid(sendgridApiKey);
                Email from = new Email(fromEmail);
                Email to = new Email(email);
                Content content = new Content("text/plain",
                        "Tu código de verificación es: " + codigoGenerado[0] + "\n\nEste código es válido por 10 minutos.");
                Mail mail = new Mail(from, "Código de verificación - Consultorio Acupuntura", to, content);

                // Envía el correo via API HTTP
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());

                Response response = sg.api(request);

                // Verifica la respuesta
                if (response.getStatusCode() == 202) {
                    logger.info("✅ Código enviado a: {}", email);
                    Notification.show("Código enviado a " + email, 5000, Notification.Position.MIDDLE);
                } else {
                    logger.error("❌ Error al enviar correo a {}: Status {}, Body: {}",
                            email, response.getStatusCode(), response.getBody());
                    Notification.show("Error al enviar correo: Status " + response.getStatusCode(),
                            5000, Notification.Position.MIDDLE);
                }
            } catch (Exception ex) {
                logger.error("❌ Error al enviar correo a {}: {}", email, ex.getMessage(), ex);
                Notification.show("Error al enviar correo: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        // Acción para registrar
        registrarBtn.addClickListener(e -> {
            String email = emailField.getValue();
            String nombres = nombresField.getValue();
            String apellidos = apellidosField.getValue();
            String celular = celularField.getValue();
            String password = passwordField.getValue();
            String codigoIngresado = codigoVerificacionField.getValue();

            // Validación de campos
            if (nombres == null || nombres.isBlank()
                    || apellidos == null || apellidos.isBlank()
                    || celular == null || celular.isBlank()
                    || email == null || email.isBlank()
                    || password == null || password.isBlank()) {
                Notification.show("Por favor completa todos los campos.", 5000, Notification.Position.MIDDLE);
                return;
            }

            // Validación del código
            if (codigoIngresado == null || !codigoIngresado.equals(codigoGenerado[0])) {
                Notification.show("Código incorrecto.", 5000, Notification.Position.MIDDLE);
                return;
            }

            // Verificar si el usuario ya existe
            if (usuarioRepositorio.existsByUsername(email)) {
                Notification.show("Ya existe una cuenta con ese correo.", 5000, Notification.Position.MIDDLE);
                return;
            }

            // Crear y guardar paciente
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setNombres(nombres);
            nuevoPaciente.setApellidos(apellidos);
            nuevoPaciente.setCelular(celular);
            nuevoPaciente.setUsername(email);
            nuevoPaciente.setPassword(passwordEncoder.encode(password));
            nuevoPaciente.setRol(Rol.PACIENTE);
            nuevoPaciente.setActivo(true);
            pacienteRepositorio.save(nuevoPaciente);
            logger.info("✅ Paciente guardado: {}", nuevoPaciente);

            // Notificar éxito y redirigir
            Notification.show("Registro exitoso.", 5000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("paciente");
        });

        // Añadir componentes a la vista
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

        // Configuración del layout
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setSizeFull();
    }
}