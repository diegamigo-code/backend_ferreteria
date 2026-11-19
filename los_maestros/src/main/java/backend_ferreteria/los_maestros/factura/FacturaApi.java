package backend_ferreteria.los_maestros.factura;

import backend_ferreteria.los_maestros.factura.dto.EmitirFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.FacturaDTO;

public interface FacturaApi {

    FacturaDTO emitir(EmitirFacturaDTO solicitud);

    FacturaDTO anular(Long numero);

    FacturaDTO obtenerPorNumero(Long numero);
}
