package backend_ferreteria.los_maestros.producto.service;

import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.producto.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public ProductoDTO obtenerPorId(Long id) {
        return toDTO(buscarPorId(id));
    }

    public List<ProductoDTO> listarPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ProductoDTO> listarPorMarca(String marca) {
        return productoRepository.findByMarca(marca).stream()
                .map(this::toDTO)
                .toList();
    }

    public ProductoDTO crear(ProductoDTO dto) {
        Producto producto = toEntity(dto);
        return toDTO(productoRepository.save(producto));
    }

    public ProductoDTO actualizar(Long id, ProductoDTO dto) {
        Producto producto = buscarPorId(id);
        producto.setNombre(dto.getNombre());
        producto.setMarca(dto.getMarca());
        producto.setCategoria(dto.getCategoria());
        producto.setPrecio(dto.getPrecio());
        producto.setDescripcion(dto.getDescripcion());
        return toDTO(productoRepository.save(producto));
    }

    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        productoRepository.delete(producto);
    }

    private Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id " + id));
    }

    private ProductoDTO toDTO(Producto producto) {
        return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getMarca(),
                producto.getCategoria(),
                producto.getPrecio(),
                producto.getDescripcion()
        );
    }

    private Producto toEntity(ProductoDTO dto) {
        return new Producto(
                null,
                dto.getNombre(),
                dto.getMarca(),
                dto.getCategoria(),
                dto.getPrecio(),
                dto.getDescripcion()
        );
    }
}
