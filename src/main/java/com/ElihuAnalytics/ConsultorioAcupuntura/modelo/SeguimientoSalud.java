package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "seguimiento_salud") // Recomendado: nombre explícito de la tabla principal
public class SeguimientoSalud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha = LocalDateTime.now();

    @Lob
    private String descripcion;

    // Archivos opcionales que el paciente puede subir
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "seguimiento_salud_archivos",
            joinColumns = @JoinColumn(name = "seguimiento_id"))
    @Column(name = "ruta", length = 191)
    private List<String> archivos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "seguimiento_adjuntos",
            joinColumns = @JoinColumn(name = "seguimiento_id")
    )
    @MapKeyColumn(name = "ruta", length = 191)
    @Column(name = "descripcion")
    private Map<String, String> descripcionesPorRuta = new LinkedHashMap<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "historia_clinica_id")
    private HistoriaClinica historiaClinica;

    public SeguimientoSalud() {
    }

    public SeguimientoSalud(String descripcion, HistoriaClinica historiaClinica) {
        this.descripcion = descripcion;
        this.historiaClinica = historiaClinica;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getArchivos() {
        return archivos;
    }

    public void setArchivos(List<String> archivos) {
        this.archivos = archivos;
    }

    public Map<String, String> getDescripcionesPorRuta() { return descripcionesPorRuta; }
    public void setDescripcionesPorRuta(Map<String, String> descripcionesPorRuta) { this.descripcionesPorRuta = descripcionesPorRuta; }

    public HistoriaClinica getHistoriaClinica() { return historiaClinica; }
    public void setHistoriaClinica(HistoriaClinica historiaClinica) { this.historiaClinica = historiaClinica; }
}
