package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.BlogArticulo; // <-- IMPORTAR MODELO
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.BlogArticuloService; // <-- IMPORTAR SERVICIO
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div; // <-- IMPORTAR DIV
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException; // <-- IMPORTAR NOTFOUND
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired; // <-- IMPORTAR AUTOWIRED

import java.util.Optional;

/**
 * Vista pública para mostrar un artículo de blog individual.
 * Carga el contenido desde la base de datos usando BlogArticuloService.
 */
@Route("blog/:slug")
@AnonymousAllowed
@CssImport("./styles/home-view.css") // Reutiliza estilos
@CssImport("./styles/blog-articulo-view.css") // <-- NUEVO CSS (lo crearemos después)
public class BlogArticuloView extends VerticalLayout implements BeforeEnterObserver {

    private final BlogArticuloService blogArticuloService; // <-- INYECTAR SERVICIO

    private H1 titulo = new H1();
    // Usamos Div para poder renderizar HTML del RichTextEditor
    private Div contenidoHtml = new Div();
    private Image imagenHeader = new Image(); // (Opcional, si guardas imágenes)

    // Contenedor principal del artículo
    private VerticalLayout contenedorArticulo = new VerticalLayout();

    @Autowired // <-- AÑADIR AUTOWIRED AL CONSTRUCTOR
    public BlogArticuloView(BlogArticuloService blogArticuloService) {
        this.blogArticuloService = blogArticuloService; // <-- ASIGNAR SERVICIO

        // --- Configuración de la Vista ---
        addClassName("login-view");
        getStyle().set("background", "transparent").set("background-color", "transparent");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        getStyle().set("padding", "var(--lumo-space-l)");

        // --- Contenedor del Artículo (efecto "glass") ---
        contenedorArticulo.setPadding(true);
        contenedorArticulo.setSpacing(true);
        contenedorArticulo.setAlignItems(Alignment.START); // Alinea el contenido a la izquierda
        contenedorArticulo.addClassName("blog-section");
        contenedorArticulo.getStyle()
                .set("max-width", "900px") // Ancho máximo del contenido
                .set("margin-top", "var(--lumo-space-xl)");

        // Configuración de los componentes
        titulo.addClassName("blog-section-title");
        imagenHeader.setWidthFull();
        imagenHeader.getStyle().set("border-radius", "var(--radius-md)").set("margin-bottom", "var(--lumo-space-l)");
        imagenHeader.setVisible(false); // Ocultar si no hay imagen

        // Estilo para el contenido HTML
        contenidoHtml.addClassName("blog-contenido");

        contenedorArticulo.add(titulo, imagenHeader, contenidoHtml);
        add(contenedorArticulo);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Obtiene el "slug" de la URL
        String slugArticulo = event.getRouteParameters().get("slug")
                .orElseThrow(() -> new NotFoundException("Slug no proporcionado")); // Lanza error 404 si no hay slug

        // --- INICIO DE LA LÓGICA DE CARGA ---
        Optional<BlogArticulo> articuloOpt = blogArticuloService.findBySlug(slugArticulo);

        if (articuloOpt.isPresent()) {
            BlogArticulo articulo = articuloOpt.get();

            // 1. Actualizar UI
            titulo.setText(articulo.getTitulo());
            // ¡IMPORTANTE! Usamos setValue para renderizar el HTML del RichTextEditor
            contenidoHtml.getElement().setProperty("innerHTML", articulo.getContenido());

            // (Opcional: Si tienes imágenes asociadas al artículo)
            // imagenHeader.setSrc("/ruta/a/imagen/" + articulo.getImagenId());
            // imagenHeader.setVisible(true);

            // 2. Actualizar SEO
            String pageTitle = "Buena Vida | " + articulo.getTitulo();
            // Creamos una descripción corta (primeros 155 caracteres del contenido SIN HTML)
            String textoPlano = articulo.getContenido().replaceAll("<[^>]*>", ""); // Quita etiquetas HTML
            String description = textoPlano.substring(0, Math.min(textoPlano.length(), 155)) + "...";

            UI.getCurrent().getPage().setTitle(pageTitle);

            String script = "let meta = document.querySelector('meta[name=\"description\"]');" +
                    "if (!meta) { meta = document.createElement('meta'); meta.setAttribute('name', 'description'); document.head.appendChild(meta); }" +
                    "meta.setAttribute('content', $0);";
            event.getUI().getPage().executeJs(script, description);

        } else {
            // Artículo no encontrado - Mostrar mensaje o redirigir
            titulo.setText("Artículo no encontrado");
            contenidoHtml.setText("Lo sentimos, la página que buscas no existe o ha sido movida.");
            imagenHeader.setVisible(false);

            // También actualiza el título de la página para indicar el error
            UI.getCurrent().getPage().setTitle("Artículo no encontrado | Buena Vida");
            // Podrías lanzar NotFoundException aquí si prefieres una página 404 estándar
            // throw new NotFoundException("Artículo no encontrado con slug: " + slugArticulo);
        }
        // --- FIN DE LA LÓGICA DE CARGA ---
    }

}