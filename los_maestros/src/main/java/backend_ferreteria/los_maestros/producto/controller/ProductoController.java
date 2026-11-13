package backend_ferreteria.los_maestros.producto.controller;

import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listar() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ProductoDTO> obtener(@PathVariable String sku) {
        return ResponseEntity.ok(productoService.obtenerPorSku(sku));
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

    @PutMapping("/{sku}")
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable String sku,
                                                  @Valid @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(sku, dto));
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> eliminar(@PathVariable String sku) {
        productoService.eliminar(sku);
        return ResponseEntity.noContent().build();
    }
}
