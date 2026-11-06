package backend_ferreteria.los_maestros.producto.repository;

import backend_ferreteria.los_maestros.producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoria(String categoria);

    List<Producto> findByMarca(String marca);
}
