// java
package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Nota privada del médico sobre el tratamiento del paciente.
 * - No visible para el paciente (solo se usa en vistas del médico).
 * - Persistida y asociada a la HistoriaClínica.
 */
@Entity
@Table(name = "notas_privadas", indexes = {
        @Index(name = "idx_notaprivada_hc_fecha", columnList = "historia_clinica_id, fecha_hora DESC")
})
public class NotaPrivada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sello temporal al crear la nota (se puede usar también como ordenación)
    @CreationTimestamp
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Lob
    @Column(name = "texto", nullable = false)
    private String texto;

    // Relación N:1 con HistoriaClinica. Muchas notas pertenecen a una historia.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_clinica_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_nota_privada_hc_id"))
    private HistoriaClinica historiaClinica;

    public NotaPrivada() {}

    public NotaPrivada(HistoriaClinica historiaClinica, String texto) {
        this.historiaClinica = historiaClinica;
        this.texto = texto;
    }

    public Long getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public HistoriaClinica getHistoriaClinica() { return historiaClinica; }
    public void setHistoriaClinica(HistoriaClinica historiaClinica) { this.historiaClinica = historiaClinica; }
}