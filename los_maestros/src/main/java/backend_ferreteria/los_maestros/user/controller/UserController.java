package backend_ferreteria.los_maestros.user.controller;

import backend_ferreteria.los_maestros.user.dto.UserDTO;
import backend_ferreteria.los_maestros.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/usuarios")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> listar() {
        return ResponseEntity.ok(userService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(userService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<UserDTO> crear(@Valid @RequestBody UserDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> actualizar(@PathVariable Long id,
                                              @Valid @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        userService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
