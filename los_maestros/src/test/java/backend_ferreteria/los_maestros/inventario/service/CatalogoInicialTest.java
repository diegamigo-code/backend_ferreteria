package backend_ferreteria.los_maestros.inventario.service;

import backend_ferreteria.los_maestros.inventario.model.Inventario;
import backend_ferreteria.los_maestros.inventario.repository.InventarioRepository;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoInicialTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private CatalogoInicial catalogoInicial;

    @Test
    void cargaLosProductosConSuStockSiLaBaseEstaVacia() throws Exception {
        when(productoRepository.count()).thenReturn(0L);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        catalogoInicial.run(null);

        ArgumentCaptor<Producto> productos = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository, atLeastOnce()).save(productos.capture());
        ArgumentCaptor<Inventario> inventarios = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository, atLeastOnce()).save(inventarios.capture());

        assertThat(productos.getAllValues()).hasSize(33);
        assertThat(inventarios.getAllValues()).hasSize(33);
        Producto cemento = productos.getAllValues().getFirst();
        assertThat(cemento.getSku()).isEqualTo("CON-001");
        assertThat(cemento.getImagen()).isEqualTo("assets/saco_cemento_melon.png");
        assertThat(inventarios.getAllValues().getFirst().getCantidad()).isEqualTo(120);
    }

    @Test
    void noHaceNadaSiYaHayProductos() throws Exception {
        when(productoRepository.count()).thenReturn(5L);

        catalogoInicial.run(null);

        verify(productoRepository, never()).save(any(Producto.class));
        verifyNoInteractions(inventarioRepository);
    }
}
