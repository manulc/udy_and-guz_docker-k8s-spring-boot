package org.mlorenzo.springcloud.msvc.cursos.controllers;

import feign.FeignException;
import org.mlorenzo.springcloud.msvc.cursos.entities.Curso;
import org.mlorenzo.springcloud.msvc.cursos.models.Usuario;
import org.mlorenzo.springcloud.msvc.cursos.services.CursoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
public class CursoController {
    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public List<Curso> listar() {
        return cursoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id,
                                     @RequestHeader(value = "Authorization") String authorizationHeader) {
        try {
            return cursoService.obtenerPorIdConUsuarios(id, authorizationHeader)
                    // Versión simplificada de la expresión "curso -> ResponseEntity.ok(curso)"
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        catch(FeignException ex) {
            return handleFeignExceptionResponse(ex);
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Curso curso, BindingResult result) {
        if(result.hasErrors())
            return createNotValidResponse(result);
        return new ResponseEntity<>(cursoService.guardar(curso), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @Valid @RequestBody Curso curso, BindingResult result) {
        if(result.hasErrors())
            return createNotValidResponse(result);
        return cursoService.editar(curso, id)
                // Versión simplificada de la expresión "curso -> ResponseEntity.ok(curso)"
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return cursoService.obtenerPorId(id)
                .map(curso -> {
                    cursoService.eliminar(curso);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Nota: Indicamos que el contenido de la respuesta sea de tipo "application/json" porque el método privado
    // "handleFeignExceptionResponse" puede devolver un objeto "ResponseEntity" con una representación de un Json
    // como String. Entonces, para esos casos, queremos renderizar ese tring como un Json.
    @PatchMapping(value = "/{id}/asignar-usuario/{usuarioId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> asignarUsuario(@PathVariable(name = "id") Long cursoId, @PathVariable Long usuarioId,
                                            @RequestHeader(name = "Authorization") String authorizationHeader) {
        try {
            return cursoService.asignarUsuario(cursoId, usuarioId,authorizationHeader)
                    .map(curso -> ResponseEntity.ok(Collections.singletonMap("mensaje",
                            String.format("El usuario con id %d ha sido asignado al curso con id %d correctamente",
                                    usuarioId, cursoId))))
                    .orElse(new ResponseEntity<>(Collections.singletonMap("mensaje",
                            String.format("El curso con id %d no existe", cursoId)), HttpStatus.NOT_FOUND));
        }
        catch(FeignException ex) {
            return handleFeignExceptionResponse(ex);
        }
    }

    // Nota: Indicamos que el contenido de la respuesta sea de tipo "application/json" porque el método privado
    // "handleFeignExceptionResponse" puede devolver un objeto "ResponseEntity" con una representación de un Json
    // como String. Entonces, para esos casos, queremos renderizar ese tring como un Json.
    @PostMapping(value = "/{id}/crear-usuario", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario, @PathVariable(value = "id") Long cursoId,
                                          @RequestHeader(value = "Authorization") String authorizationHeader) {
        try {
            Optional<Usuario> oUsuario = cursoService.crearUsuario(usuario, cursoId, authorizationHeader);
            if(oUsuario.isPresent()) {
                return ResponseEntity.created(null).body(oUsuario.get());
            }
            return new ResponseEntity<>(Collections.singletonMap("mensaje",
                    String.format("El curso con id %d no existe", cursoId)), HttpStatus.NOT_FOUND);
        }
        catch(FeignException ex) {
            return handleFeignExceptionResponse(ex);
        }
    }

    @PatchMapping("/{id}/eliminar-usuario/{usuarioId}")
    public ResponseEntity<Map<String, String>> eliminarCurso(@PathVariable(name = "id") Long cursoId,
                                                             @PathVariable Long usuarioId) {
        Optional<Curso> oCurso = cursoService.eliminarUsuario(cursoId, usuarioId);
        if(oCurso.isPresent()) {
            return new ResponseEntity<>(Collections.singletonMap("mensaje",
                    String.format("El usuario con id %d ha sido eliminado del curso con id %d correctamente",
                            usuarioId, cursoId)), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(Collections.singletonMap("mensaje",
                    String.format("No hay relación entre el curso con id %d y el usuario con id %d", cursoId, usuarioId)),
                    HttpStatus.NOT_FOUND);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/eliminar-usuario/{usuarioId}")
    public void eliminarCursoUsuarioPorId(@PathVariable Long usuarioId) {
        cursoService.eliminarCursoUsuarioPorId(usuarioId);
    }

    private ResponseEntity<Map<String, String>> createNotValidResponse(BindingResult result) {
        Map<String, String> errors = result.getFieldErrors().stream()
                // Versión simplificada de la expresión "toMap(fe -> fe.getField(), fe -> fe.getDefaultMessage())"
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return ResponseEntity.badRequest().body(errors);
    }

    private ResponseEntity<?> handleFeignExceptionResponse(FeignException ex) {
        // Nota: El método "contentUTF8" de la excepción devuelve un Json en formato String con el cuerpo de la
        // respuesta de la petición http
        switch(ex.status()) {
            case 404:
                return new ResponseEntity<>(ex.contentUTF8(), HttpStatus.NOT_FOUND);
            case 400:
                return new ResponseEntity<>(ex.contentUTF8(), HttpStatus.BAD_REQUEST);
            default: {
                ex.printStackTrace();
                return ResponseEntity.internalServerError().body(Collections.singletonMap("mensaje",
                        "Error de conexión con el microservicio usuarios. Hable con el administrador"));
            }
        }
    }
}
