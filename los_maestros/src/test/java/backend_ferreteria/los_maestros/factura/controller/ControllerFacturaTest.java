package backend_ferreteria.los_maestros.factura.controller;

import backend_ferreteria.los_maestros.compartido.SecurityConfig;
import backend_ferreteria.los_maestros.compartido.config.CorsConfig;
import backend_ferreteria.los_maestros.compartido.config.JwtConfig;
import backend_ferreteria.los_maestros.factura.dto.EmitirFacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.FacturaDTO;
import backend_ferreteria.los_maestros.factura.dto.LineaFacturaDTO;
import backend_ferreteria.los_maestros.factura.model.EstadoFactura;
import backend_ferreteria.los_maestros.factura.service.FacturaService;
import backend_ferreteria.los_maestros.soporte.DatosDePrueba;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ControllerFactura.class)
@Import({SecurityConfig.class, JwtConfig.class, CorsConfig.class})
class ControllerFacturaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacturaService facturaService;

    @MockitoBean
    private UsuarioApi usuarioApi;

    @Test
    void emitirDevuelve201ConLaFactura() throws Exception {
        String sku = DatosDePrueba.sku();
        long precio = DatosDePrueba.precio();
        FacturaDTO factura = new FacturaDTO(15L, LocalDateTime.now(), EstadoFactura.EMITIDA, 3L,
                DatosDePrueba.faker().name().fullName(), DatosDePrueba.email(),
                List.of(new LineaFacturaDTO(sku, DatosDePrueba.nombre(), precio, 2, precio * 2)),
                0L, 0L, precio * 2, null, null, null, null, null, null, null);
        when(facturaService.emitir(any(EmitirFacturaDTO.class))).thenReturn(factura);

        mockMvc.perform(post("/api/facturas").with(vendedor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"lineas\":[{\"sku\":\"" + sku + "\",\"cantidad\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(15))
                .andExpect(jsonPath("$.estado").value("EMITIDA"))
                .andExpect(jsonPath("$.lineas[0].sku").value(sku))
                .andExpect(jsonPath("$.total").value(precio * 2));
    }

    @Test
    void emitirSinLineasDevuelve400() throws Exception {
        mockMvc.perform(post("/api/facturas").with(vendedor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"lineas\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.lineas").exists());
        verifyNoInteractions(facturaService);
    }

    @Test
    void emitirConCantidadCeroDevuelve400() throws Exception {
        mockMvc.perform(post("/api/facturas").with(vendedor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"lineas\":[{\"sku\":\"" + DatosDePrueba.sku() + "\",\"cantidad\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores['lineas[0].cantidad']").exists());
        verifyNoInteractions(facturaService);
    }

    @Test
    void emitirSinStockDevuelve409() throws Exception {
        when(facturaService.emitir(any(EmitirFacturaDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente"));

        mockMvc.perform(post("/api/facturas").with(vendedor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"lineas\":[{\"sku\":\"" + DatosDePrueba.sku() + "\",\"cantidad\":9}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente"));
    }

    @Test
    void anularFacturaYaAnuladaDevuelve409() throws Exception {
        when(facturaService.anular(15L))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "La factura 15 ya está anulada"));

        mockMvc.perform(post("/api/facturas/15/anular").with(vendedor()))
                .andExpect(status().isConflict());
    }

    @Test
    void emitirSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/facturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"lineas\":[{\"sku\":\"" + DatosDePrueba.sku() + "\",\"cantidad\":1}]}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(facturaService);
    }

    @Test
    void emitirComoClienteUsaSuPropioUsuario() throws Exception {
        UserDTO cliente = DatosDePrueba.usuarioDTO();
        cliente.setId(8L);
        when(usuarioApi.obtenerPorEmail("cliente@correo.cl")).thenReturn(cliente);
        when(facturaService.emitir(any(EmitirFacturaDTO.class))).thenReturn(new FacturaDTO());

        mockMvc.perform(post("/api/facturas")
                        .with(jwt().jwt(j -> j.subject("cliente@correo.cl"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"lineas\":[{\"sku\":\"" + DatosDePrueba.sku() + "\",\"cantidad\":1}]}"))
                .andExpect(status().isCreated());
        verify(facturaService).emitir(argThat(s -> s.getUsuarioId() == 8L));
    }

    @Test
    void anularComoClienteDevuelve403() throws Exception {
        mockMvc.perform(post("/api/facturas/15/anular")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(facturaService);
    }

    private static RequestPostProcessor vendedor() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_VENDEDOR"));
    }
}
