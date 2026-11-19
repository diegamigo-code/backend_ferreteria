package backend_ferreteria.los_maestros.factura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LineaSolicitudDTO {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "solo puede contener letras, números y guiones")
    private String sku;

    @NotNull
    @Positive
    private Integer cantidad;
}
