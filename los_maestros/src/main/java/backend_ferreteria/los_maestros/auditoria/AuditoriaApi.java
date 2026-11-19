package backend_ferreteria.los_maestros.auditoria;

import backend_ferreteria.los_maestros.auditoria.model.AccionAuditoria;

public interface AuditoriaApi {

    void registrar(String entidad, String entidadId, AccionAuditoria accion, String usuario);
}
