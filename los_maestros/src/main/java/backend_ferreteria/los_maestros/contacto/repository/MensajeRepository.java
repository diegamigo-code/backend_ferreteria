package backend_ferreteria.los_maestros.contacto.repository;

import backend_ferreteria.los_maestros.contacto.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
}
