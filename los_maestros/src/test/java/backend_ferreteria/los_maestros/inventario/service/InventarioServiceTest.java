package backend_ferreteria.los_maestros.inventario.service;

import backend_ferreteria.los_maestros.inventario.dto.InventarioDTO;
import backend_ferreteria.los_maestros.inventario.model.Inventario;
import backend_ferreteria.los_maestros.inventario.repository.InventarioRepository;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private InventarioService inventarioService;

    @Test
    void crearInventarioDeProductoExistente() {
        Producto producto = DatosDePrueba.producto();
        int cantidad = DatosDePrueba.cantidad();
        InventarioDTO dto = new InventarioDTO(producto.getSku(), null, cantidad, 5, null, null);
        when(productoRepository.findById(producto.getSku())).thenReturn(Optional.of(producto));
        when(inventarioRepository.existsById(producto.getSku())).thenReturn(false);
        when(inventarioRepository.saveAndFlush(any(Inventario.class))).thenAnswer(inv -> {
            Inventario i = inv.getArgument(0);
            i.setSku(i.getProducto().getSku());
            return i;
        });

        InventarioDTO resultado = inventarioService.crear(dto);

        assertThat(resultado.getSku()).isEqualTo(producto.getSku());
        assertThat(resultado.getNombreProducto()).isEqualTo(producto.getNombre());
        assertThat(resultado.getCantidad()).isEqualTo(cantidad);
        assertThat(resultado.getStockMinimo()).isEqualTo(5);
    }

    @Test
    void crearInventarioDeProductoInexistenteLanzaNoEncontrado() {
        String sku = DatosDePrueba.sku();
        InventarioDTO dto = new InventarioDTO(sku, null, 10, 2, null, null);
        when(productoRepository.findById(sku)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.crear(dto))
                .isInstanceOf(EntityNotFoundException.class);
        verify(inventarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void crearInventarioRepetidoLanzaConflicto() {
        Producto producto = DatosDePrueba.producto();
        InventarioDTO dto = new InventarioDTO(producto.getSku(), null, 10, 2, null, null);
        when(productoRepository.findById(producto.getSku())).thenReturn(Optional.of(producto));
        when(inventarioRepository.existsById(producto.getSku())).thenReturn(true);

        assertThatThrownBy(() -> inventarioService.crear(dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void entradaSumaAlStock() {
        Inventario inventario = DatosDePrueba.inventario(DatosDePrueba.producto(), 10, 3);
        int entrada = DatosDePrueba.faker().number().numberBetween(1, 100);
        when(inventarioRepository.findById(inventario.getSku())).thenReturn(Optional.of(inventario));
        when(inventarioRepository.saveAndFlush(inventario)).thenReturn(inventario);

        InventarioDTO resultado = inventarioService.registrarEntrada(inventario.getSku(), entrada);

        assertThat(resultado.getCantidad()).isEqualTo(10 + entrada);
    }

    @Test
    void salidaRestaDelStockYMarcaBajoStock() {
        Inventario inventario = DatosDePrueba.inventario(DatosDePrueba.producto(), 10, 3);
        when(inventarioRepository.findById(inventario.getSku())).thenReturn(Optional.of(inventario));
        when(inventarioRepository.saveAndFlush(inventario)).thenReturn(inventario);

        InventarioDTO resultado = inventarioService.registrarSalida(inventario.getSku(), 8);

        assertThat(resultado.getCantidad()).isEqualTo(2);
        assertThat(resultado.getBajoStock()).isTrue();
    }

    @Test
    void salidaMayorAlStockLanzaConflictoYNoGuarda() {
        Inventario inventario = DatosDePrueba.inventario(DatosDePrueba.producto(), 4, 1);
        when(inventarioRepository.findById(inventario.getSku())).thenReturn(Optional.of(inventario));

        assertThatThrownBy(() -> inventarioService.registrarSalida(inventario.getSku(), 5))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(inventario.getCantidad()).isEqualTo(4);
        verify(inventarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void descontarStockDeProductoSinInventarioLanzaConflicto() {
        String sku = DatosDePrueba.sku();
        when(inventarioRepository.findById(sku)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.descontarStock(sku, 1))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void descontarStockResta() {
        Inventario inventario = DatosDePrueba.inventario(DatosDePrueba.producto(), 10, 3);
        when(inventarioRepository.findById(inventario.getSku())).thenReturn(Optional.of(inventario));

        inventarioService.descontarStock(inventario.getSku().toLowerCase(), 4);

        assertThat(inventario.getCantidad()).isEqualTo(6);
        verify(inventarioRepository).saveAndFlush(inventario);
    }

    @Test
    void reponerStockSuma() {
        Inventario inventario = DatosDePrueba.inventario(DatosDePrueba.producto(), 10, 3);
        when(inventarioRepository.findById(inventario.getSku())).thenReturn(Optional.of(inventario));

        inventarioService.reponerStock(inventario.getSku(), 7);

        assertThat(inventario.getCantidad()).isEqualTo(17);
    }

    @Test
    void reponerStockDeProductoSinInventarioNoFalla() {
        String sku = DatosDePrueba.sku();
        when(inventarioRepository.findById(sku)).thenReturn(Optional.empty());

        inventarioService.reponerStock(sku, 5);

        verify(inventarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarConOtroSkuEnElBodyLanzaBadRequest() {
        Inventario inventario = DatosDePrueba.inventario(DatosDePrueba.producto(), 10, 3);
        InventarioDTO dto = new InventarioDTO(DatosDePrueba.sku(), null, 20, 5, null, null);
        when(inventarioRepository.findById(inventario.getSku())).thenReturn(Optional.of(inventario));

        assertThatThrownBy(() -> inventarioService.actualizar(inventario.getSku(), dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
