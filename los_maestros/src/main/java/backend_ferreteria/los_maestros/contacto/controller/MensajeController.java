package backend_ferreteria.los_maestros.contacto.controller;

import backend_ferreteria.los_maestros.contacto.dto.MensajeDTO;
import backend_ferreteria.los_maestros.contacto.service.MensajeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @PostMapping
    public ResponseEntity<MensajeDTO> enviar(@Valid @RequestBody MensajeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mensajeService.enviar(dto));
    }

    @GetMapping
    public ResponseEntity<PagedModel<MensajeDTO>> listar(
            @PageableDefault(size = 50, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(mensajeService.listar(pageable)));
    }
}
