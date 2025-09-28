package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Usuario;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final LoginForm loginForm;

    public LoginView() {
        H1 titulo = new H1("Por favor inicia sesión");

        loginForm = new LoginForm();
        loginForm.setAction("login");

        Button registroBtn = new Button("¿No tienes cuenta? Regístrate");
        registroBtn.addClickListener(e ->
                UI.getCurrent().navigate("registro"));

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        add(titulo, loginForm, registroBtn);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Si llega con ?error, mostramos el mensaje de error en el formulario

        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}