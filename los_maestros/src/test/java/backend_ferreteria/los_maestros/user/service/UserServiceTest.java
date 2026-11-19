package backend_ferreteria.los_maestros.user.service;

import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import backend_ferreteria.los_maestros.user.model.User;
import backend_ferreteria.los_maestros.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void crearGuardaElEmailEnMinusculasEIgnoraElIdDelBody() {
        UserDTO dto = DatosDePrueba.usuarioDTO();
        String emailEsperado = dto.getEmail();
        dto.setId(999L);
        dto.setEmail(" " + emailEsperado.toUpperCase() + " ");
        when(userRepository.existsByEmailIgnoreCase(emailEsperado)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertThat(u.getId()).isNull();
            u.setId(1L);
            return u;
        });

        UserDTO resultado = userService.crear(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(emailEsperado);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getFechaNacimiento()).isEqualTo(dto.getFechaNacimiento());
    }

    @Test
    void crearConEmailRepetidoLanzaConflicto() {
        UserDTO dto = DatosDePrueba.usuarioDTO();
        when(userRepository.existsByEmailIgnoreCase(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.crear(dto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
    }

    @Test
    void actualizarConElEmailDeOtroUsuarioLanzaConflicto() {
        UserDTO original = DatosDePrueba.usuarioDTO();
        User existente = new User(1L, original.getNombre(), original.getEmail(),
                original.getFechaNacimiento(), original.getDireccion());
        UserDTO cambios = DatosDePrueba.usuarioDTO();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(userRepository.existsByEmailIgnoreCase(cambios.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.actualizar(1L, cambios))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void actualizarManteniendoSuPropioEmailFunciona() {
        UserDTO original = DatosDePrueba.usuarioDTO();
        User existente = new User(1L, original.getNombre(), original.getEmail(),
                original.getFechaNacimiento(), original.getDireccion());
        UserDTO cambios = DatosDePrueba.usuarioDTO();
        cambios.setEmail(original.getEmail().toUpperCase());
        when(userRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(userRepository.save(existente)).thenReturn(existente);

        UserDTO resultado = userService.actualizar(1L, cambios);

        assertThat(resultado.getNombre()).isEqualTo(cambios.getNombre());
        assertThat(resultado.getEmail()).isEqualTo(original.getEmail());
        verify(userRepository, never()).existsByEmailIgnoreCase(any());
    }

    @Test
    void obtenerUsuarioInexistenteLanzaNoEncontrado() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.obtenerPorId(42L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
