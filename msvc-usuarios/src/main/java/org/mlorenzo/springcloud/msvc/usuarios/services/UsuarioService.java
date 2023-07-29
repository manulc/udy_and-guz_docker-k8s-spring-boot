package org.mlorenzo.springcloud.msvc.usuarios.services;

import org.mlorenzo.springcloud.msvc.usuarios.entities.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> listar();
    List<Usuario> listarPorIds(Iterable<Long> ids);
    Optional<Usuario> obtenerPorId(Long id);
    Optional<Usuario> obtenerPorEmail(String email);
    Usuario guardar(Usuario usuario);
    Optional<Usuario> editar(Usuario usuario, Long id);
    void eliminar(Usuario usuario);
}
