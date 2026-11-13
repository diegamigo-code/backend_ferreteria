package backend_ferreteria.los_maestros.producto.repository;

import backend_ferreteria.los_maestros.producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** El SKU (String) es la clave primaria de Producto. */
public interface ProductoRepository extends JpaRepository<Producto, String> {

    List<Producto> findByCategoria(String categoria);

    List<Producto> findByMarca(String marca);
}
