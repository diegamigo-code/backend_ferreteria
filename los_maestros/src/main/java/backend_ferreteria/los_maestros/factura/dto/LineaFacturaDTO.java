package backend_ferreteria.los_maestros.factura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LineaFacturaDTO {
    private String sku;
    private String nombreProducto;
    private Long precioUnitario;
    private Integer cantidad;
    private Long subtotal;
}
