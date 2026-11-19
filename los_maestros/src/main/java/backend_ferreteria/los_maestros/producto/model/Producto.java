package backend_ferreteria.los_maestros.producto.model;

import backend_ferreteria.los_maestros.compartido.listener.AuditoriaListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "productos")
public class Producto {

    @Id
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9-]+$")
    @Column(nullable = false, length = 50)
    private String sku;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String marca;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String categoria;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Long precio;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String descripcion;
}
