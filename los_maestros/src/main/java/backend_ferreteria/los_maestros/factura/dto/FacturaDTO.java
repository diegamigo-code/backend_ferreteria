package backend_ferreteria.los_maestros.factura.dto;

import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import backend_ferreteria.los_maestros.factura.model.EstadoPedido;
import backend_ferreteria.los_maestros.factura.model.FormaPago;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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
    private FormaPago formaPago;
    private String entrega;
    private EstadoPedido estadoPedido;
    private LocalDate vence;
    private Boolean pagada;
}
