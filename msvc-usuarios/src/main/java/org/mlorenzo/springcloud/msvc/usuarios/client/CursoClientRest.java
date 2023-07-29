package org.mlorenzo.springcloud.msvc.usuarios.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "msvc-cursos", url = "${msvc.cursos.host}:8002")
public interface CursoClientRest {

    @PatchMapping("/eliminar-usuario/{id}")
    void eliminarCursoUsuarioPorId(@PathVariable Long id);
}
