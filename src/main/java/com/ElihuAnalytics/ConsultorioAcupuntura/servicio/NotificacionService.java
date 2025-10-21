package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;

import java.time.LocalDateTime;

public interface NotificacionService {
    void enviarRecordatorioPaciente(Sesion sesion);
    void enviarRecordatorioMedico(Sesion sesion);
    void enviarConfirmacionPaciente(Sesion sesion, String mensaje);
    void enviarNotificacionProgramacionMedico(Sesion sesion);
    void enviarCancelacion(Sesion sesion);// Añadido
    void enviarReprogramacionCita(Sesion sesion, LocalDateTime fechaAnterior);
}