package backend_ferreteria.los_maestros.user.repository;

import backend_ferreteria.los_maestros.user.model.Rol;
import backend_ferreteria.los_maestros.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByRol(Rol rol);
}
