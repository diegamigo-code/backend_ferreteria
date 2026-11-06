package backend_ferreteria.los_maestros.producto.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank
    @Column(nullable = false,length = 100)
    private String nombre;

    @Column(nullable = false,length = 100)
    @NotBlank
    private String marca;

    @Column(nullable = false,length = 100)
    @NotBlank
    private String categoria;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Double precio;

    @NotBlank
    @Column(nullable = false,length = 1000)
    private String descripcion;




}
