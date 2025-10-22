package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.BlogArticulo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/**
 * Repositorio para la entidad BlogArticulo.
 * Proporciona métodos CRUD y consultas personalizadas.
 */
public interface BlogArticuloRepository extends JpaRepository<BlogArticulo, Long> {

    /**
     * Busca un artículo por su 'slug' (el identificador de la URL).
     * Es fundamental para que la vista pública (BlogArticuloView) encuentre el artículo.
     *
     * @param slug El identificador único de la URL (ej: "articulo-parkinson")
     * @return Un Optional que contiene el artículo si se encuentra
     */
    Optional<BlogArticulo> findBySlug(String slug);

    /**
     * Obtiene todos los artículos ordenados por la fecha de creación más reciente.
     * Útil para el panel de administrador.
     */
    List<BlogArticulo> findAllByOrderByFechaCreacionDesc();

    /**
     * Obtiene los 3 artículos más recientes.
     * Lo usaremos para alimentar la sección "Publicaciones Recientes" del HomeView.
     */
    List<BlogArticulo> findTop3ByOrderByFechaCreacionDesc();

}