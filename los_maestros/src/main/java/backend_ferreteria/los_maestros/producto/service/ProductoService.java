package backend_ferreteria.los_maestros.producto.service;

import backend_ferreteria.los_maestros.producto.ProductoApi;
import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class ProductoService implements ProductoApi {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listar(Pageable pageable) {
        return productoRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorSku(String sku) {
        return toDTO(buscarPorSku(sku));
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarPorCategoria(String categoria, Pageable pageable) {
        return productoRepository.findByCategoriaIgnoreCase(categoria, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarPorMarca(String marca, Pageable pageable) {
        return productoRepository.findByMarcaIgnoreCase(marca, pageable).map(this::toDTO);
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
