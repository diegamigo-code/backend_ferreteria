package backend_ferreteria.los_maestros.inventario.model;

import backend_ferreteria.los_maestros.compartido.listener.AuditoriaListener;
import backend_ferreteria.los_maestros.producto.model.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "inventario")
public class Inventario {

    @Id
    @Column(name = "producto_sku", length = 50)
    private String sku;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_sku")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Producto producto;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull
    @PositiveOrZero
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @UpdateTimestamp
    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    @Version
    private Long version;

    public boolean isBajoStock() {
        return cantidad != null && stockMinimo != null && cantidad <= stockMinimo;
    }
}
