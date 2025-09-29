package com.ElihuAnalytics.ConsultorioAcupuntura.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion_enviada")
public class NotificacionEnviada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sesion_id", nullable = false)
    private Long sesionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotificacion canal;

    @Column(name = "destinatario", length = 100)
    private String destinatario; // email o teléfono

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNotificacion estado;

    @Column(name = "mensaje_error", length = 500)
    private String mensajeError;

    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    // Constructores
    public NotificacionEnviada() {}

    public NotificacionEnviada(Long sesionId, TipoNotificacion tipo, CanalNotificacion canal, String destinatario) {
        this.sesionId = sesionId;
        this.tipo = tipo;
        this.canal = canal;
        this.destinatario = destinatario;
        this.fechaEnvio = LocalDateTime.now();
        this.estado = EstadoNotificacion.PENDIENTE;
        this.intentos = 0;
    }

    // Enums
    public enum TipoNotificacion {
        RECORDATORIO_PACIENTE,
        RECORDATORIO_MEDICO,
        CONFIRMACION_CITA,
        CANCELACION_CITA,
        REPROGRAMACION_CITA,
        CONFIRMACION_PACIENTE
    }

    public enum CanalNotificacion {
        WHATSAPP,
        PUSH,
        EMAIL,
        SMS
    }

    public enum EstadoNotificacion {
        PENDIENTE,
        ENVIADA,
        FALLIDA,
        REINTENTANDO
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSesionId() { return sesionId; }
    public void setSesionId(Long sesionId) { this.sesionId = sesionId; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public CanalNotificacion getCanal() { return canal; }
    public void setCanal(CanalNotificacion canal) { this.canal = canal; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public EstadoNotificacion getEstado() { return estado; }
    public void setEstado(EstadoNotificacion estado) { this.estado = estado; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }

    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer intentos) { this.intentos = intentos; }

    public void incrementarIntentos() { this.intentos++; }
}