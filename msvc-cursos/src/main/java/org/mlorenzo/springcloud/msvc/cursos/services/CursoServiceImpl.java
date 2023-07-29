package org.mlorenzo.springcloud.msvc.cursos.services;

import org.mlorenzo.springcloud.msvc.cursos.clients.UsuarioClientRest;
import org.mlorenzo.springcloud.msvc.cursos.entities.Curso;
import org.mlorenzo.springcloud.msvc.cursos.entities.CursoUsuario;
import org.mlorenzo.springcloud.msvc.cursos.models.Usuario;
import org.mlorenzo.springcloud.msvc.cursos.repositories.CursoRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CursoServiceImpl implements CursoService {
    private final CursoRepository cursoRepository;
    private final UsuarioClientRest usuarioClientRest;

    public CursoServiceImpl(CursoRepository cursoRepository, UsuarioClientRest usuarioClientRest) {
        this.cursoRepository = cursoRepository;
        this.usuarioClientRest = usuarioClientRest;
    }

    @Override
    public List<Curso> listar() {
        return (List<Curso>)cursoRepository.findAll();
    }

    @Override
    public Optional<Curso> obtenerPorId(Long id) {
        return cursoRepository.findById(id);
    }

    @Override
    public Optional<Curso> obtenerPorIdConUsuarios(Long id) {
        return obtenerPorId(id)
                .map(curso -> {
                    final List<CursoUsuario> cursoUsuarios = curso.getCursoUsuarios();
                    if(!cursoUsuarios.isEmpty()) {
                        List<Long> idsUsuarios = cursoUsuarios.stream()
                                // Versión simplificada de la expresión "usuario -> usuario.getUsuarioId()"
                                .map(CursoUsuario::getUsuarioId)
                                .collect(Collectors.toList());
                        List<Usuario> usuarios = usuarioClientRest.obtenerUsuariosPorIds(idsUsuarios);
                        curso.setUsuarios(usuarios);
                    }
                    return curso;
                });
    }

    @Override
    public Curso guardar(Curso curso) {
        return cursoRepository.save(curso);
    }

    @Override
    public Optional<Curso> editar(Curso curso, Long id) {
        Optional<Curso> oCurso = obtenerPorId(id);
        Curso savedCurso = null;
        if(oCurso.isPresent()) {
            final Curso cursoDB = oCurso.get();
            BeanUtils.copyProperties(curso, cursoDB, "id");
            savedCurso = cursoRepository.save(cursoDB);
        }
        return Optional.ofNullable(savedCurso);
    }

    @Override
    public void eliminar(Curso curso) {
        cursoRepository.delete(curso);
    }

    @Override
    public Optional<Curso> asignarUsuario(Long cursoId, Long usuarioId) {
        return obtenerPorId(cursoId)
                .map(curso -> {
                    Usuario usuario = usuarioClientRest.obtenerUsuario(usuarioId);
                    CursoUsuario cursoUsuario = new CursoUsuario();
                    cursoUsuario.setUsuarioId(usuario.getId());
                    curso.addUsuario(cursoUsuario);
                    cursoRepository.save(curso);
                    return curso;
                });
    }

    @Override
    public Optional<Usuario> crearUsuario(Usuario usuario, Long cursoId) {
        Optional<Curso> oCurso = obtenerPorId(cursoId);
        if(oCurso.isPresent()) {
            Curso curso = oCurso.get();
            Usuario nuevoUsuario = usuarioClientRest.crearUsuario(usuario);
            CursoUsuario cursoUsuario = new CursoUsuario();
            cursoUsuario.setUsuarioId(nuevoUsuario.getId());
            curso.addUsuario(cursoUsuario);
            cursoRepository.save(curso);
            return Optional.of(nuevoUsuario);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Curso> eliminarUsuario(Long cursoId, Long usuarioId) {
        return cursoRepository.findByIdAndCursoUsuarios_UsuarioId(cursoId, usuarioId)
                .map(curso -> {
                    CursoUsuario cursoUsuario = new CursoUsuario();
                    cursoUsuario.setUsuarioId(usuarioId);
                    curso.removeUsuario(cursoUsuario);
                    cursoRepository.save(curso);
                    return curso;
                });
    }

    @Override
    public void eliminarCursoUsuarioPorId(Long usuarioId) {
        cursoRepository.eliminarCursoUsuarioPorId(usuarioId);
    }
}
