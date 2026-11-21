package backend_ferreteria.los_maestros.factura.model;

import backend_ferreteria.los_maestros.compartido.listener.AuditoriaListener;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "facturas", indexes = {
        @Index(name = "idx_facturas_usuario", columnList = "usuario_id"),
        @Index(name = "idx_facturas_fecha", columnList = "fecha")
})
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long numero;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "cliente_nombre", nullable = false, length = 100)
    private String clienteNombre;

    @Column(name = "cliente_email", nullable = false, length = 255)
    private String clienteEmail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFactura estado;

    @Column(nullable = false)
    private Long neto;

    @Column(nullable = false)
    private Long iva;

    @Column(nullable = false)
    private Long total;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pago", length = 20)
    private FormaPago formaPago;

    @Column(length = 255)
    private String entrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pedido", length = 30)
    private EstadoPedido estadoPedido;

    private LocalDate vence;

    private Boolean pagada;

    @Column(name = "costo_despacho")
    private Long costoDespacho;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @BatchSize(size = 50)
    private List<LineaFactura> lineas = new ArrayList<>();

    @Version
    private Long version;

    public void agregarLinea(LineaFactura linea) {
        linea.setFactura(this);
        lineas.add(linea);
    }
}
