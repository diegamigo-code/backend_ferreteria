package backend_ferreteria.los_maestros.producto.service;

import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
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
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorSku(String sku) {
        return toDTO(buscarPorSku(sku));
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorMarca(String marca) {
        return productoRepository.findByMarca(marca).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public ProductoDTO crear(ProductoDTO dto) {
        String sku = normalizarSku(dto.getSku());
        if (productoRepository.existsById(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con SKU " + sku);
        }
        Producto producto = toEntity(dto);
        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDTO actualizar(String sku, ProductoDTO dto) {
        Producto producto = buscarPorSku(sku);

        // El SKU es la clave primaria: no se puede cambiar. Para "renombrarlo" hay que crear otro producto.
        if (!producto.getSku().equals(normalizarSku(dto.getSku()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El SKU del body no coincide con el de la URL; el SKU no se puede modificar");
        }

        producto.setNombre(dto.getNombre());
        producto.setMarca(dto.getMarca());
        producto.setCategoria(dto.getCategoria());
        producto.setPrecio(dto.getPrecio());
        producto.setDescripcion(dto.getDescripcion());
        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(String sku) {
        productoRepository.delete(buscarPorSku(sku));
    }

    private Producto buscarPorSku(String sku) {
        String skuNormalizado = normalizarSku(sku);
        return productoRepository.findById(skuNormalizado)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con SKU " + skuNormalizado));
    }

    /** Guarda los SKU siempre en mayúsculas y sin espacios, para que "mart-001" y "MART-001" sean el mismo. */
    private String normalizarSku(String sku) {
        return sku == null ? null : sku.trim().toUpperCase(Locale.ROOT);
    }

    private ProductoDTO toDTO(Producto producto) {
        return new ProductoDTO(
                producto.getSku(),
                producto.getNombre(),
                producto.getMarca(),
                producto.getCategoria(),
                producto.getPrecio(),
                producto.getDescripcion()
        );
    }

    private Producto toEntity(ProductoDTO dto) {
        return new Producto(
                normalizarSku(dto.getSku()),
                dto.getNombre(),
                dto.getMarca(),
                dto.getCategoria(),
                dto.getPrecio(),
                dto.getDescripcion()
        );
    }
}
