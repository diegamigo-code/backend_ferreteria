package backend_ferreteria.los_maestros.inventario.controller;

import backend_ferreteria.los_maestros.inventario.dto.InventarioDTO;
import backend_ferreteria.los_maestros.inventario.dto.MovimientoStockDTO;
import backend_ferreteria.los_maestros.inventario.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public ResponseEntity<List<InventarioDTO>> listar() {
        return ResponseEntity.ok(inventarioService.listarTodos());
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<List<InventarioDTO>> listarBajoStock() {
        return ResponseEntity.ok(inventarioService.listarBajoStock());
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventarioDTO> obtener(@PathVariable String sku) {
        return ResponseEntity.ok(inventarioService.obtenerPorSku(sku));
    }

    @PostMapping
    public ResponseEntity<InventarioDTO> crear(@Valid @RequestBody InventarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crear(dto));
    }

    @PutMapping("/{sku}")
    public ResponseEntity<InventarioDTO> actualizar(@PathVariable String sku,
                                                    @Valid @RequestBody InventarioDTO dto) {
        return ResponseEntity.ok(inventarioService.actualizar(sku, dto));
    }

    @PostMapping("/{sku}/entrada")
    public ResponseEntity<InventarioDTO> registrarEntrada(@PathVariable String sku,
                                                          @Valid @RequestBody MovimientoStockDTO movimiento) {
        return ResponseEntity.ok(inventarioService.registrarEntrada(sku, movimiento.getCantidad()));
    }

    @PostMapping("/{sku}/salida")
    public ResponseEntity<InventarioDTO> registrarSalida(@PathVariable String sku,
                                                         @Valid @RequestBody MovimientoStockDTO movimiento) {
        return ResponseEntity.ok(inventarioService.registrarSalida(sku, movimiento.getCantidad()));
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> eliminar(@PathVariable String sku) {
        inventarioService.eliminar(sku);
        return ResponseEntity.noContent().build();
    }
}
