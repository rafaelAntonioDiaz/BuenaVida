package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String nombre;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String tipo; // pdf, imagen, etc.

    @NotBlank
    @Column(nullable = false, length = 500)
    private String rutaArchivo;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    public Documento() {
        this.fechaSubida = LocalDateTime.now();
    }

    public Documento(String nombre, String tipo, String rutaArchivo, LocalDateTime fechaSubida, Paciente paciente) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.rutaArchivo = rutaArchivo;
        this.fechaSubida = fechaSubida != null ? fechaSubida : LocalDateTime.now();
        this.paciente = paciente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Documento)) return false;
        Documento documento = (Documento) o;
        return Objects.equals(id, documento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Documento{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", rutaArchivo='" + rutaArchivo + '\'' +
                ", fechaSubida=" + fechaSubida +
                '}';
    }
}

