package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.BlogArticulo;
import com.ElihuAnalytics.ConsultorioAcupuntura.repositorio.BlogArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Servicio para gestionar la lógica de negocio de los artículos del blog.
 */
@Service
public class BlogArticuloService {

    private final BlogArticuloRepository blogArticuloRepository;

    // Patrón para crear "slugs" (URLs amigables)
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Autowired
    public BlogArticuloService(BlogArticuloRepository blogArticuloRepository) {
        this.blogArticuloRepository = blogArticuloRepository;
    }

    /**
     * Guarda un artículo (nuevo o existente) en la base de datos.
     * Genera el slug automáticamente a partir del título si es un artículo nuevo.
     * Establece las fechas de creación/modificación.
     *
     * @param articulo El artículo a guardar.
     * @return El artículo guardado.
     */
    public BlogArticulo guardar(BlogArticulo articulo) {
        if (articulo.getId() == null) {
            // Es un artículo nuevo
            articulo.setFechaCreacion(LocalDateTime.now());
            // Genera el slug a partir del título si no se proveyó uno
            if (articulo.getSlug() == null || articulo.getSlug().isBlank()) {
                articulo.setSlug(generarSlug(articulo.getTitulo()));
            }
        } else {
            // Es una actualización
            // Asegura que la fecha de creación original se mantenga
            BlogArticulo original = blogArticuloRepository.findById(articulo.getId()).orElse(null);
            if (original != null) {
                articulo.setFechaCreacion(original.getFechaCreacion());
            }
        }
        articulo.setFechaModificacion(LocalDateTime.now());
        return blogArticuloRepository.save(articulo);
    }

    /**
     * Busca un artículo por su slug.
     * Usado por la vista pública del blog.
     *
     * @param slug El identificador de la URL.
     * @return Un Optional con el artículo.
     */
    public Optional<BlogArticulo> findBySlug(String slug) {
        return blogArticuloRepository.findBySlug(slug);
    }

    /**
     * Obtiene los 3 artículos más recientes.
     * Usado por la página de inicio (HomeView).
     *
     * @return Lista de los 3 artículos más nuevos.
     */
    public List<BlogArticulo> findTop3Recientes() {
        return blogArticuloRepository.findTop3ByOrderByFechaCreacionDesc();
    }

    /**
     * Obtiene todos los artículos ordenados por fecha de creación descendente.
     * Usado por el panel de administración.
     *
     * @return Lista de todos los artículos.
     */
    public List<BlogArticulo> findAllOrdenados() {
        return blogArticuloRepository.findAllByOrderByFechaCreacionDesc();
    }

    /**
     * Elimina un artículo por su ID.
     *
     * @param id El ID del artículo a eliminar.
     */
    public void eliminar(Long id) {
        blogArticuloRepository.deleteById(id);
    }

    /**
     * Genera un "slug" amigable para la URL a partir de un título.
     * Ejemplo: "Manejo del Párkinson" -> "manejo-del-parkinson"
     *
     * @param input El título del artículo.
     * @return El slug normalizado.
     */
    public static String generarSlug(String input) {
        if (input == null) {
            return "";
        }
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}