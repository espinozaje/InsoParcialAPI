package com.vocacional.prestamoinso.Controller;

import com.vocacional.prestamoinso.DTO.PrestamoDTO;
import com.vocacional.prestamoinso.DTO.ReniecResponseDTO;
import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Entity.Prestamo;
import com.vocacional.prestamoinso.Mapper.PrestamoMapper;
import com.vocacional.prestamoinso.Repository.ClienteRepository;
import com.vocacional.prestamoinso.Service.ClienteService;
import com.vocacional.prestamoinso.Service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PrestamoMapper prestamoMapper;

    @PostMapping("/crear")
    public ResponseEntity<Prestamo> crearPrestamo(@RequestBody PrestamoDTO request) {
        // Validar el DNI del cliente con RENIEC
        ReniecResponseDTO datosReniec = clienteService.validarDNI(request.getDni());

        if (datosReniec == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // No se encontró el DNI en RENIEC
        }

        // Verificar si el cliente existe en la base de datos
        Cliente cliente = clienteRepository.findByDni(request.getDni());
        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Cliente no encontrado
        }

        // Aquí puedes continuar con la lógica para crear el préstamo si el DNI es válido
        Prestamo prestamo = prestamoService.crearPrestamo(
                cliente.getId(), request.getMonto(), request.getPlazo(), request.getInteres()
        );
        return ResponseEntity.ok(prestamo);
    }

    @GetMapping("/prestamoPorDni/{dni}")
    public ResponseEntity<List<PrestamoDTO>> getPrestamosporDNI(@PathVariable String dni) {
        List<Prestamo> prestamos = prestamoService.findByClienteDni(dni);

        for (Prestamo prestamo : prestamos) {
            prestamo.setCronogramaPagos(prestamoService.obtenerCronogramaConEstadosActualizados(prestamo));
        }

        List<PrestamoDTO> prestamoDTO = prestamos.stream()
                .map(prestamoMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(prestamoDTO);
    }

    @PutMapping("/marcarPagado/{id}")
    public ResponseEntity<Void> marcarComoPagado(@PathVariable Long id) {
        prestamoService.marcarComoPagado(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarPrestamo(@PathVariable Long id) {
        prestamoService.eliminarPrestamo(id);
        return ResponseEntity.ok().build();
    }
}
