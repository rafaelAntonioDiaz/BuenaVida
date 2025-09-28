package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Administrador")
public class Administrador extends Usuario {

    public Administrador() {
        super();
    }

    public Administrador(String username, String password, String celular,
                         String nombres, String apellidos) {
        super(username, password, celular, nombres, apellidos, Rol.ADMINISTRADOR);
    }
}
