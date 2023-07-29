package org.mlorenzo.springcloud.msvc.cursos.repositories;

import org.mlorenzo.springcloud.msvc.cursos.entities.Curso;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CursoRepository extends CrudRepository<Curso, Long> {
    Optional<Curso> findByIdAndCursoUsuarios_UsuarioId(Long cursoId, Long usuarioId);

    @Transactional
    @Modifying
    @Query("delete from CursoUsuario cu where cu.usuarioId = ?1")
    void eliminarCursoUsuarioPorId(Long id);
}
