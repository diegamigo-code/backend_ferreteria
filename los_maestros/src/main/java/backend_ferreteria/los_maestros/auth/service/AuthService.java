package backend_ferreteria.los_maestros.auth.service;

import backend_ferreteria.los_maestros.auth.dto.LoginDTO;
import backend_ferreteria.los_maestros.auth.dto.TokenDTO;
import backend_ferreteria.los_maestros.compartido.SecurityConfig;
import backend_ferreteria.los_maestros.user.UsuarioApi;
import backend_ferreteria.los_maestros.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
public class AuthService {

    public static final String EMISOR = "los-maestros";

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UsuarioApi usuarioApi;
    private final long expiracionMinutos;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtEncoder jwtEncoder,
                       UsuarioApi usuarioApi,
                       @Value("${app.jwt.expiracion-minutos:60}") long expiracionMinutos) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.usuarioApi = usuarioApi;
        this.expiracionMinutos = expiracionMinutos;
    }

    public TokenDTO login(LoginDTO login) {
        String email = login.getEmail().trim().toLowerCase(Locale.ROOT);
        Authentication autenticado = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, login.getPassword()));

        return emitir(usuarioApi.obtenerPorEmail(autenticado.getName()));
    }

    public TokenDTO renovar(String email) {
        return emitir(usuarioApi.obtenerPorEmail(email));
    }

    private TokenDTO emitir(UserDTO usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiracionMinutos, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .issuedAt(ahora)
                .expiresAt(expira)
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim(SecurityConfig.CLAIM_ROL, usuario.getRol().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenDTO(token, "Bearer", expira, usuario.getId(), usuario.getNombre(),
                usuario.getEmail(), usuario.getRol());
    }
}
