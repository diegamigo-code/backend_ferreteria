package backend_ferreteria.los_maestros.compartido.listener;

import backend_ferreteria.los_maestros.auditoria.AuditoriaApi;
import backend_ferreteria.los_maestros.auditoria.model.AccionAuditoria;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class AuditoriaListener {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);

    private final ObjectProvider<AuditoriaApi> auditoriaApi;
    private final ObjectProvider<EntityManagerFactory> entityManagerFactory;

    public AuditoriaListener(ObjectProvider<AuditoriaApi> auditoriaApi,
                             ObjectProvider<EntityManagerFactory> entityManagerFactory) {
        this.auditoriaApi = auditoriaApi;
        this.entityManagerFactory = entityManagerFactory;
    }

    @PostPersist
    public void alCrear(Object entidad) {
        registrar(entidad, AccionAuditoria.CREAR);
    }

    @PostUpdate
    public void alActualizar(Object entidad) {
        registrar(entidad, AccionAuditoria.ACTUALIZAR);
    }

    @PostRemove
    public void alEliminar(Object entidad) {
        registrar(entidad, AccionAuditoria.ELIMINAR);
    }

    private void registrar(Object entidad, AccionAuditoria accion) {
        String nombre = Hibernate.getClass(entidad).getSimpleName();
        Object id = entityManagerFactory.getObject().getPersistenceUnitUtil().getIdentifier(entidad);
        String entidadId = String.valueOf(id);
        String usuario = usuarioActual();

        Runnable guardar = () -> {
            try {
                auditoriaApi.getObject().registrar(nombre, entidadId, accion, usuario);
            } catch (RuntimeException ex) {
                log.error("No se pudo registrar auditoría {} {} {}", accion, nombre, entidadId, ex);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    guardar.run();
                }
            });
        } else {
            guardar.run();
        }
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "anonimo";
        }
        return auth.getName();
    }
}
