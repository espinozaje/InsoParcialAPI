package com.vocacional.prestamoinso.Repository;

import com.vocacional.prestamoinso.Entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente,Long> {
    Optional<Cliente> findById(Long id);
    Cliente findByDni(String dni);

    boolean existsByDni(String dni);
}
