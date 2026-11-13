package backend_ferreteria.los_maestros.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InventarioDTO {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "solo puede contener letras, números y guiones")
    private String sku;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String nombreProducto;

    @NotNull
    @PositiveOrZero
    private Integer cantidad;

    @NotNull
    @PositiveOrZero
    private Integer stockMinimo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean bajoStock;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime ultimaActualizacion;
}
