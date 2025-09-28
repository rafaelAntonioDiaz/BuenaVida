package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("Paciente")
public class Paciente extends Usuario {
    @Column(name = "ruta_foto_perfil", length = 512)
    private String rutaFotoPerfil;

    @OneToOne(mappedBy = "paciente", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private HistoriaClinica historiaClinica;


    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sesion> sesiones;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documento> documentos;

    public Paciente() {
        super();
    }

    public Paciente(HistoriaClinica historiaClinica,
                    List<Sesion> sesiones, List<Documento> documentos) {
        this.historiaClinica = historiaClinica;
        this.sesiones = sesiones;
        this.documentos = documentos;
    }



    public void setHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
    }
    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public List<Sesion> getSesiones() {
        return sesiones;
    }

    public void setSesiones(List<Sesion> sesiones) {
        this.sesiones = sesiones;
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }

    public String getRutaFotoPerfil() {
        return rutaFotoPerfil;
    }

    public void setRutaFotoPerfil(String rutaFotoPerfil) {
        this.rutaFotoPerfil = rutaFotoPerfil;
    }
    @Override
    public String toString() {
        return "Paciente{" +
                "id=" + getId() +
                ", nombres='" + getNombres() + '\'' +
                ", apellidos='" + getApellidos() + '\'' +
                ", email='" + getUsername() + '\'' +
                ", celular='" + getCelular() + '\'' +
                ", fechaRegistro=" + getFechaRegistro() +
                '}';
    }
}
