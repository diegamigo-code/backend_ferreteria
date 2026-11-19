package backend_ferreteria.los_maestros.producto.service;

import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void listarTodosDevuelveUnDtoPorProducto() {
        List<Producto> productos = Stream.generate(DatosDePrueba::producto).limit(5).toList();
        when(productoRepository.findAll()).thenReturn(productos);

        List<ProductoDTO> resultado = productoService.listarTodos();

        assertThat(resultado).hasSize(5);
        assertThat(resultado).extracting(ProductoDTO::getSku)
                .containsExactlyElementsOf(productos.stream().map(Producto::getSku).toList());
    }

    @Test
    void crearGuardaElProductoConElSkuEnMayusculas() {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        String skuEsperado = dto.getSku();
        dto.setSku("  " + skuEsperado.toLowerCase() + " ");
        when(productoRepository.existsById(skuEsperado)).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoDTO resultado = productoService.crear(dto);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertThat(captor.getValue().getSku()).isEqualTo(skuEsperado);
        assertThat(resultado.getSku()).isEqualTo(skuEsperado);
        assertThat(resultado.getNombre()).isEqualTo(dto.getNombre());
        assertThat(resultado.getPrecio()).isEqualTo(dto.getPrecio());
    }

    @Test
    void crearConSkuRepetidoLanzaConflicto() {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        when(productoRepository.existsById(dto.getSku())).thenReturn(true);

        assertThatThrownBy(() -> productoService.crear(dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void obtenerPorSkuInexistenteLanzaNoEncontrado() {
        String sku = DatosDePrueba.sku();
        when(productoRepository.findById(sku)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorSku(sku))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(sku);
    }

    @Test
    void actualizarCambiaLosDatosPeroNoElSku() {
        Producto existente = DatosDePrueba.producto();
        ProductoDTO cambios = DatosDePrueba.productoDTO();
        cambios.setSku(existente.getSku());
        when(productoRepository.findById(existente.getSku())).thenReturn(Optional.of(existente));
        when(productoRepository.save(existente)).thenReturn(existente);

        ProductoDTO resultado = productoService.actualizar(existente.getSku(), cambios);

        assertThat(resultado.getSku()).isEqualTo(existente.getSku());
        assertThat(resultado.getNombre()).isEqualTo(cambios.getNombre());
        assertThat(resultado.getMarca()).isEqualTo(cambios.getMarca());
        assertThat(resultado.getCategoria()).isEqualTo(cambios.getCategoria());
        assertThat(resultado.getPrecio()).isEqualTo(cambios.getPrecio());
        assertThat(resultado.getDescripcion()).isEqualTo(cambios.getDescripcion());
    }

    @Test
    void actualizarConOtroSkuEnElBodyLanzaBadRequest() {
        Producto existente = DatosDePrueba.producto();
        ProductoDTO cambios = DatosDePrueba.productoDTO();
        when(productoRepository.findById(existente.getSku())).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> productoService.actualizar(existente.getSku(), cambios))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void eliminarBorraElProductoEncontrado() {
        Producto existente = DatosDePrueba.producto();
        when(productoRepository.findById(existente.getSku())).thenReturn(Optional.of(existente));

        productoService.eliminar(existente.getSku().toLowerCase());

        verify(productoRepository).delete(existente);
    }
}
