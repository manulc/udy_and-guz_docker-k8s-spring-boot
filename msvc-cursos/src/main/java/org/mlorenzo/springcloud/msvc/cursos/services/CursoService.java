package org.mlorenzo.springcloud.msvc.cursos.services;

import org.mlorenzo.springcloud.msvc.cursos.entities.Curso;
import org.mlorenzo.springcloud.msvc.cursos.models.Usuario;

import java.util.List;
import java.util.Optional;

public interface CursoService {
    List<Curso> listar();
    Optional<Curso> obtenerPorId(Long id);
    Optional<Curso> obtenerPorIdConUsuarios(Long id, String authorizationHeader);
    Curso guardar(Curso curso);
    Optional<Curso> editar(Curso curso, Long id);
    void eliminar(Curso curso);
    Optional<Curso> asignarUsuario(Long cursoId, Long usuarioId, String authorizationHeader);
    Optional<Usuario> crearUsuario(Usuario usuario, Long cursoId, String authorizationHeader);
    Optional<Curso> eliminarUsuario(Long cursoId, Long usuarioId);
    void eliminarCursoUsuarioPorId(Long usuarioId);
}
