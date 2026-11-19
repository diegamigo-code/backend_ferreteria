package backend_ferreteria.los_maestros.inventario;

public interface InventarioApi {

    void descontarStock(String sku, int cantidad);

    void reponerStock(String sku, int cantidad);
}
