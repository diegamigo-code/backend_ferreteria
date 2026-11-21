package backend_ferreteria.los_maestros.credito.repository;

import backend_ferreteria.los_maestros.credito.model.MovimientoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoCreditoRepository extends JpaRepository<MovimientoCredito, Long> {

    List<MovimientoCredito> findByUsuarioIdOrderByIdAsc(Long usuarioId);
}
