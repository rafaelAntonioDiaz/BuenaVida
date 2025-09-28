package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // Asegura que el usuario no sea nulo
    private String email; // Email o celular

    @Column(nullable = false) // Asegura que el código no sea nulo
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime generadoEl;

    @Column(nullable = false)
    private LocalDateTime expiraEl; // Nueva fecha de expiración

    @Column(nullable = false)
    private boolean verificado;

    // Duración de la validez del código (ej. 5 minutos)
    private static final java.time.Duration VALIDEZ_CODIGO = java.time.Duration.ofMinutes(5);

    public CodigoVerificacion() {
        this.generadoEl = LocalDateTime.now();
        this.expiraEl = this.generadoEl.plus(VALIDEZ_CODIGO); // Calcula la fecha de expiración
        this.verificado = false;
    }

    public CodigoVerificacion(String email, String codigo) {
        this();
        this.email = email;
        this.codigo = codigo;
    }

    // === Getters y setters ===

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getGeneradoEl() {
        return generadoEl;
    }

    public LocalDateTime getExpiraEl() {
        return expiraEl;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    /**
     * Método para verificar si el código ha expirado.
     * @return true si el código ha expirado, false en caso contrario.
     */
    public boolean haExpirado() {
        return LocalDateTime.now().isAfter(this.expiraEl);
    }
}