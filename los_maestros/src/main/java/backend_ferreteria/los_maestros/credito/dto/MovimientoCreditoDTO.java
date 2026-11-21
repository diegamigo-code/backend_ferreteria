package backend_ferreteria.los_maestros.credito.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovimientoCreditoDTO {

    private LocalDateTime fecha;
    private String documento;
    private String detalle;
    private Long cargo;
    private Long abono;
    private Long saldo;
}
