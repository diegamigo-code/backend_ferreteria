package backend_ferreteria.los_maestros.factura.repository;

import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import backend_ferreteria.los_maestros.factura.model.Factura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RepositoryFactura extends JpaRepository<Factura, Long> {

    @Query("""
            select f from Factura f
            where (:usuarioId is null or f.usuarioId = :usuarioId)
              and (:estado is null or f.estado = :estado)
              and (:desde is null or f.fecha >= :desde)
              and (:hasta is null or f.fecha <= :hasta)
            """)
    Page<Factura> buscar(@Param("usuarioId") Long usuarioId,
                         @Param("estado") EstadoFactura estado,
                         @Param("desde") LocalDateTime desde,
                         @Param("hasta") LocalDateTime hasta,
                         Pageable pageable);
}
