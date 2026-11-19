package backend_ferreteria.los_maestros.producto.controller;

import backend_ferreteria.los_maestros.compartido.SecurityConfig;
import backend_ferreteria.los_maestros.compartido.config.CorsConfig;
import backend_ferreteria.los_maestros.producto.dto.ProductoDTO;
import backend_ferreteria.los_maestros.producto.service.ProductoService;
import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void listarDevuelveLosProductosSinId() throws Exception {
        List<ProductoDTO> productos = Stream.generate(DatosDePrueba::productoDTO).limit(3).toList();
        when(productoService.listarTodos()).thenReturn(productos);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].sku").value(productos.get(0).getSku()))
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void crearProductoValidoDevuelve201() throws Exception {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        when(productoService.crear(any(ProductoDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(dto.getSku()))
                .andExpect(jsonPath("$.nombre").value(dto.getNombre()));
    }

    @Test
    void crearProductoInvalidoDevuelve400ConErroresPorCampo() throws Exception {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        dto.setNombre("");
        dto.setPrecio(-1.0);
        dto.setSku("SKU CON ESPACIOS!");

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.precio").exists())
                .andExpect(jsonPath("$.errores.sku").exists());
    }

    @Test
    void crearConJsonMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\": "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConSkuRepetidoDevuelve409() throws Exception {
        ProductoDTO dto = DatosDePrueba.productoDTO();
        when(productoService.crear(any(ProductoDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con SKU " + dto.getSku()));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un producto con SKU " + dto.getSku()));
    }

    @Test
    void obtenerProductoInexistenteDevuelve404() throws Exception {
        String sku = DatosDePrueba.sku();
        when(productoService.obtenerPorSku(sku))
                .thenThrow(new EntityNotFoundException("Producto no encontrado con SKU " + sku));

        mockMvc.perform(get("/api/productos/{sku}", sku))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.ruta").value("/api/productos/" + sku));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/productos/{sku}", DatosDePrueba.sku()))
                .andExpect(status().isNoContent());
    }

    private static String json(ProductoDTO dto) {
        return """
                {"sku":%s,"nombre":%s,"marca":%s,"categoria":%s,"precio":%s,"descripcion":%s}
                """.formatted(
                texto(dto.getSku()), texto(dto.getNombre()), texto(dto.getMarca()),
                texto(dto.getCategoria()), dto.getPrecio(), texto(dto.getDescripcion()));
    }

    private static String texto(String valor) {
        return "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
