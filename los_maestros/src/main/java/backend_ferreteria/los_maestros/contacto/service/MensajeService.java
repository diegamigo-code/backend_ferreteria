package backend_ferreteria.los_maestros.contacto.service;

import backend_ferreteria.los_maestros.contacto.dto.MensajeDTO;
import backend_ferreteria.los_maestros.contacto.model.Mensaje;
import backend_ferreteria.los_maestros.contacto.repository.MensajeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;

    public MensajeService(MensajeRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @Transactional
    public MensajeDTO enviar(MensajeDTO dto) {
        Mensaje mensaje = new Mensaje();
        mensaje.setNombre(dto.getNombre().trim());
        mensaje.setEmail(dto.getEmail().trim().toLowerCase());
        mensaje.setTelefono(dto.getTelefono());
        mensaje.setMotivo(dto.getMotivo());
        mensaje.setMensaje(dto.getMensaje().trim());
        return toDTO(mensajeRepository.save(mensaje));
    }

    @Transactional(readOnly = true)
    public Page<MensajeDTO> listar(Pageable pageable) {
        return mensajeRepository.findAll(pageable).map(this::toDTO);
    }

    private MensajeDTO toDTO(Mensaje m) {
        return new MensajeDTO(m.getId(), m.getNombre(), m.getEmail(), m.getTelefono(), m.getMotivo(),
                m.getMensaje(), m.getFecha());
    }
}
