package backend_ferreteria.los_maestros.auth.dto;

import backend_ferreteria.los_maestros.user.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TokenDTO {
    private String token;
    private String tipo;
    private Instant expiraEn;
    private Long usuarioId;
    private String nombre;
    private String email;
    private Rol rol;
}
