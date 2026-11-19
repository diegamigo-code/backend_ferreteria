package backend_ferreteria.los_maestros.factura;

import backend_ferreteria.los_maestros.auditoria.repository.AuditoriaRepository;
import backend_ferreteria.los_maestros.factura.dto.EmitirFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.FacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.LineaSolicitudDTO;
import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import backend_ferreteria.los_maestros.factura.repository.RepositoryFactura;
import backend_ferreteria.los_maestros.factura.service.FacturaService;
import backend_ferreteria.los_maestros.inventario.dto.InventarioDTO;
import backend_ferreteria.los_maestros.inventario.repository.InventarioRepository;
import backend_ferreteria.los_maestros.inventario.service.InventarioService;
import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import backend_ferreteria.los_maestros.producto.service.ProductoService;
import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import backend_ferreteria.los_maestros.user.repository.UserRepository;
import backend_ferreteria.los_maestros.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FacturaIntegracionTest {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private UserService userService;

    @Autowired
    private RepositoryFactura repositoryFactura;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @AfterEach
    void limpiar() {
        repositoryFactura.deleteAll();
        inventarioRepository.deleteAllInBatch();
        productoRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        auditoriaRepository.deleteAllInBatch();
    }

    @Test
    void emitirDescuentaElStockYGuardaLaFactura() {
        UserDTO cliente = userService.crear(DatosDePrueba.usuarioDTO());
        ProductoDTO martillo = productoConStock(5_990L, 10);
        ProductoDTO clavos = productoConStock(1_500L, 100);

        FacturaDTO factura = facturaService.emitir(new EmitirFacturaDTO(cliente.getId(), List.of(
                new LineaSolicitudDTO(martillo.getSku(), 2),
                new LineaSolicitudDTO(clavos.getSku(), 30))));

        assertThat(factura.getNumero()).isNotNull();
        assertThat(factura.getFecha()).isNotNull();
        assertThat(factura.getTotal()).isEqualTo(2 * 5_990L + 30 * 1_500L);
        assertThat(stock(martillo)).isEqualTo(8);
        assertThat(stock(clavos)).isEqualTo(70);

        FacturaDTO guardada = facturaService.obtenerPorNumero(factura.getNumero());
        assertThat(guardada.getLineas()).hasSize(2);
        assertThat(guardada.getClienteEmail()).isEqualTo(cliente.getEmail());
    }

    @Test
    void siUnaLineaNoTieneStockNoSeDescuentaNadaNiSeGuardaLaFactura() {
        UserDTO cliente = userService.crear(DatosDePrueba.usuarioDTO());
        ProductoDTO conStock = productoConStock(1_000L, 50);
        ProductoDTO sinStock = productoConStock(2_000L, 1);

        assertThatThrownBy(() -> facturaService.emitir(new EmitirFacturaDTO(cliente.getId(), List.of(
                new LineaSolicitudDTO(conStock.getSku(), 5),
                new LineaSolicitudDTO(sinStock.getSku(), 3)))))
                .isInstanceOf(ResponseStatusException.class);

        assertThat(stock(conStock)).isEqualTo(50);
        assertThat(stock(sinStock)).isEqualTo(1);
        assertThat(repositoryFactura.count()).isZero();
    }

    @Test
    void anularDevuelveElStock() {
        UserDTO cliente = userService.crear(DatosDePrueba.usuarioDTO());
        ProductoDTO producto = productoConStock(3_000L, 20);
        FacturaDTO factura = facturaService.emitir(new EmitirFacturaDTO(cliente.getId(),
                List.of(new LineaSolicitudDTO(producto.getSku(), 6))));
        assertThat(stock(producto)).isEqualTo(14);

        FacturaDTO anulada = facturaService.anular(factura.getNumero());

        assertThat(anulada.getEstado()).isEqualTo(EstadoFactura.ANULADA);
        assertThat(stock(producto)).isEqualTo(20);
        assertThatThrownBy(() -> facturaService.anular(factura.getNumero()))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(stock(producto)).isEqualTo(20);
    }

    @Test
    void laFacturaConservaElPrecioAunqueElProductoCambie() {
        UserDTO cliente = userService.crear(DatosDePrueba.usuarioDTO());
        ProductoDTO producto = productoConStock(4_000L, 10);
        FacturaDTO factura = facturaService.emitir(new EmitirFacturaDTO(cliente.getId(),
                List.of(new LineaSolicitudDTO(producto.getSku(), 1))));

        producto.setPrecio(9_999L);
        productoService.actualizar(producto.getSku(), producto);

        assertThat(facturaService.obtenerPorNumero(factura.getNumero()).getTotal()).isEqualTo(4_000L);
    }

    @Test
    void buscarFiltraPorClienteYEstado() {
        UserDTO ana = userService.crear(DatosDePrueba.usuarioDTO());
        UserDTO luis = userService.crear(DatosDePrueba.usuarioDTO());
        ProductoDTO producto = productoConStock(1_000L, 100);
        FacturaDTO deAna = facturaService.emitir(new EmitirFacturaDTO(ana.getId(),
                List.of(new LineaSolicitudDTO(producto.getSku(), 1))));
        facturaService.emitir(new EmitirFacturaDTO(ana.getId(), List.of(new LineaSolicitudDTO(producto.getSku(), 1))));
        facturaService.emitir(new EmitirFacturaDTO(luis.getId(), List.of(new LineaSolicitudDTO(producto.getSku(), 1))));
        facturaService.anular(deAna.getNumero());

        assertThat(facturaService.buscar(ana.getId(), null, null, null, PageRequest.of(0, 20)).getTotalElements())
                .isEqualTo(2);
        assertThat(facturaService.buscar(ana.getId(), EstadoFactura.EMITIDA, null, null, PageRequest.of(0, 20))
                .getTotalElements()).isEqualTo(1);
        assertThat(facturaService.buscar(null, EstadoFactura.ANULADA, null, null, PageRequest.of(0, 20))
                .getContent()).extracting(FacturaDTO::getNumero).containsExactly(deAna.getNumero());
    }

    private ProductoDTO productoConStock(long precio, int cantidad) {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        dto.setPrecio(precio);
        ProductoDTO producto = productoService.crear(dto);
        inventarioService.crear(new InventarioDTO(producto.getSku(), null, cantidad, 0, null, null));
        return producto;
    }

    private int stock(ProductoDTO producto) {
        return inventarioService.obtenerPorSku(producto.getSku()).getCantidad();
    }
}
