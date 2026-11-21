package backend_ferreteria.los_maestros.soporte;

import backend_ferreteria.los_maestros.inventario.model.Inventario;
import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.model.Producto;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import net.datafaker.Faker;

import java.util.Locale;

public final class DatosDePrueba {

    private static final Faker FAKER = new Faker(Locale.of("es"));

    private DatosDePrueba() {
    }

    public static Faker faker() {
        return FAKER;
    }

    public static String sku() {
        return FAKER.regexify("[A-Z]{3}-[0-9]{6}");
    }

    public static String nombre() {
        return recortar(FAKER.commerce().productName(), 100);
    }

    public static String marca() {
        return recortar(FAKER.company().name(), 100);
    }

    public static String categoria() {
        return recortar(FAKER.commerce().department(), 100);
    }

    public static long precio() {
        return FAKER.number().numberBetween(990L, 250_000L);
    }

    public static String descripcion() {
        return recortar(FAKER.lorem().sentence(15), 1000);
    }

    public static int cantidad() {
        return FAKER.number().numberBetween(0, 500);
    }

    public static ProductoDTO productoDTO() {
        return new ProductoDTO(sku(), nombre(), marca(), categoria(), precio(), descripcion());
    }

    public static Producto producto() {
        return new Producto(sku(), nombre(), marca(), categoria(), precio(), descripcion());
    }

    public static String email() {
        return FAKER.regexify("[a-z]{5,10}\\.[a-z]{4,8}[0-9]{3}") + "@"
                + FAKER.options().option("gmail.com", "outlook.com", "ferreteria.cl");
    }

    public static String password() {
        return FAKER.regexify("[a-z]{4}[A-Z]{4}[0-9]{4}");
    }

    public static UserDTO usuarioDTO() {
        return new UserDTO(
                null,
                recortar(FAKER.name().fullName(), 100),
                email(),
                FAKER.timeAndDate().birthday(18, 80),
                recortar(FAKER.address().fullAddress(), 255),
                password(),
                null,
                FAKER.regexify("[1-2][0-9]\\.[0-9]{3}\\.[0-9]{3}-[0-9K]"),
                FAKER.regexify("\\+569[0-9]{8}")
        );
    }

    public static Inventario inventario(Producto producto, int cantidad, int stockMinimo) {
        Inventario inventario = new Inventario();
        inventario.setSku(producto.getSku());
        inventario.setProducto(producto);
        inventario.setCantidad(cantidad);
        inventario.setStockMinimo(stockMinimo);
        return inventario;
    }

    private static String recortar(String valor, int max) {
        return valor.length() <= max ? valor : valor.substring(0, max);
    }
}
