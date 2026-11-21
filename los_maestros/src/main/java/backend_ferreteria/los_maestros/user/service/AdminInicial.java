package backend_ferreteria.los_maestros.user.service;

import backend_ferreteria.los_maestros.user.model.Rol;
import backend_ferreteria.los_maestros.user.model.User;
import backend_ferreteria.los_maestros.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminInicial(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${app.admin.email}") String email,
                        @Value("${app.admin.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRol(Rol.ADMIN) || userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        userRepository.save(new User(null, "Administrador", email, LocalDate.of(2000, 1, 1),
                "Sin dirección", passwordEncoder.encode(password), Rol.ADMIN, null, null));
        log.info("Usuario administrador inicial creado: {}", email);
    }
}
