package com.ElihuAnalytics.ConsultorioAcupuntura.repositorio;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.NotificacionEnviada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificacionEnviadaRepository extends JpaRepository<NotificacionEnviada, Long> {

    @Query("SELECT COUNT(n) > 0 FROM NotificacionEnviada n " +
            "WHERE n.sesionId = :sesionId " +
            "AND n.tipo = :tipo " +
            "AND n.estado = 'ENVIADA' " +
            "AND n.fechaEnvio >= :desde")
    boolean existeNotificacionEnviada(@Param("sesionId") Long sesionId,
                                      @Param("tipo") NotificacionEnviada.TipoNotificacion tipo,
                                      @Param("desde") LocalDateTime desde);

    @Query("SELECT n FROM NotificacionEnviada n " +
            "WHERE n.estado = 'FALLIDA' " +
            "AND n.intentos < 3 " +
            "ORDER BY n.fechaEnvio ASC")
    List<NotificacionEnviada> findNotificacionesPendientesReintento();

    List<NotificacionEnviada> findBySesionIdAndTipo(Long sesionId, NotificacionEnviada.TipoNotificacion tipo);
}
