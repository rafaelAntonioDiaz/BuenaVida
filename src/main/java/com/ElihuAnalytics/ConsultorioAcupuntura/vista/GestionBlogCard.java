package com.ElihuAnalytics.ConsultorioAcupuntura.vista; // O tu paquete de componentes

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.BlogArticulo;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.BlogArticuloService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea; // Usando TextArea
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException; // <-- IMPORTAR ValidationException
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.slf4j.Logger; // <-- IMPORTAR Logger
import org.slf4j.LoggerFactory; // <-- IMPORTAR LoggerFactory


import java.time.format.DateTimeFormatter;

/**
 * Componente reutilizable (Card) para gestionar los artículos del blog.
 * CORREGIDO: Slug automático y guardado con Binder.
 */
@Component
@Scope("prototype")
public class GestionBlogCard extends VerticalLayout {

    private final BlogArticuloService blogArticuloService;
    private static final Logger log = LoggerFactory.getLogger(GestionBlogCard.class); // <-- Logger

    // Campos UI
    private Grid<BlogArticulo> gridBlog = new Grid<>(BlogArticulo.class);
    // Binder ahora con validación activada
    private Binder<BlogArticulo> binderBlog = new Binder<>(BlogArticulo.class);
    private TextField tituloBlog = new TextField("Título");
    private TextField slug = new TextField("Slug (URL)");
    private TextField categoria = new TextField("Categoría");
    private TextArea contenido = new TextArea("Contenido"); // Usando TextArea
    private Button guardarBtn = new Button("Guardar Artículo");
    private Button limpiarBtn = new Button("Nuevo Artículo", VaadinIcon.PLUS.create());

    @Autowired
    public GestionBlogCard(BlogArticuloService blogArticuloService) {
        this.blogArticuloService = blogArticuloService;

        addClassName("admin-card");
        H3 tituloCard = new H3("Gestor de Contenido del Blog");

        configurarGridBlog();

        VerticalLayout editorForm = new VerticalLayout();
        editorForm.setPadding(false);

        contenido.setWidthFull();
        contenido.setMinHeight("400px");
        contenido.getStyle().set("resize", "vertical");

        // --- Slug Automático (Listener se mantiene, pero aseguramos bean) ---
        tituloBlog.addValueChangeListener(e -> {
            // Verifica si hay un bean y si es NUEVO (id null)
            BlogArticulo currentBean = binderBlog.getBean();
            if (currentBean != null && currentBean.getId() == null && e.getValue() != null) {
                slug.setValue(BlogArticuloService.generarSlug(e.getValue()));
            }
        });
        editorForm.add(tituloBlog, slug, categoria, contenido);

        HorizontalLayout botonesLayout = new HorizontalLayout(guardarBtn, limpiarBtn);
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        limpiarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        configurarBinder();
        configurarListeners();

        add(tituloCard, gridBlog, editorForm, botonesLayout);
        setWidthFull();
        setVisible(false);

        // --- INICIO CORRECCIÓN SLUG ---
        // Establecer un bean vacío inicial para que el listener del slug funcione desde el principio
        binderBlog.setBean(new BlogArticulo());
        // --- FIN CORRECCIÓN SLUG ---
    }

    private void configurarBinder() {
        // --- INICIO CORRECCIÓN GUARDADO ---
        // Añadir validación requerida explícita para el título en el Binder
        binderBlog.forField(tituloBlog)
                .asRequired("El título es obligatorio") // Mensaje de validación
                .bind(BlogArticulo::getTitulo, BlogArticulo::setTitulo);
        // --- FIN CORRECCIÓN GUARDADO ---

        binderBlog.bind(slug, BlogArticulo::getSlug, BlogArticulo::setSlug);
        binderBlog.bind(categoria, BlogArticulo::getCategoria, BlogArticulo::setCategoria);
        binderBlog.bind(contenido, BlogArticulo::getContenido, BlogArticulo::setContenido);
    }

