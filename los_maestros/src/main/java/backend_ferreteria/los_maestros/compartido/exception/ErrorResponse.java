package backend_ferreteria.los_maestros.compartido.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cuerpo JSON que devuelve la API cuando algo sale mal.
 *
 * @param errores detalle por campo (solo en errores de validación)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> errores
) {
}
