package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String motivo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSesion estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente", nullable = false)
    private Paciente paciente;

    //lugar/dirección de la sesión (lo carga el paciente)
    @Column(length = 255)
    private String lugar;


    public Sesion() {
    }

    public Sesion(LocalDateTime fecha, String motivo, EstadoSesion estado, Paciente paciente) {
        this.fecha = fecha;
        this.motivo = motivo;
        this.estado = estado;
        this.paciente = paciente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getFecha() {
        return fecha;
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
                '}';
    }

    public enum EstadoSesion {
        PROGRAMADA,
        REALIZADA,
        CANCELADA,
        CONFIRMADA
    }

}
