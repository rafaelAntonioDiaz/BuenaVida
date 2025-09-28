package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class Prescripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha = LocalDateTime.now();

    @Lob
    private String indicaciones;

    @ManyToOne(optional = false)
    @JoinColumn(name = "historia_clinica_id")
    private HistoriaClinica historiaClinica;

    public Prescripcion() {
    }

    public Prescripcion(String indicaciones, HistoriaClinica historiaClinica) {
        this.indicaciones = indicaciones;
        this.historiaClinica = historiaClinica;
    }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaStr = fecha != null ? fecha.format(f) : "";
        String texto = indicaciones != null ? indicaciones : "";
        return (fechaStr.isEmpty() ? "" : (fechaStr + ": ")) + texto;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public void setHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
    }
}
