package backend_ferreteria.los_maestros.factura.service;

import backend_ferreteria.los_maestros.factura.FacturaApi;
import backend_ferreteria.los_maestros.factura.dto.EmitirFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.FacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.LineaFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.LineaSolicitudDTO;
import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import backend_ferreteria.los_maestros.factura.model.Factura;
import backend_ferreteria.los_maestros.factura.model.LineaFactura;
import backend_ferreteria.los_maestros.factura.repository.RepositoryFactura;
import backend_ferreteria.los_maestros.inventario.InventarioApi;
import backend_ferreteria.los_maestros.producto.ProductoApi;
import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class FacturaService implements FacturaApi {

    public static final long IVA_PORCENTAJE = 19;

    private final RepositoryFactura repositoryFactura;
    private final UsuarioApi usuarioApi;
    private final ProductoApi productoApi;
    private final InventarioApi inventarioApi;

    public FacturaService(RepositoryFactura repositoryFactura,
                          UsuarioApi usuarioApi,
                          ProductoApi productoApi,
                          InventarioApi inventarioApi) {
        this.repositoryFactura = repositoryFactura;
        this.usuarioApi = usuarioApi;
        this.productoApi = productoApi;
        this.inventarioApi = inventarioApi;
    }

    @Override
    @Transactional
    public FacturaDTO emitir(EmitirFacturaDTO solicitud) {
        UserDTO cliente = usuarioApi.obtenerPorId(solicitud.getUsuarioId());

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

        return toDTO(repositoryFactura.save(factura));
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

        factura.setEstado(EstadoFactura.ANULADA);
        factura.setFechaAnulacion(LocalDateTime.now());
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
                factura.getFechaAnulacion()
        );
    }
}
