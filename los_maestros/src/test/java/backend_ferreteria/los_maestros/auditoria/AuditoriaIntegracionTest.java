package backend_ferreteria.los_maestros.auditoria;

import backend_ferreteria.los_maestros.auditoria.model.AccionAuditoria;
import backend_ferreteria.los_maestros.auditoria.model.Auditoria;
import backend_ferreteria.los_maestros.auditoria.repository.AuditoriaRepository;
import backend_ferreteria.los_maestros.inventario.dto.InventarioDTO;
import backend_ferreteria.los_maestros.inventario.repository.InventarioRepository;
import backend_ferreteria.los_maestros.inventario.service.InventarioService;
import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import backend_ferreteria.los_maestros.producto.service.ProductoService;
import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AuditoriaIntegracionTest {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @AfterEach
    void limpiar() {
        inventarioRepository.deleteAllInBatch();
        productoRepository.deleteAllInBatch();
        auditoriaRepository.deleteAllInBatch();
    }

    @Test
    void crearYActualizarProductoQuedaAuditado() {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        productoService.crear(dto);
        dto.setNombre(DatosDePrueba.nombre());
        productoService.actualizar(dto.getSku(), dto);

        List<Auditoria> registros = registros("Producto", dto.getSku());

        assertThat(registros).extracting(Auditoria::getAccion)
                .containsExactlyInAnyOrder(AccionAuditoria.CREAR, AccionAuditoria.ACTUALIZAR);
        assertThat(registros).allSatisfy(a -> {
            assertThat(a.getUsuario()).isEqualTo("anonimo");
            assertThat(a.getFecha()).isNotNull();
        });
    }

    @Test
    void eliminarProductoQuedaAuditado() {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        productoService.crear(dto);

        productoService.eliminar(dto.getSku());

        assertThat(registros("Producto", dto.getSku())).extracting(Auditoria::getAccion)
                .contains(AccionAuditoria.ELIMINAR);
    }

    @Test
    void unaOperacionQueFallaNoQuedaAuditada() {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        productoService.crear(dto);
        inventarioService.crear(new InventarioDTO(dto.getSku(), null, 3, 1, null, null));

        assertThatThrownBy(() -> productoService.crear(dto)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> inventarioService.registrarSalida(dto.getSku(), 10))
                .isInstanceOf(ResponseStatusException.class);

        assertThat(registros("Producto", dto.getSku())).hasSize(1);
        assertThat(registros("Inventario", dto.getSku())).extracting(Auditoria::getAccion)
                .containsExactly(AccionAuditoria.CREAR);
    }

    @Test
    void movimientosDeStockQuedanAuditados() {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        productoService.crear(dto);
        inventarioService.crear(new InventarioDTO(dto.getSku(), null, 10, 2, null, null));

        inventarioService.registrarEntrada(dto.getSku(), 5);
        inventarioService.registrarSalida(dto.getSku(), 3);

        assertThat(registros("Inventario", dto.getSku())).extracting(Auditoria::getAccion)
                .containsExactlyInAnyOrder(AccionAuditoria.CREAR, AccionAuditoria.ACTUALIZAR, AccionAuditoria.ACTUALIZAR);
    }

    private List<Auditoria> registros(String entidad, String entidadId) {
        return auditoriaRepository.buscar(entidad, entidadId, null, null, null, Pageable.unpaged()).getContent();
    }
}
