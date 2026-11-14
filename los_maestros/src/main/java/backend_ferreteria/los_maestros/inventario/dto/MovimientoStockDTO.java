package backend_ferreteria.los_maestros.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovimientoStockDTO {

    @NotNull
    @Positive
    private Integer cantidad;
}
