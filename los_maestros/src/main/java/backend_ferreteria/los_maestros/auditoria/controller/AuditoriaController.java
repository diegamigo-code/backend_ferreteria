package backend_ferreteria.los_maestros.auditoria.controller;

import backend_ferreteria.los_maestros.auditoria.dto.AuditoriaDTO;
import backend_ferreteria.los_maestros.auditoria.model.AccionAuditoria;
import backend_ferreteria.los_maestros.auditoria.service.AuditoriaService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

// Solo lectura: los registros los crea AuditoriaListener automáticamente.
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // Ej: /api/auditoria?entidad=Producto&entidadId=MART-001&accion=ACTUALIZAR&desde=2026-11-01T00:00:00&page=0&size=20
    @GetMapping
    public ResponseEntity<PagedModel<AuditoriaDTO>> buscar(
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String entidadId,
            @RequestParam(required = false) AccionAuditoria accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(
                auditoriaService.buscar(entidad, entidadId, accion, desde, hasta, pageable)));
    }
}
