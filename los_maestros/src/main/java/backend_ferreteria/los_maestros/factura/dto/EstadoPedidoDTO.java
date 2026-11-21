package backend_ferreteria.los_maestros.factura.dto;

import backend_ferreteria.los_maestros.factura.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EstadoPedidoDTO {

    @NotNull
    private EstadoPedido estado;
}
