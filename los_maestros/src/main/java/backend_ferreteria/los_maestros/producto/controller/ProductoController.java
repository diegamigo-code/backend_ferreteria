package backend_ferreteria.los_maestros.producto.controller;

import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<PagedModel<ProductoDTO>> listar(
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(productoService.listar(pageable)));
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ProductoDTO> obtener(@PathVariable String sku) {
        return ResponseEntity.ok(productoService.obtenerPorSku(sku));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<PagedModel<ProductoDTO>> listarPorCategoria(
            @PathVariable String categoria,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(productoService.listarPorCategoria(categoria, pageable)));
    }

    @GetMapping("/marca/{marca}")
    public ResponseEntity<PagedModel<ProductoDTO>> listarPorMarca(
            @PathVariable String marca,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(productoService.listarPorMarca(marca, pageable)));
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
