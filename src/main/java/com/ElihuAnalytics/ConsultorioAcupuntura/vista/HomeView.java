package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.seguridad.AutenticacionServicio;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.BlogArticuloService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.BlogArticulo;
import java.util.List;
/**
 * Vista principal (Home) - Página de aterrizaje pública.
 * Corregida para no usar LayoutPrincipal y añadir sección de blog.
 */

// --- INICIO DE LA CORRECCIÓN ---
// Se elimina "layout = LayoutPrincipal.class" para restaurar el diseño original.
@Route("")
// --- FIN DE LA CORRECCIÓN ---
@PageTitle("Acupuntura para Parkinson, Asma y Gota en Bucaramanga | Rafael Díaz")
@AnonymousAllowed
@CssImport("./styles/home-view.css") // Importamos los estilos
public class HomeView extends VerticalLayout implements BeforeEnterObserver {

    private final AutenticacionServicio auth;
    private final String appUrl;
    private final BlogArticuloService blogArticuloService;
    private Div cardContainer = new Div();

    public HomeView(@Autowired AutenticacionServicio auth,
                    @Value("${app.share.url}") String appUrl,
                    @Autowired BlogArticuloService blogArticuloService) {

        this.blogArticuloService = blogArticuloService;
        this.auth = auth;
        this.appUrl = appUrl;

        // --- INICIO DE LA CORRECCIÓN DE DISEÑO ---
        // Usamos la misma clase de fondo que LoginView
        addClassName("login-view");
        // Eliminamos el fondo por defecto del VerticalLayout
        getStyle()
                .set("background", "transparent")
                .set("background-color", "transparent");
        // --- FIN DE LA CORRECCIÓN DE DISEÑO ---

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        // Ajustamos la justificación para permitir el scroll
        setJustifyContentMode(JustifyContentMode.START);
        // Añadimos padding para que el contenido no pegue con los bordes
        getStyle().set("padding", "var(--lumo-space-l)");

        // --- Contenido Original del Home (Logo, Título, Botones) ---
        Image logo = new Image("/images/logo-rafael-diaz-sarmiento.svg", "Rafael Antonio Díaz Sarmiento - Logo");
        logo.addClassName("logo");
        // Damos un margen superior al logo
        logo.getStyle().set("margin-top", "var(--lumo-space-xl)");

        H1 titulo = new H1("Buena Vida  Medicina Ancestral");
        titulo.addClassName("titulo");

        Paragraph intro = new Paragraph("Acupuntura a domicilio en Bucaramanga, Floridablanca y Girón.");
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

        // --- NUEVA SECCIÓN DE BLOG ---
        VerticalLayout seccionBlog = crearSeccionBlog();

        // Añadimos todo el contenido a la vista
        add(logo, titulo, intro, botones, socialButtons, seccionBlog);
        cargarArticulosRecientes();
    }

    /**
     * Crea la nueva sección de publicaciones de blog.
     */
    /**
     * Crea la ESTRUCTURA de la sección de publicaciones de blog.
     * La carga de datos se hace en cargarArticulosRecientes().
     */
    private VerticalLayout crearSeccionBlog() {
        VerticalLayout section = new VerticalLayout();
        section.setAlignItems(Alignment.CENTER);
        section.setSpacing(false);
        section.getStyle().set("margin-top", "var(--lumo-space-xl)");
        section.addClassName("blog-section");

        H2 tituloSeccion = new H2("Publicaciones Recientes");
        tituloSeccion.addClassName("blog-section-title");

        // Contenedor de las tarjetas de blog (ahora es un campo: this.cardContainer)
        this.cardContainer.addClassName("blog-card-container");
        this.cardContainer.setWidthFull(); // Asegura que ocupe el espacio

        section.add(tituloSeccion, this.cardContainer);
        return section;
    }
    /**
     * Helper para crear una tarjeta de blog individual.
     */
    /**
     * Helper para crear una tarjeta de blog individual.
     */
    /**
     * Helper para crear una tarjeta de blog individual.
     */
    /**
     * Helper para crear una tarjeta de blog individual.
     */
    private VerticalLayout crearBlogCard(String categoria, String titulo, String resumen, String slug) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("blog-card");
        card.setPadding(true);
        card.setSpacing(false);

        Span spanCategoria = new Span(categoria);
        spanCategoria.addClassName("blog-card-categoria");

        H3 h3Titulo = new H3(titulo);
        h3Titulo.addClassName("blog-card-titulo");

        // --- AJUSTE: Asegura que el resumen no falle si es muy corto ---
        Paragraph pResumen = new Paragraph(resumen != null ? resumen : ""); // Usa resumen o cadena vacía
        pResumen.addClassName("blog-card-resumen");
        // --- FIN AJUSTE ---

        RouteParameters parametros = new RouteParameters("slug", slug);
        RouterLink link = new RouterLink("Leer más...", BlogArticuloView.class, parametros);
        link.addClassName("blog-card-link");

        card.add(spanCategoria, h3Titulo, pResumen, link);
        return card;
    }    // --- MÉTODOS ORIGINALES (sin cambios) ---

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
    /**
     * Carga los 3 artículos más recientes desde el servicio y los muestra.
     */
    private void cargarArticulosRecientes() {
        // Limpiamos el contenedor por si acaso
        this.cardContainer.removeAll();

        // Obtenemos los artículos del servicio
        List<BlogArticulo> articulos = blogArticuloService.findTop3Recientes();

        if (articulos.isEmpty()) {
            // Si no hay artículos, muestra un mensaje
            Paragraph msg = new Paragraph("Aún no hay publicaciones disponibles.");
            msg.getStyle().set("color", "var(--color-text-medium)");
            this.cardContainer.add(msg);
        } else {
            // Si hay artículos, crea una tarjeta para cada uno
            for (BlogArticulo articulo : articulos) {
                // Genera un resumen corto (quita HTML y toma los primeros 150 caracteres)
                String textoPlano = articulo.getContenido().replaceAll("<[^>]*>", "");
                String resumen = textoPlano.substring(0, Math.min(textoPlano.length(), 150)) + "...";

                VerticalLayout card = crearBlogCard(
                        articulo.getCategoria(),
                        articulo.getTitulo(),
                        resumen,
                        articulo.getSlug() // Pasamos el slug real
                );
                this.cardContainer.add(card);
            }
        }
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        // --- Lógica de SEO (se mantiene) ---
        String descriptionContent = "Descubre tratamientos efectivos con medicina ancestral. Ofrezco acupuntura a domicilio en Bucaramanga, Floridablanca y Girón para condiciones como Parkinson, asma, gota y esclerosis múltiple.";
        String script = "let meta = document.querySelector('meta[name=\"description\"]');" +
                "if (!meta) {" +
                "  meta = document.createElement('meta');" +
                "  meta.setAttribute('name', 'description');" +
                "  document.head.appendChild(meta);" +
                "}" +
                "meta.setAttribute('content', $0);";
        event.getUI().getPage().executeJs(script, descriptionContent);


        // --- Lógica de Autenticación (se mantiene) ---
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