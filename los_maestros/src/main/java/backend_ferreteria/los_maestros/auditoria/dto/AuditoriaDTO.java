package backend_ferreteria.los_maestros.auditoria.dto;

import backend_ferreteria.los_maestros.auditoria.model.AccionAuditoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuditoriaDTO {
    private String entidad;
    private String entidadId;
    private AccionAuditoria accion;
    private String usuario;
    private LocalDateTime fecha;
}
