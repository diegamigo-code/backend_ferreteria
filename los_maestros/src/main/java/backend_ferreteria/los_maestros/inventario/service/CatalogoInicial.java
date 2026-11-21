package backend_ferreteria.los_maestros.inventario.service;

import backend_ferreteria.los_maestros.inventario.model.Inventario;
import backend_ferreteria.los_maestros.inventario.repository.InventarioRepository;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.catalogo.cargar-inicial", havingValue = "true", matchIfMissing = true)
public class CatalogoInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogoInicial.class);

    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;

    public CatalogoInicial(ProductoRepository productoRepository, InventarioRepository inventarioRepository) {
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        if (productoRepository.count() > 0) {
            return;
        }
        List<ProductoInicial> productos;
        try (InputStream archivo = new ClassPathResource("catalogo-inicial.json").getInputStream()) {
            productos = JsonMapper.builder().build().readValue(archivo, new TypeReference<>() {
            });
        }
        for (ProductoInicial p : productos) {
            Producto producto = productoRepository.save(new Producto(p.sku(), p.nombre(), p.marca(), p.categoria(),
                    p.precio(), p.descripcion(), p.precioNormal(), p.imagen()));
            Inventario inventario = new Inventario();
            inventario.setProducto(producto);
            inventario.setCantidad(p.stock());
            inventario.setStockMinimo(p.stockMinimo());
            inventarioRepository.save(inventario);
        }
        log.info("Catálogo inicial cargado: {} productos", productos.size());
    }

    record ProductoInicial(String sku, String nombre, String marca, String categoria, Long precio,
                           Long precioNormal, String descripcion, String imagen, Integer stock,
                           Integer stockMinimo) {
    }
}
