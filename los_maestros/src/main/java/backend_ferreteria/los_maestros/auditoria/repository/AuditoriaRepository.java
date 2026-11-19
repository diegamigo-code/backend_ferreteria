package backend_ferreteria.los_maestros.auditoria.repository;

import backend_ferreteria.los_maestros.auditoria.model.AccionAuditoria;
import backend_ferreteria.los_maestros.auditoria.model.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    @Query("""
            select a from Auditoria a
            where (:entidad is null or lower(a.entidad) = lower(:entidad))
              and (:entidadId is null or a.entidadId = :entidadId)
              and (:accion is null or a.accion = :accion)
              and (:desde is null or a.fecha >= :desde)
              and (:hasta is null or a.fecha <= :hasta)
            """)
    Page<Auditoria> buscar(@Param("entidad") String entidad,
                           @Param("entidadId") String entidadId,
                           @Param("accion") AccionAuditoria accion,
                           @Param("desde") LocalDateTime desde,
                           @Param("hasta") LocalDateTime hasta,
                           Pageable pageable);
}
