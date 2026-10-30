package backend_ferreteria.los_maestros.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "usuarios")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false,length = 100)
    private String nombre;

    @NotBlank
    @Email
    @Column(nullable = false,unique = true,length = 255)
    private String email;

    @NotNull
    @Past
    @Column(nullable = false)
    private Date fechaNacimiento;

    @NotBlank
    @Column(nullable = false,length = 255)
    private String direccion;
}
