package backend_ferreteria.los_maestros.credito.service;

import backend_ferreteria.los_maestros.credito.CreditoApi;
import backend_ferreteria.los_maestros.credito.dto.CreditoDTO;
import backend_ferreteria.los_maestros.credito.dto.MovimientoCreditoDTO;
import backend_ferreteria.los_maestros.credito.model.CuentaCredito;
import backend_ferreteria.los_maestros.credito.model.MovimientoCredito;
import backend_ferreteria.los_maestros.credito.repository.CuentaCreditoRepository;
import backend_ferreteria.los_maestros.credito.repository.MovimientoCreditoRepository;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import backend_ferreteria.los_maestros.user.model.Rol;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CreditoService implements CreditoApi {

    private final CuentaCreditoRepository cuentaRepository;
    private final MovimientoCreditoRepository movimientoRepository;
    private final UsuarioApi usuarioApi;

    public CreditoService(CuentaCreditoRepository cuentaRepository,
                          MovimientoCreditoRepository movimientoRepository,
                          UsuarioApi usuarioApi) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioApi = usuarioApi;
    }

    @Transactional(readOnly = true)
    public List<CreditoDTO> listar() {
        return cuentaRepository.findAll().stream()
                .map(cuenta -> toDTO(cuenta, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CreditoDTO obtener(Long usuarioId) {
        CuentaCredito cuenta = cuentaRepository.findById(usuarioId)
                .orElseGet(() -> new CuentaCredito(usuarioId, 0L));
        List<MovimientoCreditoDTO> movimientos = movimientoRepository.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
                .map(m -> new MovimientoCreditoDTO(m.getFecha(), m.getDocumento(), m.getDetalle(),
                        m.getCargo(), m.getAbono(), m.getSaldo()))
                .toList();
        return toDTO(cuenta, movimientos);
    }

    @Transactional
    public CreditoDTO asignarCupo(Long usuarioId, long cupo) {
        UserDTO usuario = usuarioApi.obtenerPorId(usuarioId);
        if (usuario.getRol() != Rol.CONTRATISTA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo los contratistas pueden tener crédito");
        }
        CuentaCredito cuenta = cuentaRepository.findById(usuarioId)
                .orElseGet(() -> new CuentaCredito(usuarioId, 0L));
        if (cupo < cuenta.getSaldo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El cupo no puede ser menor al saldo pendiente de $" + cuenta.getSaldo());
        }
        cuenta.setCupo(cupo);
        return toDTO(cuentaRepository.save(cuenta), List.of());
    }

    @Override
    @Transactional
    public void cargar(Long usuarioId, long monto, String documento, String detalle) {
        CuentaCredito cuenta = cuentaRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "El usuario no tiene crédito aprobado"));
        if (monto > cuenta.getDisponible()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cupo insuficiente: disponible $" + cuenta.getDisponible() + ", compra $" + monto);
        }
        cuenta.setSaldo(cuenta.getSaldo() + monto);
        cuentaRepository.save(cuenta);
        movimientoRepository.save(new MovimientoCredito(usuarioId, documento, detalle, monto, 0, cuenta.getSaldo()));
    }

    @Override
    @Transactional
    public void abonar(Long usuarioId, long monto, String documento, String detalle) {
        CuentaCredito cuenta = cuentaRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "El usuario no tiene crédito aprobado"));
        cuenta.setSaldo(Math.max(0, cuenta.getSaldo() - monto));
        cuentaRepository.save(cuenta);
        movimientoRepository.save(new MovimientoCredito(usuarioId, documento, detalle, 0, monto, cuenta.getSaldo()));
    }

    private CreditoDTO toDTO(CuentaCredito cuenta, List<MovimientoCreditoDTO> movimientos) {
        UserDTO usuario = usuarioApi.obtenerPorId(cuenta.getUsuarioId());
        return new CreditoDTO(cuenta.getUsuarioId(), usuario.getNombre(), usuario.getRut(), usuario.getTelefono(),
                cuenta.getCupo(), cuenta.getSaldo(), cuenta.getDisponible(), movimientos);
    }
}
