package backend_ferreteria.los_maestros.factura.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmitirFacturaDTO {

    @NotNull
    private Long usuarioId;

    @NotEmpty
    @Size(max = 200)
    private List<@Valid @NotNull LineaSolicitudDTO> lineas;
}
