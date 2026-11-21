package backend_ferreteria.los_maestros.user.service;

import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import backend_ferreteria.los_maestros.user.model.Rol;
import backend_ferreteria.los_maestros.user.model.User;
import backend_ferreteria.los_maestros.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class UserService implements UsuarioApi {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    @Override
    @Transactional(readOnly = true)
    public UserDTO obtenerPorEmail(String email) {
        String normalizado = normalizarEmail(email);
        return userRepository.findByEmailIgnoreCase(normalizado)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email " + normalizado));
    }

    @Override
    @Transactional
    public UserDTO registrar(UserDTO dto) {
        return guardarNuevo(dto, dto.getRol() == Rol.CONTRATISTA ? Rol.CONTRATISTA : Rol.CLIENTE);
    }

    @Transactional
    public UserDTO crear(UserDTO dto) {
        return guardarNuevo(dto, dto.getRol() != null ? dto.getRol() : Rol.CLIENTE);
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
        user.setRut(dto.getRut());
        user.setTelefono(dto.getTelefono());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRol() != null) {
            user.setRol(dto.getRol());
        }
        return toDTO(userRepository.save(user));
    }

    @Transactional
    public void eliminar(Long id) {
        userRepository.delete(buscarPorId(id));
    }

    private UserDTO guardarNuevo(UserDTO dto, Rol rol) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        String email = normalizarEmail(dto.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw emailRepetido(email);
        }
        User user = new User(null, dto.getNombre(), email, dto.getFechaNacimiento(), dto.getDireccion(),
                passwordEncoder.encode(dto.getPassword()), rol, dto.getRut(), dto.getTelefono());
        return toDTO(userRepository.save(user));
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
                user.getDireccion(),
                null,
                user.getRol(),
                user.getRut(),
                user.getTelefono()
        );
    }
}
