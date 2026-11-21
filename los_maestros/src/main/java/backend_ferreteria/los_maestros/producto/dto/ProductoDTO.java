package backend_ferreteria.los_maestros.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "solo puede contener letras, números y guiones")
    private String sku;

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
    private Long precio;

    @NotBlank
    @Size(max = 1000)
    private String descripcion;

    @PositiveOrZero
    private Long precioNormal;

    @Size(max = 255)
    private String imagen;
}
