package backend_ferreteria.los_maestros.factura.service;

import backend_ferreteria.los_maestros.credito.CreditoApi;
import backend_ferreteria.los_maestros.factura.FacturaApi;
import backend_ferreteria.los_maestros.factura.dto.EmitirFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.FacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.LineaFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.LineaSolicitudDTO;
import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import backend_ferreteria.los_maestros.factura.model.EstadoPedido;
import backend_ferreteria.los_maestros.factura.model.FormaPago;
import backend_ferreteria.los_maestros.factura.model.Factura;
import backend_ferreteria.los_maestros.factura.model.LineaFactura;
import backend_ferreteria.los_maestros.factura.repository.RepositoryFactura;
import backend_ferreteria.los_maestros.inventario.InventarioApi;
import backend_ferreteria.los_maestros.producto.ProductoApi;
import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import backend_ferreteria.los_maestros.user.model.Rol;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class FacturaService implements FacturaApi {

    public static final long IVA_PORCENTAJE = 19;
    public static final int DIAS_CREDITO = 30;

    private final RepositoryFactura repositoryFactura;
    private final UsuarioApi usuarioApi;
    private final ProductoApi productoApi;
    private final InventarioApi inventarioApi;
    private final CreditoApi creditoApi;

    public FacturaService(RepositoryFactura repositoryFactura,
                          UsuarioApi usuarioApi,
                          ProductoApi productoApi,
                          InventarioApi inventarioApi,
                          CreditoApi creditoApi) {
        this.repositoryFactura = repositoryFactura;
        this.usuarioApi = usuarioApi;
        this.productoApi = productoApi;
        this.inventarioApi = inventarioApi;
        this.creditoApi = creditoApi;
    }

    @Override
    @Transactional
    public FacturaDTO emitir(EmitirFacturaDTO solicitud) {
        UserDTO cliente = usuarioApi.obtenerPorId(solicitud.getUsuarioId());
        FormaPago formaPago = solicitud.getFormaPago() != null ? solicitud.getFormaPago() : FormaPago.WEBPAY;
        if (formaPago == FormaPago.CREDITO && cliente.getRol() != Rol.CONTRATISTA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo los contratistas pueden comprar con crédito");
        }

        Map<String, Integer> cantidadesPorSku = new LinkedHashMap<>();
        for (LineaSolicitudDTO linea : solicitud.getLineas()) {
            cantidadesPorSku.merge(normalizarSku(linea.getSku()), linea.getCantidad(),
                    (a, b) -> Math.addExact(a.intValue(), b.intValue()));
        }

        Factura factura = new Factura();
        factura.setUsuarioId(cliente.getId());
        factura.setClienteNombre(cliente.getNombre());
        factura.setClienteEmail(cliente.getEmail());
        factura.setEstado(EstadoFactura.EMITIDA);
        factura.setFormaPago(formaPago);
        factura.setEntrega(solicitud.getEntrega() != null ? solicitud.getEntrega() : "Retiro en tienda");
        factura.setEstadoPedido(EstadoPedido.PENDIENTE);
        factura.setPagada(formaPago != FormaPago.CREDITO);
        if (formaPago == FormaPago.CREDITO) {
            factura.setVence(LocalDate.now().plusDays(DIAS_CREDITO));
        }

        long total = 0;
        for (Map.Entry<String, Integer> entrada : cantidadesPorSku.entrySet()) {
            String sku = entrada.getKey();
            int cantidad = entrada.getValue();

            ProductoDTO producto = productoApi.obtenerPorSku(sku);
            inventarioApi.descontarStock(sku, cantidad);

            long subtotal = Math.multiplyExact(producto.getPrecio(), (long) cantidad);
            total = Math.addExact(total, subtotal);
            factura.agregarLinea(new LineaFactura(null, null, sku, producto.getNombre(),
                    producto.getPrecio(), cantidad, subtotal));
        }

        long neto = calcularNeto(total);
        factura.setTotal(total);
        factura.setNeto(neto);
        factura.setIva(total - neto);

        Factura guardada = repositoryFactura.save(factura);
        if (formaPago == FormaPago.CREDITO) {
            creditoApi.cargar(cliente.getId(), total, "F-" + guardada.getNumero(), "Compra a crédito");
        }
        return toDTO(guardada);
    }

    @Override
    @Transactional
    public FacturaDTO anular(Long numero) {
        Factura factura = buscarPorNumero(numero);
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La factura " + numero + " ya está anulada");
        }

        for (LineaFactura linea : factura.getLineas()) {
            inventarioApi.reponerStock(linea.getSku(), linea.getCantidad());
        }

        if (factura.getFormaPago() == FormaPago.CREDITO && !Boolean.TRUE.equals(factura.getPagada())) {
            creditoApi.abonar(factura.getUsuarioId(), factura.getTotal(), "Anula F-" + numero, "Factura anulada");
        }
        factura.setEstado(EstadoFactura.ANULADA);
        factura.setFechaAnulacion(LocalDateTime.now());
        return toDTO(repositoryFactura.saveAndFlush(factura));
    }

    @Transactional
    public FacturaDTO pagar(Long numero) {
        Factura factura = buscarPorNumero(numero);
        if (factura.getFormaPago() != FormaPago.CREDITO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La factura " + numero + " no es a crédito");
        }
        if (factura.getEstado() == EstadoFactura.ANULADA || Boolean.TRUE.equals(factura.getPagada())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La factura " + numero + " no tiene saldo pendiente");
        }
        creditoApi.abonar(factura.getUsuarioId(), factura.getTotal(), "Pago F-" + numero, "Pago registrado");
        factura.setPagada(true);
        return toDTO(repositoryFactura.saveAndFlush(factura));
    }

    @Transactional
    public FacturaDTO cambiarEstadoPedido(Long numero, EstadoPedido estado) {
        Factura factura = buscarPorNumero(numero);
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La factura " + numero + " está anulada");
        }
        factura.setEstadoPedido(estado);
        return toDTO(repositoryFactura.saveAndFlush(factura));
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaDTO obtenerPorNumero(Long numero) {
        return toDTO(buscarPorNumero(numero));
    }

    @Transactional(readOnly = true)
    public Page<FacturaDTO> buscar(Long usuarioId, EstadoFactura estado,
                                   LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'desde' no puede ser posterior a 'hasta'");
        }
        return repositoryFactura.buscar(usuarioId, estado, desde, hasta, pageable).map(this::toDTO);
    }

    static long calcularNeto(long total) {
        return Math.round(total * 100.0 / (100 + IVA_PORCENTAJE));
    }

    private Factura buscarPorNumero(Long numero) {
        return repositoryFactura.findById(numero)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada con número " + numero));
    }

    private String normalizarSku(String sku) {
        return sku == null ? null : sku.trim().toUpperCase(Locale.ROOT);
    }

    private FacturaDTO toDTO(Factura factura) {
        return new FacturaDTO(
                factura.getNumero(),
                factura.getFecha(),
                factura.getEstado(),
                factura.getUsuarioId(),
                factura.getClienteNombre(),
                factura.getClienteEmail(),
                factura.getLineas().stream()
                        .map(l -> new LineaFacturaDTO(l.getSku(), l.getNombreProducto(),
                                l.getPrecioUnitario(), l.getCantidad(), l.getSubtotal()))
                        .toList(),
                factura.getNeto(),
                factura.getIva(),
                factura.getTotal(),
                factura.getFechaAnulacion(),
                factura.getFormaPago(),
                factura.getEntrega(),
                factura.getEstadoPedido(),
                factura.getVence(),
                factura.getPagada()
        );
    }
}
