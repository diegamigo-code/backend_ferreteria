package backend_ferreteria.los_maestros.factura.controller;

import backend_ferreteria.los_maestros.factura.dto.EmitirFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.EstadoPedidoDTO;
import backend_ferreteria.los_maestros.factura.dto.FacturaDTO;
import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import backend_ferreteria.los_maestros.factura.service.FacturaService;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/facturas")
public class ControllerFactura {

    private final FacturaService facturaService;
    private final UsuarioApi usuarioApi;

    public ControllerFactura(FacturaService facturaService, UsuarioApi usuarioApi) {
        this.facturaService = facturaService;
        this.usuarioApi = usuarioApi;
    }

    @GetMapping("/mias")
    public ResponseEntity<PagedModel<FacturaDTO>> mias(
            Authentication authentication,
            @PageableDefault(size = 50, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        Long usuarioId = usuarioApi.obtenerPorEmail(authentication.getName()).getId();
        return ResponseEntity.ok(new PagedModel<>(facturaService.buscar(usuarioId, null, null, null, pageable)));
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
    public ResponseEntity<FacturaDTO> emitir(@Valid @RequestBody EmitirFacturaDTO solicitud,
                                             Authentication authentication) {
        if (!esPersonal(authentication)) {
            solicitud.setUsuarioId(usuarioApi.obtenerPorEmail(authentication.getName()).getId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.emitir(solicitud));
    }

    @PostMapping("/{numero}/anular")
    public ResponseEntity<FacturaDTO> anular(@PathVariable Long numero) {
        return ResponseEntity.ok(facturaService.anular(numero));
    }

    @PostMapping("/{numero}/pagar")
    public ResponseEntity<FacturaDTO> pagar(@PathVariable Long numero) {
        return ResponseEntity.ok(facturaService.pagar(numero));
    }

    @PatchMapping("/{numero}/estado")
    public ResponseEntity<FacturaDTO> cambiarEstado(@PathVariable Long numero,
                                                    @Valid @RequestBody EstadoPedidoDTO dto) {
        return ResponseEntity.ok(facturaService.cambiarEstadoPedido(numero, dto.getEstado()));
    }

    private boolean esPersonal(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.equals("ROLE_ADMIN") || rol.equals("ROLE_VENDEDOR"));
    }
}
