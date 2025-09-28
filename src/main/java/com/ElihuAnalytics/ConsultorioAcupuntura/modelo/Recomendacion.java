package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha = LocalDateTime.now();

    @Lob
    private String contenido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "historia_clinica_id")
    private HistoriaClinica historiaClinica;

    public Recomendacion() {
    }

    public Recomendacion(String contenido, HistoriaClinica historiaClinica) {
        this.contenido = contenido;
        this.historiaClinica = historiaClinica;
    }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaStr = fecha != null ? fecha.format(f) : "";
        String texto = contenido != null ? contenido : "";
        return (fechaStr.isEmpty() ? "" : (fechaStr + ": ")) + texto;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public void setHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
    }
}
