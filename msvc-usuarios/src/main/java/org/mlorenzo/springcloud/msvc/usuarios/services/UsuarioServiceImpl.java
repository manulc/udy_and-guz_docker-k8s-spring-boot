package org.mlorenzo.springcloud.msvc.usuarios.services;

import org.mlorenzo.springcloud.msvc.usuarios.client.CursoClientRest;
import org.mlorenzo.springcloud.msvc.usuarios.entities.Usuario;
import org.mlorenzo.springcloud.msvc.usuarios.exceptions.EmailExistsException;
import org.mlorenzo.springcloud.msvc.usuarios.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final CursoClientRest cursoClientRest;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, CursoClientRest cursoClientRest) {
        this.usuarioRepository = usuarioRepository;
        this.cursoClientRest = cursoClientRest;
    }

    @Override
    public List<Usuario> listar() {
        return (List<Usuario>)usuarioRepository.findAll();
    }

    @Override
    public List<Usuario> listarPorIds(Iterable<Long> ids) {
        return (List<Usuario>)usuarioRepository.findAllById(ids);
    }

    @Override
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        final String email = usuario.getEmail();
        if(obtenerPorEmail(email).isPresent())
            throw new EmailExistsException(String.format("El email %s ya existe", email));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> editar(Usuario usuario, Long id) {
        final String email = usuario.getEmail();
        return obtenerPorId(id)
                .map(usuarioDB -> {
                    if(!email.equals(usuarioDB.getEmail()) && obtenerPorEmail(email).isPresent())
                        throw new EmailExistsException(String.format("El email %s ya existe", email));
                    usuario.setId(usuarioDB.getId());
                    return usuarioRepository.save(usuario);
                });
    }

    // Nota: En este caso, no incluimos la anotación @Transaction porque primero nos comunicamos con el microservicio
    // "cursos" y después eliminamos el usuario. Entonces, si la comunicación con el microservicio "curso" provoca
    // una excepción(Por ejemplo, si dicho microservicio no esta disponible), la sentencia que elimina al usuario
    // de la base de datos no llegará a ejecutarse.
    // Por otra parte, si esa sentencia llega a estar en primer lugar, en ese caso sí tendría que usarse la anotaciçon
    // @Transaction para que se haga automáticamente un Rollback de la eliminación del usuario de la base de datos
    // cuando la comunicación con el microservicio "cursos" provoque una excepción

    // Primera opción válida
    @Override
    public void eliminar(Usuario usuario) {
        usuarioRepository.delete(usuario);
        cursoClientRest.eliminarCursoUsuarioPorId(usuario.getId());
    }

    // Segunda opción válida
    /*@Transactional
    @Override
    public void eliminar(Usuario usuario) {
        usuarioRepository.delete(usuario);
        cursoClientRest.eliminarCursoUsuarioPorId(usuario.getId());
    }*/
}
