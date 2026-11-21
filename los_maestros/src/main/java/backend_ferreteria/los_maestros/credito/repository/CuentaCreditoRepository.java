package backend_ferreteria.los_maestros.credito.repository;

import backend_ferreteria.los_maestros.credito.model.CuentaCredito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaCreditoRepository extends JpaRepository<CuentaCredito, Long> {
}
