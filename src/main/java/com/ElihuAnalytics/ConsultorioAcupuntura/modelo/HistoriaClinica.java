package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp; // Import para la anotación de Hibernate.
import java.time.LocalDateTime;
import java.util.ArrayList;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;

@Entity
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "creada_el", nullable = false, updatable = false)
    private LocalDateTime creadaEl;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @OneToOne(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AntecedenteRelevante antecedenteRelevante;


    @Lob
    private String diagnosticoTradicional;

    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden_prescripcion")
    private List<Prescripcion> prescripciones = new ArrayList<>();

    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden_recomendacion")
    private List<Recomendacion> recomendaciones = new ArrayList<>();


    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SeguimientoSalud> seguimientos = new ArrayList<>();

    public HistoriaClinica() {
    }

    public void setAntecedenteRelevante(AntecedenteRelevante antecedenteRelevante) {
        if (antecedenteRelevante == null) {
            if (this.antecedenteRelevante != null) {
                this.antecedenteRelevante.setHistoriaClinica(null);
            }
        } else {
            antecedenteRelevante.setHistoriaClinica(this);
        }
        this.antecedenteRelevante = antecedenteRelevante;
    }
    // --- Getters y Setters ---
    public Long getId() { return id; }

    public LocalDateTime getCreadaEl() { return creadaEl; }

    public Paciente getPaciente() { return paciente; }

    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public AntecedenteRelevante getAntecedenteRelevante() { return antecedenteRelevante; }

    public List<Recomendacion> getRecomendaciones() { return recomendaciones; }

    public void setRecomendaciones(List<Recomendacion> recomendaciones) {
        for (Recomendacion r : new ArrayList<>(this.recomendaciones)) {
            removeRecomendacion(r);
        }
        if (recomendaciones != null) {
            for (Recomendacion r : recomendaciones) {
                addRecomendacion(r);
            }
        }
    }

    public void addRecomendacion(Recomendacion r) {
        if (r == null) return;
        if (!this.recomendaciones.contains(r)) {
            this.recomendaciones.add(r);
        }
        r.setHistoriaClinica(this);
    }

    public void removeRecomendacion(Recomendacion r) {
        if (r == null) return;
        this.recomendaciones.remove(r);
        if (r.getHistoriaClinica() == this) {
            r.setHistoriaClinica(null);
        }
    }

    public List<Prescripcion> getPrescripciones() { return prescripciones; }

    public void setPrescripciones(List<Prescripcion> prescripciones) {
        // Limpiar existentes respetando la relación inversa
        for (Prescripcion p : new ArrayList<>(this.prescripciones)) {
            removePrescripcion(p);
        }
        if (prescripciones != null) {
            for (Prescripcion p : prescripciones) {
                addPrescripcion(p);
            }
        }
    }

    public void addPrescripcion(Prescripcion p) {
        if (p == null) return;
        if (!this.prescripciones.contains(p)) {
            this.prescripciones.add(p);
        }
        p.setHistoriaClinica(this);
    }

    public void removePrescripcion(Prescripcion p) {
        if (p == null) return;
        this.prescripciones.remove(p);
        if (p.getHistoriaClinica() == this) {
            p.setHistoriaClinica(null);
        }
    }

    public List<SeguimientoSalud> getSeguimientos() { return seguimientos; }

    public void setSeguimientos(List<SeguimientoSalud> seguimientos) { this.seguimientos = seguimientos; }

    public String getDiagnosticoTradicional() {
        return diagnosticoTradicional;
    }

    public void setDiagnosticoTradicional(String diagnosticoTradicional) {
        this.diagnosticoTradicional = diagnosticoTradicional;
    }
}