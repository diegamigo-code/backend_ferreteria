package backend_ferreteria.los_maestros.auditoria.service;

import backend_ferreteria.los_maestros.auditoria.AuditoriaApi;
import backend_ferreteria.los_maestros.auditoria.dto.AuditoriaDTO;
import backend_ferreteria.los_maestros.auditoria.model.AccionAuditoria;
import backend_ferreteria.los_maestros.auditoria.model.Auditoria;
import backend_ferreteria.los_maestros.auditoria.repository.AuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AuditoriaService implements AuditoriaApi {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String entidad, String entidadId, AccionAuditoria accion, String usuario) {
        auditoriaRepository.save(new Auditoria(null, entidad, entidadId, accion, usuario, LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaDTO> buscar(String entidad, String entidadId, AccionAuditoria accion,
                                     LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'desde' no puede ser posterior a 'hasta'");
        }
        return auditoriaRepository.buscar(vacioANull(entidad), vacioANull(entidadId), accion, desde, hasta, pageable)
                .map(this::toDTO);
    }

    private String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private AuditoriaDTO toDTO(Auditoria a) {
        return new AuditoriaDTO(a.getEntidad(), a.getEntidadId(), a.getAccion(), a.getUsuario(), a.getFecha());
    }
}
