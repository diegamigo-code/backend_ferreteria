package backend_ferreteria.los_maestros.factura.service;

import backend_ferreteria.los_maestros.factura.dto.EmitirFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.FacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.LineaSolicitudDTO;
import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import backend_ferreteria.los_maestros.factura.model.Factura;
import backend_ferreteria.los_maestros.factura.model.LineaFactura;
import backend_ferreteria.los_maestros.factura.repository.RepositoryFactura;
import backend_ferreteria.los_maestros.inventario.InventarioApi;
import backend_ferreteria.los_maestros.producto.ProductoApi;
import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private RepositoryFactura repositoryFactura;

    @Mock
    private UsuarioApi usuarioApi;

    @Mock
    private ProductoApi productoApi;

    @Mock
    private InventarioApi inventarioApi;

    @InjectMocks
    private FacturaService facturaService;

    @Test
    void emitirCalculaSubtotalesTotalYDescuentaStock() {
        UserDTO cliente = cliente();
        ProductoDTO martillo = producto(5_990L);
        ProductoDTO clavos = producto(1_500L);
        when(usuarioApi.obtenerPorId(cliente.getId())).thenReturn(cliente);
        when(productoApi.obtenerPorSku(martillo.getSku())).thenReturn(martillo);
        when(productoApi.obtenerPorSku(clavos.getSku())).thenReturn(clavos);
        when(repositoryFactura.save(any(Factura.class))).thenAnswer(inv -> guardada(inv.getArgument(0)));

        FacturaDTO factura = facturaService.emitir(new EmitirFacturaDTO(cliente.getId(), List.of(
                new LineaSolicitudDTO(martillo.getSku(), 2),
                new LineaSolicitudDTO(clavos.getSku(), 3))));

        assertThat(factura.getNumero()).isEqualTo(1L);
        assertThat(factura.getEstado()).isEqualTo(EstadoFactura.EMITIDA);
        assertThat(factura.getClienteNombre()).isEqualTo(cliente.getNombre());
        assertThat(factura.getLineas()).hasSize(2);
        assertThat(factura.getLineas().get(0).getSubtotal()).isEqualTo(11_980L);
        assertThat(factura.getLineas().get(1).getSubtotal()).isEqualTo(4_500L);
        assertThat(factura.getTotal()).isEqualTo(16_480L);
        assertThat(factura.getNeto() + factura.getIva()).isEqualTo(factura.getTotal());
        verify(inventarioApi).descontarStock(martillo.getSku(), 2);
        verify(inventarioApi).descontarStock(clavos.getSku(), 3);
    }

    @Test
    void emitirJuntaLasLineasDelMismoSku() {
        UserDTO cliente = cliente();
        ProductoDTO producto = producto(1_000L);
        when(usuarioApi.obtenerPorId(cliente.getId())).thenReturn(cliente);
        when(productoApi.obtenerPorSku(producto.getSku())).thenReturn(producto);
        when(repositoryFactura.save(any(Factura.class))).thenAnswer(inv -> guardada(inv.getArgument(0)));

        FacturaDTO factura = facturaService.emitir(new EmitirFacturaDTO(cliente.getId(), List.of(
                new LineaSolicitudDTO(producto.getSku(), 2),
                new LineaSolicitudDTO(producto.getSku().toLowerCase(), 5))));

        assertThat(factura.getLineas()).hasSize(1);
        assertThat(factura.getLineas().get(0).getCantidad()).isEqualTo(7);
        verify(inventarioApi).descontarStock(producto.getSku(), 7);
    }

    @Test
    void elIvaEsEl19PorCientoIncluidoEnElTotal() {
        assertThat(FacturaService.calcularNeto(11_900L)).isEqualTo(10_000L);
        assertThat(FacturaService.calcularNeto(0L)).isZero();
    }

    @Test
    void emitirParaUsuarioInexistenteNoTocaElStock() {
        when(usuarioApi.obtenerPorId(99L)).thenThrow(new EntityNotFoundException("Usuario no encontrado con id 99"));

        assertThatThrownBy(() -> facturaService.emitir(new EmitirFacturaDTO(99L,
                List.of(new LineaSolicitudDTO(DatosDePrueba.sku(), 1)))))
                .isInstanceOf(EntityNotFoundException.class);
        verify(inventarioApi, never()).descontarStock(anyString(), anyInt());
        verify(repositoryFactura, never()).save(any());
    }

    @Test
    void emitirSinStockNoGuardaLaFactura() {
        UserDTO cliente = cliente();
        ProductoDTO producto = producto(2_000L);
        when(usuarioApi.obtenerPorId(cliente.getId())).thenReturn(cliente);
        when(productoApi.obtenerPorSku(producto.getSku())).thenReturn(producto);
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente"))
                .when(inventarioApi).descontarStock(producto.getSku(), 50);

        assertThatThrownBy(() -> facturaService.emitir(new EmitirFacturaDTO(cliente.getId(),
                List.of(new LineaSolicitudDTO(producto.getSku(), 50)))))
                .isInstanceOf(ResponseStatusException.class);
        verify(repositoryFactura, never()).save(any());
    }

    @Test
    void anularReponeElStockYMarcaLaFactura() {
        Factura factura = facturaEmitida();
        when(repositoryFactura.findById(1L)).thenReturn(Optional.of(factura));
        when(repositoryFactura.saveAndFlush(factura)).thenReturn(factura);

        FacturaDTO resultado = facturaService.anular(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoFactura.ANULADA);
        assertThat(resultado.getFechaAnulacion()).isNotNull();
        for (LineaFactura linea : factura.getLineas()) {
            verify(inventarioApi).reponerStock(linea.getSku(), linea.getCantidad());
        }
    }

    @Test
    void anularUnaFacturaYaAnuladaLanzaConflicto() {
        Factura factura = facturaEmitida();
        factura.setEstado(EstadoFactura.ANULADA);
        when(repositoryFactura.findById(1L)).thenReturn(Optional.of(factura));

        assertThatThrownBy(() -> facturaService.anular(1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verify(inventarioApi, never()).reponerStock(anyString(), anyInt());
    }

    @Test
    void obtenerFacturaInexistenteLanzaNoEncontrado() {
        when(repositoryFactura.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facturaService.obtenerPorNumero(7L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private static UserDTO cliente() {
        UserDTO cliente = DatosDePrueba.usuarioDTO();
        cliente.setId(DatosDePrueba.faker().number().numberBetween(1L, 1_000L));
        return cliente;
    }

    private static ProductoDTO producto(long precio) {
        ProductoDTO producto = DatosDePrueba.productoDTO();
        producto.setPrecio(precio);
        return producto;
    }

    private static Factura guardada(Factura factura) {
        factura.setNumero(1L);
        factura.setFecha(LocalDateTime.now());
        return factura;
    }

    private static Factura facturaEmitida() {
        Factura factura = new Factura();
        factura.setNumero(1L);
        factura.setUsuarioId(1L);
        factura.setClienteNombre(DatosDePrueba.faker().name().fullName());
        factura.setClienteEmail(DatosDePrueba.email());
        factura.setEstado(EstadoFactura.EMITIDA);
        factura.setFecha(LocalDateTime.now());
        for (int i = 0; i < 3; i++) {
            int cantidad = DatosDePrueba.faker().number().numberBetween(1, 10);
            long precio = DatosDePrueba.precio();
            factura.agregarLinea(new LineaFactura(null, null, DatosDePrueba.sku(), DatosDePrueba.nombre(),
                    precio, cantidad, precio * cantidad));
        }
        long total = factura.getLineas().stream().mapToLong(LineaFactura::getSubtotal).sum();
        factura.setTotal(total);
        factura.setNeto(FacturaService.calcularNeto(total));
        factura.setIva(total - factura.getNeto());
        return factura;
    }
}
