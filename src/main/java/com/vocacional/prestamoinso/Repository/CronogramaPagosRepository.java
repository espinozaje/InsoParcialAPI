package com.vocacional.prestamoinso.Repository;

import com.vocacional.prestamoinso.Entity.CronogramaPagos;
import com.vocacional.prestamoinso.Entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CronogramaPagosRepository extends JpaRepository<CronogramaPagos,Long> {

    List<CronogramaPagos> findByPrestamoIdAndEstadoOrderByFechaPagoAsc(Long prestamoId, String estado);
    List<CronogramaPagos> findByPrestamoId(Long id);
    List<CronogramaPagos> findByPrestamoAndEstado(Prestamo prestamo, String estado);

    // Método para obtener el total de pagos realizados para un préstamo específico
    @Query("SELECT SUM(cp.montoCuota) FROM CronogramaPagos cp WHERE cp.prestamo.id = :prestamoId AND cp.estado = 'Pagado'")
    Double obtenerTotalPagosPrestamo(@Param("prestamoId") Long prestamoId);

}
