package backend_ferreteria.los_maestros.credito;

public interface CreditoApi {

    void cargar(Long usuarioId, long monto, String documento, String detalle);

    void abonar(Long usuarioId, long monto, String documento, String detalle);
}
