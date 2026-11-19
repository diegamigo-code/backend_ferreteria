package backend_ferreteria.los_maestros.factura.controller;

import backend_ferreteria.los_maestros.factura.dto.EmitirFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.FacturaDTO;
import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import backend_ferreteria.los_maestros.factura.service.FacturaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/facturas")
public class ControllerFactura {

    private final FacturaService facturaService;

    public ControllerFactura(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public ResponseEntity<PagedModel<FacturaDTO>> buscar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) EstadoFactura estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(
                facturaService.buscar(usuarioId, estado, desde, hasta, pageable)));
    }

    @GetMapping("/{numero}")
    public ResponseEntity<FacturaDTO> obtener(@PathVariable Long numero) {
        return ResponseEntity.ok(facturaService.obtenerPorNumero(numero));
    }

    @PostMapping
    public ResponseEntity<FacturaDTO> emitir(@Valid @RequestBody EmitirFacturaDTO solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.emitir(solicitud));
    }

    @PostMapping("/{numero}/anular")
    public ResponseEntity<FacturaDTO> anular(@PathVariable Long numero) {
        return ResponseEntity.ok(facturaService.anular(numero));
    }
}
