package backend_ferreteria.los_maestros.credito.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreditoDTO {

    private Long usuarioId;
    private String nombre;
    private String rut;
    private String telefono;
    private Long cupo;
    private Long saldo;
    private Long disponible;
    private List<MovimientoCreditoDTO> movimientos;
}
