package com.ElihuAnalytics.ConsultorioAcupuntura.vista; // O com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Componente reutilizable (Card) para gestionar los artículos del blog.
 */
@Component
@Scope("prototype")
public class GestionBlogCard extends VerticalLayout {

    private final BlogArticuloService blogArticuloService;

    // Campos UI
    private Grid<BlogArticulo> gridBlog = new Grid<>(BlogArticulo.class);
    private Binder<BlogArticulo> binderBlog = new Binder<>(BlogArticulo.class);
    private TextField tituloBlog = new TextField("Título");
    private TextField slug = new TextField("Slug (URL)");
    private TextField categoria = new TextField("Categoría");
    private TextArea contenido = new TextArea("contenido");
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
        // --- INICIO DE LA CORRECCIÓN ---
        // Darle altura al TextArea
        contenido.setWidthFull();
        contenido.setMinHeight("400px"); // Puedes ajustar esta altura
        contenido.getStyle().set("resize", "vertical"); // Permite redimensionar verticalmente
        // Ya no necesitamos la clase CSS 'blog-editor-contenido'
        // --- FIN DE LA CORRECCIÓN ---

        tituloBlog.addValueChangeListener(e -> {
            if (binderBlog.getBean() != null && binderBlog.getBean().getId() == null) {
                slug.setValue(BlogArticuloService.generarSlug(e.getValue()));
            }
        });
        editorForm.add(tituloBlog, slug, categoria, contenido); // Añadimos el TextArea

        HorizontalLayout botonesLayout = new HorizontalLayout(guardarBtn, limpiarBtn);
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        limpiarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        configurarBinder();
        configurarListeners();

        add(tituloCard, gridBlog, editorForm, botonesLayout);
        setWidthFull();
        setVisible(false);
    }

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

    private void configurarBinder() {
        binderBlog.bind(tituloBlog, BlogArticulo::getTitulo, BlogArticulo::setTitulo);
        binderBlog.bind(slug, BlogArticulo::getSlug, BlogArticulo::setSlug);
        binderBlog.bind(categoria, BlogArticulo::getCategoria, BlogArticulo::setCategoria);
        binderBlog.bind(contenido, BlogArticulo::getContenido, BlogArticulo::setContenido);
    }

    private void configurarListeners() {
        limpiarBtn.addClickListener(e -> limpiarFormularioBlog());
        guardarBtn.addClickListener(e -> guardarArticulo());
        gridBlog.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                binderBlog.setBean(e.getValue());
            } else {
                limpiarFormularioBlog();
            }
        });
    }

    /**
     * Carga o recarga los datos en el grid.
     * Llamado desde MedicoAdministradorView.
     */
    public void refreshGrid() {
        if (isVisible()) {
            gridBlog.setItems(blogArticuloService.findAllOrdenados());
        }
    }

    private void limpiarFormularioBlog() {
        binderBlog.setBean(new BlogArticulo());
        gridBlog.deselectAll();
    }

    private void guardarArticulo() {
        BlogArticulo articulo = binderBlog.getBean();
        if (articulo != null && articulo.getTitulo() != null && !articulo.getTitulo().isBlank()) {
            try {
                blogArticuloService.guardar(articulo);
                Notification.show("Artículo guardado.", 3000, Notification.Position.BOTTOM_STRETCH)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid(); // Actualiza este grid
                limpiarFormularioBlog();
            } catch (Exception e) {
                Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.BOTTOM_STRETCH)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Título obligatorio.", 3000, Notification.Position.BOTTOM_STRETCH)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private void eliminarArticulo(BlogArticulo articulo) {
        if (articulo != null) {
            try{
                blogArticuloService.eliminar(articulo.getId());
                Notification.show("Artículo eliminado.", 2000, Notification.Position.BOTTOM_STRETCH);
                refreshGrid(); // Actualiza este grid
                limpiarFormularioBlog();
            } catch (Exception e) {
                Notification.show("Error al eliminar: " + e.getMessage(), 5000, Notification.Position.BOTTOM_STRETCH)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
}