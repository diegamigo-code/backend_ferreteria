package backend_ferreteria.los_maestros.credito.service;

import backend_ferreteria.los_maestros.credito.dto.CreditoDTO;
import backend_ferreteria.los_maestros.credito.model.CuentaCredito;
import backend_ferreteria.los_maestros.credito.model.MovimientoCredito;
import backend_ferreteria.los_maestros.credito.repository.CuentaCreditoRepository;
import backend_ferreteria.los_maestros.credito.repository.MovimientoCreditoRepository;
import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import backend_ferreteria.los_maestros.user.model.Rol;
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
class CreditoServiceTest {

    @Mock
    private CuentaCreditoRepository cuentaRepository;

    @Mock
    private MovimientoCreditoRepository movimientoRepository;

    @Mock
    private UsuarioApi usuarioApi;

    @InjectMocks
    private CreditoService creditoService;

    @Test
    void cargarSumaAlSaldoYRegistraElMovimiento() {
        CuentaCredito cuenta = new CuentaCredito(7L, 100_000L);
        when(cuentaRepository.findById(7L)).thenReturn(Optional.of(cuenta));

        creditoService.cargar(7L, 40_000L, "F-1", "Compra de materiales");

        assertThat(cuenta.getSaldo()).isEqualTo(40_000L);
        assertThat(cuenta.getDisponible()).isEqualTo(60_000L);
        ArgumentCaptor<MovimientoCredito> movimiento = ArgumentCaptor.forClass(MovimientoCredito.class);
        verify(movimientoRepository).save(movimiento.capture());
        assertThat(movimiento.getValue().getCargo()).isEqualTo(40_000L);
        assertThat(movimiento.getValue().getSaldo()).isEqualTo(40_000L);
    }

    @Test
    void cargarSobreElCupoDevuelveConflicto() {
        CuentaCredito cuenta = new CuentaCredito(7L, 50_000L);
        cuenta.setSaldo(30_000L);
        when(cuentaRepository.findById(7L)).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> creditoService.cargar(7L, 25_000L, "F-2", "Compra"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(cuenta.getSaldo()).isEqualTo(30_000L);
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void cargarSinCuentaDevuelveConflicto() {
        when(cuentaRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> creditoService.cargar(7L, 1_000L, "F-3", "Compra"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void abonarDescuentaDelSaldo() {
        CuentaCredito cuenta = new CuentaCredito(7L, 100_000L);
        cuenta.setSaldo(70_000L);
        when(cuentaRepository.findById(7L)).thenReturn(Optional.of(cuenta));

        creditoService.abonar(7L, 30_000L, "Pago F-1", "Pago registrado");

        assertThat(cuenta.getSaldo()).isEqualTo(40_000L);
    }

    @Test
    void asignarCupoAUnClienteNormalDevuelve400() {
        UserDTO cliente = DatosDePrueba.usuarioDTO();
        cliente.setRol(Rol.CLIENTE);
        when(usuarioApi.obtenerPorId(7L)).thenReturn(cliente);

        assertThatThrownBy(() -> creditoService.asignarCupo(7L, 100_000L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void asignarCupoMenorAlSaldoDevuelveConflicto() {
        UserDTO contratista = DatosDePrueba.usuarioDTO();
        contratista.setRol(Rol.CONTRATISTA);
        CuentaCredito cuenta = new CuentaCredito(7L, 100_000L);
        cuenta.setSaldo(80_000L);
        when(usuarioApi.obtenerPorId(7L)).thenReturn(contratista);
        when(cuentaRepository.findById(7L)).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> creditoService.asignarCupo(7L, 50_000L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void asignarCupoCreaLaCuentaDelContratista() {
        UserDTO contratista = DatosDePrueba.usuarioDTO();
        contratista.setRol(Rol.CONTRATISTA);
        when(usuarioApi.obtenerPorId(7L)).thenReturn(contratista);
        when(cuentaRepository.findById(7L)).thenReturn(Optional.empty());
        when(cuentaRepository.save(any(CuentaCredito.class))).thenAnswer(inv -> inv.getArgument(0));

        CreditoDTO credito = creditoService.asignarCupo(7L, 500_000L);

        assertThat(credito.getCupo()).isEqualTo(500_000L);
        assertThat(credito.getSaldo()).isZero();
        assertThat(credito.getDisponible()).isEqualTo(500_000L);
        assertThat(credito.getRut()).isEqualTo(contratista.getRut());
    }
}
