package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import org.springframework.security.core.GrantedAuthority;

public enum Rol implements GrantedAuthority {
    PACIENTE, MEDICO, ADMINISTRADOR;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
