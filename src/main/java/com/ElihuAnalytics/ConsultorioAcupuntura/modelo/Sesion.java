package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa una sesión o cita en el consultorio.
 */
@Entity
@Table(name = "sesion")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fecha; // Fecha y hora de la cita

    @NotBlank
    @Column(nullable = false, length = 500)
    private String motivo; // Motivo de la cita

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSesion estado; // Estado de la cita (PROGRAMADA, CONFIRMADA, etc.)

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente; // Paciente asociado a la cita

    @Column(length = 255)
    private String lugar; // Lugar opcional de la cita

    @NotNull
    @Column(nullable = false)
    private Duration duracion = Duration.ofHours(1); // Duración de la cita (1 hora por defecto)

    public Sesion() {
    }

    public Sesion(LocalDateTime fecha, String motivo, EstadoSesion estado, Paciente paciente) {
        this.fecha = fecha;
        this.motivo = motivo;
        this.estado = estado;
        this.paciente = paciente;
        this.duracion = Duration.ofHours(1);
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoSesion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSesion estado) {
        this.estado = estado;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public Duration getDuracion() {
        return duracion;
    }

    public void setDuracion(Duration duracion) {
        this.duracion = duracion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sesion)) return false;
        Sesion sesion = (Sesion) o;
        return Objects.equals(id, sesion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sesion{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", motivo='" + motivo + '\'' +
                ", estado=" + estado +
                ", duracion=" + duracion +
                '}';
    }

    public enum EstadoSesion {
        PROGRAMADA,
        CONFIRMADA,
        REALIZADA,
        CANCELADA
    }
}