package backend_ferreteria.los_maestros.inventario.service;

import backend_ferreteria.los_maestros.inventario.dto.InventarioDTO;
import backend_ferreteria.los_maestros.inventario.model.Inventario;
import backend_ferreteria.los_maestros.inventario.repository.InventarioRepository;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioService(InventarioRepository inventarioRepository,
                             ProductoRepository productoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<InventarioDTO> listarTodos() {
        return inventarioRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioDTO> listarBajoStock() {
        return inventarioRepository.findBajoStock().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventarioDTO obtenerPorSku(String sku) {
        return toDTO(buscarPorSku(sku));
    }

    /** Crea el registro de inventario de un producto que ya existe. */
    @Transactional
    public InventarioDTO crear(InventarioDTO dto) {
        String sku = normalizarSku(dto.getSku());

        Producto producto = productoRepository.findById(sku)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con SKU " + sku));

        if (inventarioRepository.existsById(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El producto " + sku + " ya tiene inventario; usa PUT o los movimientos de entrada/salida");
        }

        Inventario inventario = new Inventario();
        inventario.setProducto(producto); // @MapsId copia el SKU del producto
        inventario.setCantidad(dto.getCantidad());
        inventario.setStockMinimo(dto.getStockMinimo());
        return toDTO(inventarioRepository.saveAndFlush(inventario));
    }

    /** Fija la cantidad y el stock mínimo (por ejemplo, después de un conteo físico). */
    @Transactional
    public InventarioDTO actualizar(String sku, InventarioDTO dto) {
        Inventario inventario = buscarPorSku(sku);

        if (!inventario.getSku().equals(normalizarSku(dto.getSku()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El SKU del body no coincide con el de la URL; el SKU no se puede modificar");
        }

        inventario.setCantidad(dto.getCantidad());
        inventario.setStockMinimo(dto.getStockMinimo());
        return toDTO(inventarioRepository.saveAndFlush(inventario));
    }

    /** Suma unidades al stock (compra, devolución, etc.). */
    @Transactional
    public InventarioDTO registrarEntrada(String sku, int cantidad) {
        Inventario inventario = buscarPorSku(sku);
        inventario.setCantidad(inventario.getCantidad() + cantidad);
        return toDTO(inventarioRepository.saveAndFlush(inventario));
    }

    /** Resta unidades del stock (venta, merma, etc.). No permite dejar el stock en negativo. */
    @Transactional
    public InventarioDTO registrarSalida(String sku, int cantidad) {
        Inventario inventario = buscarPorSku(sku);
        if (inventario.getCantidad() < cantidad) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente para " + inventario.getSku()
                            + ": hay " + inventario.getCantidad() + " y se pidieron " + cantidad);
        }
        inventario.setCantidad(inventario.getCantidad() - cantidad);
        return toDTO(inventarioRepository.saveAndFlush(inventario));
    }

    @Transactional
    public void eliminar(String sku) {
        inventarioRepository.delete(buscarPorSku(sku));
    }

    private Inventario buscarPorSku(String sku) {
        String skuNormalizado = normalizarSku(sku);
        return inventarioRepository.findById(skuNormalizado)
                .orElseThrow(() -> new EntityNotFoundException("No hay inventario para el SKU " + skuNormalizado));
    }

    /** Mismo criterio que ProductoService: SKU en mayúsculas y sin espacios. */
    private String normalizarSku(String sku) {
        return sku == null ? null : sku.trim().toUpperCase(Locale.ROOT);
    }

    private InventarioDTO toDTO(Inventario inventario) {
        return new InventarioDTO(
                inventario.getSku(),
                inventario.getProducto().getNombre(),
                inventario.getCantidad(),
                inventario.getStockMinimo(),
                inventario.isBajoStock(),
                inventario.getUltimaActualizacion()
        );
    }
}
