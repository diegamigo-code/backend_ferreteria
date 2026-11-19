package backend_ferreteria.los_maestros.inventario.service;

import backend_ferreteria.los_maestros.inventario.InventarioApi;
import backend_ferreteria.los_maestros.inventario.dto.InventarioDTO;
import backend_ferreteria.los_maestros.inventario.model.Inventario;
import backend_ferreteria.los_maestros.inventario.repository.InventarioRepository;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class InventarioService implements InventarioApi {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioService(InventarioRepository inventarioRepository,
                             ProductoRepository productoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public Page<InventarioDTO> listar(Pageable pageable) {
        return inventarioRepository.findAll(pageable).map(this::toDTO);
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
        inventario.setProducto(producto);
        inventario.setCantidad(dto.getCantidad());
        inventario.setStockMinimo(dto.getStockMinimo());
        return toDTO(inventarioRepository.saveAndFlush(inventario));
    }
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
    @Transactional
    public InventarioDTO registrarEntrada(String sku, int cantidad) {
        Inventario inventario = buscarPorSku(sku);
        inventario.setCantidad(inventario.getCantidad() + cantidad);
        return toDTO(inventarioRepository.saveAndFlush(inventario));
    }
    @Transactional
    public InventarioDTO registrarSalida(String sku, int cantidad) {
        Inventario inventario = buscarPorSku(sku);
        descontar(inventario, cantidad);
        return toDTO(inventarioRepository.saveAndFlush(inventario));
    }

    @Override
    @Transactional
    public void descontarStock(String sku, int cantidad) {
        String skuNormalizado = normalizarSku(sku);
        Inventario inventario = inventarioRepository.findById(skuNormalizado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "El producto " + skuNormalizado + " no tiene inventario registrado"));
        descontar(inventario, cantidad);
        inventarioRepository.saveAndFlush(inventario);
    }

    @Override
    @Transactional
    public void reponerStock(String sku, int cantidad) {
        inventarioRepository.findById(normalizarSku(sku)).ifPresent(inventario -> {
            inventario.setCantidad(inventario.getCantidad() + cantidad);
            inventarioRepository.saveAndFlush(inventario);
        });
    }

    @Transactional
    public void eliminar(String sku) {
        inventarioRepository.delete(buscarPorSku(sku));
    }

    private void descontar(Inventario inventario, int cantidad) {
        if (inventario.getCantidad() < cantidad) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente para " + inventario.getSku()
                            + ": hay " + inventario.getCantidad() + " y se pidieron " + cantidad);
        }
        inventario.setCantidad(inventario.getCantidad() - cantidad);
    }

    private Inventario buscarPorSku(String sku) {
        String skuNormalizado = normalizarSku(sku);
        return inventarioRepository.findById(skuNormalizado)
                .orElseThrow(() -> new EntityNotFoundException("No hay inventario para el SKU " + skuNormalizado));
    }
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
