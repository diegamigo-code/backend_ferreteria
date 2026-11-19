package backend_ferreteria.los_maestros.user;

import backend_ferreteria.los_maestros.user.dto.UserDTO;

public interface UsuarioApi {

    UserDTO obtenerPorId(Long id);

    UserDTO obtenerPorEmail(String email);

    UserDTO registrar(UserDTO dto);
}
