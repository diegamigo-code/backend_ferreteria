package backend_ferreteria.los_maestros.credito.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "movimientos_credito", indexes = @Index(name = "idx_movimientos_usuario", columnList = "usuario_id"))
public class MovimientoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 50)
    private String documento;

    @Column(nullable = false, length = 255)
    private String detalle;

    @Column(nullable = false)
    private Long cargo;

    @Column(nullable = false)
    private Long abono;

    @Column(nullable = false)
    private Long saldo;

    public MovimientoCredito(Long usuarioId, String documento, String detalle, long cargo, long abono, long saldo) {
        this.usuarioId = usuarioId;
        this.documento = documento;
        this.detalle = detalle;
        this.cargo = cargo;
        this.abono = abono;
        this.saldo = saldo;
    }
}
