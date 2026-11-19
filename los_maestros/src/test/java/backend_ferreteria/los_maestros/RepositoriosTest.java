package backend_ferreteria.los_maestros;

import backend_ferreteria.los_maestros.auditoria.model.AccionAuditoria;
import backend_ferreteria.los_maestros.auditoria.model.Auditoria;
import backend_ferreteria.los_maestros.auditoria.repository.AuditoriaRepository;
import backend_ferreteria.los_maestros.inventario.model.Inventario;
import backend_ferreteria.los_maestros.inventario.repository.InventarioRepository;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositoriosTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void productoSeGuardaYSeBuscaPorSku() {
        Producto producto = productoRepository.saveAndFlush(DatosDePrueba.producto());
        entityManager.clear();

        assertThat(productoRepository.findById(producto.getSku()))
                .get()
                .extracting(Producto::getNombre)
                .isEqualTo(producto.getNombre());
    }

    @Test
    void buscarPorCategoriaYMarca() {
        Producto producto = DatosDePrueba.producto();
        productoRepository.saveAndFlush(producto);
        productoRepository.saveAndFlush(DatosDePrueba.producto());

        assertThat(productoRepository.findByCategoria(producto.getCategoria()))
                .extracting(Producto::getSku).contains(producto.getSku());
        assertThat(productoRepository.findByMarca(producto.getMarca()))
                .extracting(Producto::getSku).contains(producto.getSku());
    }

    @Test
    void inventarioUsaElSkuDelProductoComoClave() {
        Producto producto = productoRepository.saveAndFlush(DatosDePrueba.producto());
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(DatosDePrueba.cantidad());
        inventario.setStockMinimo(5);

        inventarioRepository.saveAndFlush(inventario);
        entityManager.clear();

        Inventario guardado = inventarioRepository.findById(producto.getSku()).orElseThrow();
        assertThat(guardado.getProducto().getNombre()).isEqualTo(producto.getNombre());
        assertThat(guardado.getUltimaActualizacion()).isNotNull();
    }

    @Test
    void bajoStockSoloDevuelveLosQueEstanEnOBajoElMinimo() {
        Producto bajo = productoRepository.save(DatosDePrueba.producto());
        Producto justo = productoRepository.save(DatosDePrueba.producto());
        Producto sobrado = productoRepository.save(DatosDePrueba.producto());
        guardarInventario(bajo, 2, 10);
        guardarInventario(justo, 10, 10);
        guardarInventario(sobrado, 50, 10);
        entityManager.flush();
        entityManager.clear();

        List<Inventario> resultado = inventarioRepository.findBajoStock();

        assertThat(resultado).extracting(Inventario::getSku)
                .containsExactlyInAnyOrder(bajo.getSku(), justo.getSku());
    }

    @Test
    void eliminarProductoEliminaSuInventario() {
        Producto producto = productoRepository.save(DatosDePrueba.producto());
        guardarInventario(producto, 10, 2);
        entityManager.flush();
        entityManager.clear();

        productoRepository.deleteById(producto.getSku());
        entityManager.flush();
        entityManager.clear();

        assertThat(inventarioRepository.findById(producto.getSku())).isEmpty();
    }

    @Test
    void auditoriaFiltraPorEntidadAccionYFechas() {
        LocalDateTime ahora = LocalDateTime.now();
        String sku = DatosDePrueba.sku();
        auditoriaRepository.save(new Auditoria(null, "Producto", sku, AccionAuditoria.CREAR, "anonimo", ahora.minusDays(3)));
        auditoriaRepository.save(new Auditoria(null, "Producto", sku, AccionAuditoria.ACTUALIZAR, "anonimo", ahora.minusDays(1)));
        auditoriaRepository.save(new Auditoria(null, "Inventario", sku, AccionAuditoria.CREAR, "anonimo", ahora));
        auditoriaRepository.flush();
        Pageable pagina = PageRequest.of(0, 20);

        Page<Auditoria> porEntidad = auditoriaRepository.buscar("producto", sku, null, null, null, pagina);
        Page<Auditoria> porAccion = auditoriaRepository.buscar(null, sku, AccionAuditoria.CREAR, null, null, pagina);
        Page<Auditoria> porFecha = auditoriaRepository.buscar(null, sku, null, ahora.minusDays(2), null, pagina);
        Page<Auditoria> sinFiltros = auditoriaRepository.buscar(null, null, null, null, null, pagina);

        assertThat(porEntidad.getTotalElements()).isEqualTo(2);
        assertThat(porAccion.getContent()).extracting(Auditoria::getEntidad)
                .containsExactlyInAnyOrder("Producto", "Inventario");
        assertThat(porFecha.getTotalElements()).isEqualTo(2);
        assertThat(sinFiltros.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    private void guardarInventario(Producto producto, int cantidad, int stockMinimo) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(cantidad);
        inventario.setStockMinimo(stockMinimo);
        inventarioRepository.save(inventario);
    }
}
