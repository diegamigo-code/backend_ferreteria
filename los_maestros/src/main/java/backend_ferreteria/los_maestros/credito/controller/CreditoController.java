package backend_ferreteria.los_maestros.credito.controller;

import backend_ferreteria.los_maestros.credito.dto.CreditoDTO;
import backend_ferreteria.los_maestros.credito.dto.CupoDTO;
import backend_ferreteria.los_maestros.credito.service.CreditoService;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/credito")
public class CreditoController {

    private final CreditoService creditoService;
    private final UsuarioApi usuarioApi;

    public CreditoController(CreditoService creditoService, UsuarioApi usuarioApi) {
        this.creditoService = creditoService;
        this.usuarioApi = usuarioApi;
    }

    @GetMapping("/mio")
    public ResponseEntity<CreditoDTO> mio(Authentication authentication) {
        Long usuarioId = usuarioApi.obtenerPorEmail(authentication.getName()).getId();
        return ResponseEntity.ok(creditoService.obtener(usuarioId));
    }

    @GetMapping
    public ResponseEntity<List<CreditoDTO>> listar() {
        return ResponseEntity.ok(creditoService.listar());
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<CreditoDTO> obtener(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(creditoService.obtener(usuarioId));
    }

    @PutMapping("/{usuarioId}")
    public ResponseEntity<CreditoDTO> asignarCupo(@PathVariable Long usuarioId, @Valid @RequestBody CupoDTO dto) {
        return ResponseEntity.ok(creditoService.asignarCupo(usuarioId, dto.getCupo()));
    }
}
