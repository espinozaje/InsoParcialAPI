package com.vocacional.prestamoinso.Service;

import com.vocacional.prestamoinso.DTO.ClienteDTO;
import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Entity.CronogramaPagos;
import com.vocacional.prestamoinso.Entity.Prestamo;
import com.vocacional.prestamoinso.Mapper.ClienteMapper;
import com.vocacional.prestamoinso.Repository.ClienteRepository;
import com.vocacional.prestamoinso.Repository.CronogramaPagosRepository;
import com.vocacional.prestamoinso.Repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrestamoService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ClienteMapper clienteMapper;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private CronogramaPagosRepository cronogramaPagosRepository;


    public void eliminarPrestamo(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Elimina el préstamo y su cronograma de pagos relacionado
        prestamoRepository.delete(prestamo);
    }


    public Prestamo crearPrestamo(Long clienteId, double monto, int plazo, double interes) {
        // Busca el cliente en la base de datos por su ID
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setDni(cliente.getDni());// Asegúrate de que Prestamo tenga un campo para ClienteDTO
        prestamo.setMonto(monto);
        prestamo.setInteres(interes);
        prestamo.setPlazo(plazo);
        // Generar cronograma de pagos
        List<CronogramaPagos> cronograma = generarCronograma(prestamo);
        for (CronogramaPagos pago : cronograma) {
            pago.setPrestamo(prestamo);  // Asocia el pago con el préstamo
        }
        prestamo.setCronogramaPagos(cronograma);

        return prestamoRepository.save(prestamo);
    }

    private List<CronogramaPagos> generarCronograma(Prestamo prestamo) {
        List<CronogramaPagos> cronograma = new ArrayList<>();
        double cuotaMensual = calcularMontoCuota(prestamo);
        for (int i = 1; i <= prestamo.getPlazo(); i++) {
            CronogramaPagos pago = new CronogramaPagos();
            pago.setPrestamo(prestamo);
            pago.setFechaPago(LocalDate.now().plusMonths(i));
            pago.setMontoCuota(cuotaMensual);
            pago.setEstado("Pendiente");
            cronograma.add(pago);
        }
        return cronograma;
    }

    public List<CronogramaPagos> obtenerCronogramaConEstadosActualizados(Prestamo prestamo) {
        List<CronogramaPagos> cronograma = prestamo.getCronogramaPagos();
        for (CronogramaPagos pago : cronograma) {
            if (pago.getEstado().equals("Pendiente") && pago.getFechaPago().isBefore(LocalDate.now())) {
                pago.setEstado("Deuda");
                cronogramaPagosRepository.save(pago);  // Actualiza el estado si es necesario
            }
        }
        return cronograma;
    }

    public void marcarComoPagado(Long id) {
        CronogramaPagos pago = cronogramaPagosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setEstado("Pagado");
        cronogramaPagosRepository.save(pago);
    }



    private double calcularMontoCuota(Prestamo prestamo) {
        if (prestamo.getPlazo()==1){
            prestamo.setInteres(10);
            double totalPagar = prestamo.getMonto() + (prestamo.getMonto() * prestamo.getInteres() / 100);
            return totalPagar / prestamo.getPlazo();
        }
        else {
            prestamo.setInteres(20);
            double totalPagar = prestamo.getMonto() + (prestamo.getMonto() * prestamo.getInteres() / 100);
            return totalPagar / prestamo.getPlazo();
        }
    }

    public List<Prestamo> findByClienteDni(String dni){
        return prestamoRepository.findByCliente_Dni(dni);
    }

}
