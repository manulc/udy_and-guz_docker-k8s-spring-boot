package org.mlorenzo.springcloud.msvc.usuarios.repositories;

import org.mlorenzo.springcloud.msvc.usuarios.entities.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    // 3 formas de saber si existe un usuario en la base de datos a partir de un email
    Optional<Usuario> findByEmail(String email);

    @Query("select u from Usuario u where u.email = ?1")
    Optional<Usuario> porEmail(String email);

    boolean existsByEmail(String email);
}
