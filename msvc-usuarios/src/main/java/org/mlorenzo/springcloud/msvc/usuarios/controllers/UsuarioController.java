package org.mlorenzo.springcloud.msvc.usuarios.controllers;

import feign.FeignException;
import org.mlorenzo.springcloud.msvc.usuarios.entities.Usuario;
import org.mlorenzo.springcloud.msvc.usuarios.exceptions.EmailExistsException;
import org.mlorenzo.springcloud.msvc.usuarios.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UsuarioController {
    private final UsuarioService usuarioService;

    @Autowired
    private ApplicationContext ctx;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Para simular fallos en Kubernetes
    @GetMapping("/crash")
    public void crash() {
        ((ConfigurableApplicationContext)ctx).close();
    }

    @GetMapping
    public List<Usuario> listar(){
        return usuarioService.listar();
    }

    @GetMapping("listar-por-ids")
    public List<Usuario> listarPorIds(@RequestParam List<Long> ids) {
        return usuarioService.listarPorIds(ids);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        Optional<Usuario> oUsuario = usuarioService.obtenerPorId(id);
        if(oUsuario.isPresent())
            return ResponseEntity.ok(oUsuario.get());
        return new ResponseEntity<>(Collections.singletonMap("mensaje",
                String.format("El usuario con id %d no existe", id)), HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Usuario crear(@Valid @RequestBody Usuario usuario) {
        return usuarioService.guardar(usuario);
    }

    @PutMapping("{id}")
    public ResponseEntity<?> editar(@Valid @RequestBody Usuario usuario, @PathVariable Long id) {
        Optional<Usuario> oUsuario = usuarioService.editar(usuario, id);
        if(oUsuario.isPresent())
            return new ResponseEntity<>(oUsuario.get(), HttpStatus.OK);
        else {
            return new ResponseEntity<>(Collections.singletonMap("mensaje",
                    String.format("El usuario con id %d no existe", id)), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> eliminar(@PathVariable("id") Long idUsuario) {
        return usuarioService.obtenerPorId(idUsuario)
                .map(usuario -> {
                    try {
                        usuarioService.eliminar(usuario);
                        return ResponseEntity.noContent().build();
                    }
                    catch(FeignException ex) {
                        ex.printStackTrace();
                        return ResponseEntity.internalServerError().body(Collections.singletonMap("mensaje",
                                String.format("Error de conexión con del microservicio usuarios. Hable con el administrador")));
                    }
                })
                .orElse(new ResponseEntity<>(Collections.singletonMap("mensaje",
                        String.format("El usuario con id %d no existe", idUsuario)), HttpStatus.NOT_FOUND));
    }

    // Este método puede ser privado o público
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private Map<String, String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        return ex.getFieldErrors().stream()
                // Versión simplificada de la expresión "toMap(fe -> fe.getField(), fe -> fe.getDefaultMessage())"
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
    }

    // Este método puede ser privado o público
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EmailExistsException.class)
    private Map<String, String> emailExistsExceptionHandler(EmailExistsException ex) {
        return Collections.singletonMap("email", ex.getMessage());
    }
}
