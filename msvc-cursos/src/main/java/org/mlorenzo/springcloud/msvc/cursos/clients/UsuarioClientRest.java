package org.mlorenzo.springcloud.msvc.cursos.clients;

import org.mlorenzo.springcloud.msvc.cursos.models.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "msvc-usuarios", url = "${msvc.usuarios.host}:8001")
public interface UsuarioClientRest {

    @GetMapping("{id}")
    Usuario obtenerUsuario(@PathVariable Long id);

    @PostMapping
    Usuario crearUsuario(Usuario usuario);

    @GetMapping("listar-por-ids")
    List<Usuario> obtenerUsuariosPorIds(@RequestParam List<Long> ids);
}