    // (configurarGridBlog y configurarListeners se mantienen igual)
    private void configurarGridBlog() {
        gridBlog.removeAllColumns();
        gridBlog.addColumn(BlogArticulo::getTitulo).setHeader("Título").setSortable(true);
        gridBlog.addColumn(BlogArticulo::getCategoria).setHeader("Categoría").setSortable(true);
        gridBlog.addColumn(articulo ->
                articulo.getFechaCreacion() != null ?
                        articulo.getFechaCreacion().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A"
        ).setHeader("Publicado").setSortable(true);
        gridBlog.addComponentColumn(articulo -> {
            Button eliminarBtn = new Button(VaadinIcon.TRASH.create(), e -> eliminarArticulo(articulo));
            eliminarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            eliminarBtn.setTooltipText("Eliminar artículo");
            return eliminarBtn;
        });
        gridBlog.setWidthFull();
    }

    private void configurarListeners() {
        limpiarBtn.addClickListener(e -> limpiarFormularioBlog());
        guardarBtn.addClickListener(e -> guardarArticulo());
        gridBlog.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                binderBlog.setBean(e.getValue()); // Carga el artículo seleccionado
            } else {
                limpiarFormularioBlog(); // Limpia si no hay selección
            }
        });
    }


    public void refreshGrid() {
        if (isVisible()) {
            gridBlog.setItems(blogArticuloService.findAllOrdenados());
        }
    }

    private void limpiarFormularioBlog() {
        binderBlog.setBean(new BlogArticulo()); // Crea un nuevo objeto para el formulario
        gridBlog.deselectAll();
    }

    // --- INICIO CORRECCIÓN GUARDADO ---
    /**
     * Guarda el artículo actual del Binder después de validar
     * y escribir los datos del formulario en el bean.
     */
    private void guardarArticulo() {
        BlogArticulo articulo = binderBlog.getBean(); // Obtiene el bean actual (nuevo o editado)
        if (articulo == null) {
            Notification.show("No hay artículo para guardar.", 3000, Notification.Position.BOTTOM_STRETCH)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return; // Salir si no hay bean
        }

        // Intenta escribir los valores del formulario al bean.
        // Esto ejecuta las validaciones (como la del título).
        boolean esValido = binderBlog.writeBeanIfValid(articulo);

        if (esValido) {
            // Si la escritura fue exitosa (validaciones pasaron)
            try {
                // Genera el slug si es un artículo nuevo y el slug está vacío (doble chequeo)
                if (articulo.getId() == null && (articulo.getSlug() == null || articulo.getSlug().isBlank()) && articulo.getTitulo() != null) {
                    articulo.setSlug(BlogArticuloService.generarSlug(articulo.getTitulo()));
                }

                blogArticuloService.guardar(articulo); // Llama al servicio para guardar
                Notification.show("Artículo guardado.", 3000, Notification.Position.BOTTOM_STRETCH)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid(); // Actualiza la tabla
                limpiarFormularioBlog(); // Limpia para un nuevo artículo
            } catch (Exception e) {
                log.error("Error al guardar artículo: {}", e.getMessage(), e); // Log del error
                // Muestra un mensaje más genérico al usuario
                Notification.show("Error al guardar el artículo. Revisa que el 'Slug (URL)' sea único si ya existe un artículo con ese título.", 7000, Notification.Position.BOTTOM_STRETCH)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            // Si la escritura falló (validaciones no pasaron)
            // El Binder ya habrá mostrado los mensajes de error en los campos
            Notification.show("Por favor, corrige los errores en el formulario.", 3000, Notification.Position.BOTTOM_STRETCH)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }
    // --- FIN CORRECCIÓN GUARDADO ---


    // (eliminarArticulo se mantiene igual)
    private void eliminarArticulo(BlogArticulo articulo) {
        if (articulo != null) {
            try{
                blogArticuloService.eliminar(articulo.getId());
                Notification.show("Artículo eliminado.", 2000, Notification.Position.BOTTOM_STRETCH);
                refreshGrid();
                limpiarFormularioBlog();
            } catch (Exception e) {
                log.error("Error al eliminar artículo: {}", e.getMessage(), e);
                Notification.show("Error al eliminar el artículo.", 5000, Notification.Position.BOTTOM_STRETCH)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
}