package backend_ferreteria.los_maestros.producto.repository;

import backend_ferreteria.los_maestros.producto.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    Page<Producto> findByCategoriaIgnoreCase(String categoria, Pageable pageable);

    Page<Producto> findByMarcaIgnoreCase(String marca, Pageable pageable);
}
