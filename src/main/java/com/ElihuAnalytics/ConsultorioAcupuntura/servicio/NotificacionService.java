package com.ElihuAnalytics.ConsultorioAcupuntura.servicio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;

public interface NotificacionService {
    void enviarRecordatorioPaciente(Sesion sesion);
    void enviarRecordatorioMedico(Sesion sesion);
    void enviarConfirmacionPaciente(Sesion sesion, String mensaje);
}