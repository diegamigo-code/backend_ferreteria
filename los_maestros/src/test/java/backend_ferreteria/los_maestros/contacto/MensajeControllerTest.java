package backend_ferreteria.los_maestros.contacto;

import backend_ferreteria.los_maestros.compartido.SecurityConfig;
import backend_ferreteria.los_maestros.compartido.config.CorsConfig;
import backend_ferreteria.los_maestros.compartido.config.JwtConfig;
import backend_ferreteria.los_maestros.contacto.controller.MensajeController;
import backend_ferreteria.los_maestros.contacto.dto.MensajeDTO;
import backend_ferreteria.los_maestros.contacto.service.MensajeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MensajeController.class)
@Import({SecurityConfig.class, JwtConfig.class, CorsConfig.class})
class MensajeControllerTest {

    private static final String MENSAJE = "{\"nombre\":\"Ana Torres\",\"email\":\"ana@correo.cl\",\"telefono\":\"+56911223344\","
            + "\"motivo\":\"Consulta de stock\",\"mensaje\":\"¿Cuándo llega la escalera?\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MensajeService mensajeService;

    @Test
    void cualquieraPuedeEnviarUnMensaje() throws Exception {
        when(mensajeService.enviar(any(MensajeDTO.class))).thenReturn(new MensajeDTO(1L, "Ana Torres",
                "ana@correo.cl", "+56911223344", "Consulta de stock", "¿Cuándo llega la escalera?", LocalDateTime.now()));

        mockMvc.perform(post("/api/mensajes").contentType(MediaType.APPLICATION_JSON).content(MENSAJE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void mensajeSinCorreoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/mensajes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"motivo\":\"Otro\",\"mensaje\":\"Hola\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.email").exists());
        verifyNoInteractions(mensajeService);
    }

    @Test
    void soloElAdminLeeLosMensajes() throws Exception {
        mockMvc.perform(get("/api/mensajes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/mensajes"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(mensajeService);
    }
}
