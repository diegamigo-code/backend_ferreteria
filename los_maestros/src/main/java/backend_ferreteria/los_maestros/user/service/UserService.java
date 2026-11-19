package backend_ferreteria.los_maestros.user.service;

import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import backend_ferreteria.los_maestros.user.model.User;
import backend_ferreteria.los_maestros.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class UserService implements UsuarioApi {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> listar(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO obtenerPorId(Long id) {
        return toDTO(buscarPorId(id));
    }

    @Transactional
    public UserDTO crear(UserDTO dto) {
        String email = normalizarEmail(dto.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw emailRepetido(email);
        }
        User user = new User(null, dto.getNombre(), email, dto.getFechaNacimiento(), dto.getDireccion());
        return toDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO actualizar(Long id, UserDTO dto) {
        User user = buscarPorId(id);
        String email = normalizarEmail(dto.getEmail());
        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
            throw emailRepetido(email);
        }
        user.setNombre(dto.getNombre());
        user.setEmail(email);
        user.setFechaNacimiento(dto.getFechaNacimiento());
        user.setDireccion(dto.getDireccion());
        return toDTO(userRepository.save(user));
    }

    @Transactional
    public void eliminar(Long id) {
        userRepository.delete(buscarPorId(id));
    }

    private User buscarPorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id " + id));
    }

    private ResponseStatusException emailRepetido(String email) {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con el email " + email);
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getNombre(),
                user.getEmail(),
                user.getFechaNacimiento(),
                user.getDireccion()
        );
    }
}
