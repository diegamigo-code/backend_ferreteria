package backend_ferreteria.los_maestros.inventario.repository;

import backend_ferreteria.los_maestros.inventario.model.Inventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, String> {

    @Override
    @EntityGraph(attributePaths = "producto")
    Page<Inventario> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "producto")
    Optional<Inventario> findById(String sku);

    @EntityGraph(attributePaths = "producto")
    @Query("select i from Inventario i where i.cantidad <= i.stockMinimo order by i.cantidad")
    List<Inventario> findBajoStock();
}
