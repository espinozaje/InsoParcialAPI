package com.vocacional.prestamoinso.Repository;

import com.vocacional.prestamoinso.Entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo,Long> {
    List<Prestamo> findByCliente_NroDocumento(String dni);
}
