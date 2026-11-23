package backend_ferreteria.los_maestros.auth.controller;

import backend_ferreteria.los_maestros.auth.dto.LoginDTO;
import backend_ferreteria.los_maestros.auth.dto.TokenDTO;
import backend_ferreteria.los_maestros.auth.service.AuthService;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioApi usuarioApi;

    public AuthController(AuthService authService, UsuarioApi usuarioApi) {
        this.authService = authService;
        this.usuarioApi = usuarioApi;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO login) {
        return ResponseEntity.ok(authService.login(login));
    }

    @PostMapping("/registro")
    public ResponseEntity<UserDTO> registro(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioApi.registrar(dto));
    }

    @PostMapping("/renovar")
    public ResponseEntity<TokenDTO> renovar(Authentication authentication) {
        return ResponseEntity.ok(authService.renovar(authentication.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(Authentication authentication) {
        return ResponseEntity.ok(usuarioApi.obtenerPorEmail(authentication.getName()));
    }
}
