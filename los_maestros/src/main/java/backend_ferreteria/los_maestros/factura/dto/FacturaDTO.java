package backend_ferreteria.los_maestros.factura.dto;

import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FacturaDTO {
    private Long numero;
    private LocalDateTime fecha;
    private EstadoFactura estado;
    private Long usuarioId;
    private String clienteNombre;
    private String clienteEmail;
    private List<LineaFacturaDTO> lineas;
    private Long neto;
    private Long iva;
    private Long total;
    private LocalDateTime fechaAnulacion;
}
