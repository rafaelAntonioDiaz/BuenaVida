package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.AutenticacionServicio;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
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

@Route("")
@AnonymousAllowed
public class HomeView extends VerticalLayout implements BeforeEnterObserver {

    private final AutenticacionServicio auth;
    private final String appUrl;

    public HomeView(@Autowired AutenticacionServicio auth,
                    @Value("${app.share.url}") String appUrl) {
        this.auth = auth;
        this.appUrl = appUrl;

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setSizeFull();

        H1 titulo = new H1("Buena Vida - Medicina Ancestral");
        Paragraph intro = new Paragraph("Otro paso en la dirección correcta para mejorar tu salud !");

        Button registroBtn = new Button("Crear cuenta", e -> UI.getCurrent().navigate("registro"));
        Button loginBtn = new Button("Iniciar sesión", e -> UI.getCurrent().navigate("login"));

        HorizontalLayout socialButtons = crearBotonesCompartir();

        add(titulo, intro, registroBtn, loginBtn, socialButtons);
    }

    private HorizontalLayout crearBotonesCompartir() {
        Button fb = crearBotonSocial(VaadinIcon.FACEBOOK.create(), "facebook",
                "https://www.facebook.com/sharer/sharer.php?u=" + appUrl);
        fb.getElement().setAttribute("title", "Compartir en Facebook");
        fb.getElement().setAttribute("aria-label", "Compartir en Facebook");

        Button tw = crearBotonSocial(VaadinIcon.TWITTER.create(), "twitter",
                "https://twitter.com/intent/tweet?url=" + appUrl + "&text=¡Descubre%20Buena%20Vida!");
        tw.getElement().setAttribute("title", "Compartir en X");
        tw.getElement().setAttribute("aria-label", "Compartir en X");

        Button wa = crearBotonSocial(VaadinIcon.COMMENT_ELLIPSIS.create(), "whatsapp",
                "https://api.whatsapp.com/send?text=¡Descubre%20Buena%20Vida!%20" + appUrl);
        wa.getElement().setAttribute("title", "Compartir en WhatsApp");
        wa.getElement().setAttribute("aria-label", "Compartir en WhatsApp");

        Button li = crearBotonSocial(VaadinIcon.LINK.create(), "linkedin",
                "https://www.linkedin.com/sharing/share-offsite/?url=" + appUrl);
        li.getElement().setAttribute("title", "Compartir en LinkedIn");
        li.getElement().setAttribute("aria-label", "Compartir en LinkedIn");

        // 📋 Copiar enlace (con fallback si no hay navigator.clipboard)
        Button copy = crearBotonSocial(VaadinIcon.CLIPBOARD.create(), "copy", null);
        copy.getElement().setAttribute("title", "Copiar enlace");
        copy.getElement().setAttribute("aria-label", "Copiar enlace");
        copy.addClickListener(e ->
                getUI().ifPresent(ui -> ui.getPage().executeJs(
                        // HTTPS/localhost → usa Clipboard API; si no, fallback con textarea oculto
                        "const url=$0;" +
                                "if (navigator.clipboard && window.isSecureContext) {" +
                                "  navigator.clipboard.writeText(url);" +
                                "} else {" +
                                "  const ta=document.createElement('textarea');" +
                                "  ta.value=url; ta.style.position='fixed'; ta.style.left='-9999px';" +
                                "  document.body.appendChild(ta); ta.focus(); ta.select();" +
                                "  try { document.execCommand('copy'); } finally { document.body.removeChild(ta); }" +
                                "}", appUrl))
        );

        return new HorizontalLayout(fb, tw, wa, li, copy);
    }

    private Button crearBotonSocial(com.vaadin.flow.component.icon.Icon icon, String themeClass, String url) {
        Button btn = new Button(icon, e -> {
            if (url != null) {
                getUI().ifPresent(ui -> ui.getPage().open(url, "_blank"));
            }
        });
        btn.addThemeVariants(ButtonVariant.LUMO_ICON);
        btn.addClassName("social-btn");
        btn.addClassName(themeClass);
        btn.setWidth("48px");
        btn.setHeight("48px");
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
            if (!route.isEmpty()) {
                event.forwardTo(route);
            }
        }
    }
}
