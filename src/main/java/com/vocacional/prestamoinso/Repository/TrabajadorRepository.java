package com.vocacional.prestamoinso.Repository;

import com.vocacional.prestamoinso.Entity.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {
    Optional<Trabajador> findByNombreAndApellido(String nombre, String apellido);
    Trabajador findByUsername(String username);
}
