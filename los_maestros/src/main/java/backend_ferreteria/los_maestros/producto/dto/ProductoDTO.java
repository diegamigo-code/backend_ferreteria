package backend_ferreteria.los_maestros.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductoDTO {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    private String marca;

    @NotBlank
    @Size(max = 100)
    private String categoria;

    @NotNull
    @PositiveOrZero
    private Double precio;

    @NotBlank
    @Size(max = 1000)
    private String descripcion;
}
