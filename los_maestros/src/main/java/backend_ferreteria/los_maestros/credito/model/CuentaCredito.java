package backend_ferreteria.los_maestros.credito.model;

import backend_ferreteria.los_maestros.compartido.listener.AuditoriaListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "cuentas_credito")
public class CuentaCredito {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false)
    private Long cupo;

    @Column(nullable = false)
    private Long saldo;

    @Version
    private Long version;

    public CuentaCredito(Long usuarioId, Long cupo) {
        this.usuarioId = usuarioId;
        this.cupo = cupo;
        this.saldo = 0L;
    }

    public long getDisponible() {
        return cupo - saldo;
    }
}
