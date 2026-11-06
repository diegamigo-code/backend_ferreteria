package backend_ferreteria.los_maestros.producto.controller;

import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listar() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProductoDTO>> listarPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(productoService.listarPorCategoria(categoria));
    }

    @GetMapping("/marca/{marca}")
    public ResponseEntity<List<ProductoDTO>> listarPorMarca(@PathVariable String marca) {
        return ResponseEntity.ok(productoService.listarPorMarca(marca));
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable Long id,
                                                  @Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
