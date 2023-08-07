package org.mlorenzo.springcloud.msvc.cursos.clients;

import org.mlorenzo.springcloud.msvc.cursos.models.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "msvc-usuarios")
public interface UsuarioClientRest {

    @GetMapping("{id}")
    Usuario obtenerUsuario(@PathVariable Long id,
                           @RequestHeader(name = "Authorization") String authorizationHeader);

    @PostMapping
    Usuario crearUsuario(Usuario usuario,
                         @RequestHeader(value = "Authorization") String authorizationHeader);

    @GetMapping("listar-por-ids")
    List<Usuario> obtenerUsuariosPorIds(@RequestParam List<Long> ids,
                                        @RequestHeader("Authorization") String authorizationHeader);
}
