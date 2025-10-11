package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.AutenticacionServicio;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Vista principal (Home) - Rafael Antonio Díaz Sarmiento
 * Incluye autenticación, botones sociales, estilos modernos y comentarios explicativos.
 */
@Route("")
@AnonymousAllowed
@CssImport("./styles/home-view.css")
public class HomeView extends VerticalLayout implements BeforeEnterObserver {

    private final AutenticacionServicio auth;
    private final String appUrl;

    public HomeView(@Autowired AutenticacionServicio auth,
                    @Value("${app.share.url}") String appUrl) {
        this.auth = auth;
        this.appUrl = appUrl;

        addClassName("home-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Image logo = new Image("/images/logo-rafael-diaz-sarmiento.svg", "Rafael Antonio Díaz Sarmiento - Logo");
        logo.addClassName("logo");

        H1 titulo = new H1("Buena Vida – Medicina Ancestral");
        titulo.addClassName("titulo");

        Paragraph intro = new Paragraph("Acupuntura y medicina ancestral a domicilio en Bucaramanga, Floridablanca y Girón.");
        intro.addClassName("intro");

        Button registroBtn = new Button("Crear cuenta", e -> UI.getCurrent().navigate("registro"));
        registroBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registroBtn.addClassName("boton-principal");

        Button loginBtn = new Button("Iniciar sesión", e -> UI.getCurrent().navigate("login"));
        loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginBtn.addClassName("boton-secundario");

        HorizontalLayout botones = new HorizontalLayout(registroBtn, loginBtn);
        botones.addClassName("botones-acceso");

        HorizontalLayout socialButtons = crearBotonesCompartir();
        socialButtons.addClassName("social-buttons");

        add(logo, titulo, intro, botones, socialButtons);
    }

    private HorizontalLayout crearBotonesCompartir() {
        Button fb = crearBotonSocial(VaadinIcon.FACEBOOK.create(), "facebook", "https://www.facebook.com/sharer/sharer.php?u=" + appUrl);
        Button tw = crearBotonSocial(VaadinIcon.TWITTER.create(), "twitter", "https://twitter.com/intent/tweet?url=" + appUrl);
        Button wa = crearBotonSocial(VaadinIcon.COMMENT_ELLIPSIS.create(), "whatsapp", "https://api.whatsapp.com/send?text=¡Descubre%20Buena%20Vida!%20" + appUrl);
        Button li = crearBotonSocial(VaadinIcon.LINK.create(), "linkedin", "https://www.linkedin.com/sharing/share-offsite/?url=" + appUrl);
        Button copy = crearBotonSocial(VaadinIcon.CLIPBOARD.create(), "copy", null);

        copy.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().executeJs(
                "const url=$0;if(navigator.clipboard&&window.isSecureContext){navigator.clipboard.writeText(url);}else{const ta=document.createElement('textarea');ta.value=url;ta.style.position='fixed';ta.style.left='-9999px';document.body.appendChild(ta);ta.focus();ta.select();try{document.execCommand('copy');}finally{document.body.removeChild(ta);}}", appUrl)));

        return new HorizontalLayout(fb, tw, wa, li, copy);
    }

    private Button crearBotonSocial(com.vaadin.flow.component.icon.Icon icon, String themeClass, String url) {
        Button btn = new Button(icon, e -> {
            if (url != null) getUI().ifPresent(ui -> ui.getPage().open(url, "_blank"));
        });
        btn.addThemeVariants(ButtonVariant.LUMO_ICON);
        btn.addClassNames("social-btn", themeClass);
        btn.setWidth("44px");
        btn.setHeight("44px");
        return btn;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {

            var authorities = authentication.getAuthorities();
            boolean isMedico = authorities.stream().anyMatch(a -> "ROLE_MEDICO".equals(a.getAuthority()));
            boolean isAdmin = authorities.stream().anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
            boolean isPaciente = authorities.stream().anyMatch(a -> "ROLE_PACIENTE".equals(a.getAuthority()));

            String route = isMedico ? "medico" : (isAdmin ? "admin" : (isPaciente ? "paciente" : ""));
            if (!route.isEmpty()) event.forwardTo(route);
        }
    }
}