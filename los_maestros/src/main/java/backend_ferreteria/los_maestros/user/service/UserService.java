package backend_ferreteria.los_maestros.user.service;

import backend_ferreteria.los_maestros.user.dto.UserDTO;
import backend_ferreteria.los_maestros.user.model.User;
import backend_ferreteria.los_maestros.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> listarTodos() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public UserDTO obtenerPorId(Long id) {
        return toDTO(buscarPorId(id));
    }

    public UserDTO crear(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email " + dto.getEmail());
        }
        User user = toEntity(dto);
        return toDTO(userRepository.save(user));
    }

    public UserDTO actualizar(Long id, UserDTO dto) {
        User user = buscarPorId(id);
        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email " + dto.getEmail());
        }
        user.setNombre(dto.getNombre());
        user.setEmail(dto.getEmail());
        user.setFechaNacimiento(dto.getFechaNacimiento());
        user.setDireccion(dto.getDireccion());
        return toDTO(userRepository.save(user));
    }

    public void eliminar(Long id) {
        User user = buscarPorId(id);
        userRepository.delete(user);
    }

    private User buscarPorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id " + id));
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

    private User toEntity(UserDTO dto) {
        return new User(
                null,
                dto.getNombre(),
                dto.getEmail(),
                dto.getFechaNacimiento(),
                dto.getDireccion()
        );
    }
}
