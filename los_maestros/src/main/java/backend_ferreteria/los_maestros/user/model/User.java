package backend_ferreteria.los_maestros.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;
@AllArgsConstructor
@NoArgsConstructor

@Entity
public class User {
    @Id
    @NotNull
    @GeneratedValue
    private int id;

    @NotNull
    private String nombre;

    @NotNull
    private String email;

    @NotNull
    private Date fechaNacimiento;

    @NotNull
    private String direccion;
}
