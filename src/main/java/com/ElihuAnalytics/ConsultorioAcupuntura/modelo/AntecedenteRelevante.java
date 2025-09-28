package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "antecedente_relevante")
public class AntecedenteRelevante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "creado_el", nullable = false, updatable = false)
    private LocalDateTime creadoEl;

    @Lob
    @Column(name = "descripcion")
    private String descripcion;

    /**
     * Rutas de archivos (lista simple).
     * - Usamos @CollectionTable con nombre fijo y FK nombrada para estabilidad.
     * - MUY IMPORTANTE: limitar la longitud de 'ruta' a 191 para que la PK/índice no exceda 3072 bytes en utf8mb4.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "antecedente_relevante_rutas_archivos",
            joinColumns = @JoinColumn(name = "antecedente_id")
    )
    @Column(name = "ruta", length = 191)
    private List<String> rutasArchivos = new ArrayList<>();

    /**
     * Mapa ruta -> descripción (colección de elementos).
     * - Tabla fija 'antecedente_adjuntos' (la que te arroja error cuando no se crea).
     * - Limitar la longitud de la clave (ruta) a 191 para que la PK/UK sea válida en MySQL utf8mb4.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "antecedente_adjuntos",
            joinColumns = @JoinColumn(name = "antecedente_id"))
    @MapKeyColumn(name = "ruta", length = 191)
    @Column(name = "descripcion")
    private Map<String, String> descripcionesPorRuta = new LinkedHashMap<>();

    /**
     * Lado propietario del 1:1 con HistoriaClinica.
     * - Solo aquí usamos @JoinColumn para evitar FK/UK duplicadas.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historia_clinica_id", nullable = false)
    private HistoriaClinica historiaClinica;

    public AntecedenteRelevante() { }

    public AntecedenteRelevante(String descripcion, HistoriaClinica historiaClinica) {
        this.descripcion = descripcion;
        this.historiaClinica = historiaClinica;
    }

    // Getters/setters estándar
    public Long getId() { return id; }
    public LocalDateTime getCreadoEl() { return creadoEl; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public HistoriaClinica getHistoriaClinica() { return historiaClinica; }
    public void setHistoriaClinica(HistoriaClinica historiaClinica) { this.historiaClinica = historiaClinica; }

    public List<String> getRutasArchivos() { return rutasArchivos; }
    public void setRutasArchivos(List<String> rutasArchivos) { this.rutasArchivos = rutasArchivos; }

    public Map<String, String> getDescripcionesPorRuta() { return descripcionesPorRuta; }
    public void setDescripcionesPorRuta(Map<String, String> descripcionesPorRuta) { this.descripcionesPorRuta = descripcionesPorRuta; }
}