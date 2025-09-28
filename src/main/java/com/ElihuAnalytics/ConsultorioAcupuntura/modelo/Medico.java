package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Medico")
public class Medico extends Usuario {


    public Medico() {
        super();
    }

    public Medico(String username, String password, String celular,
                  String nombres, String apellidos) {
        super(username, password, celular, nombres, apellidos, Rol.MEDICO);
    }

    // Getters y setters

}
