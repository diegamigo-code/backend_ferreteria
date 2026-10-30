package backend_ferreteria.los_maestros.user.repository;

import backend_ferreteria.los_maestros.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
