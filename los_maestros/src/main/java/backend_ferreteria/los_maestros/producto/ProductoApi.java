package backend_ferreteria.los_maestros.producto;

import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;

public interface ProductoApi {

    ProductoDTO obtenerPorSku(String sku);
}
