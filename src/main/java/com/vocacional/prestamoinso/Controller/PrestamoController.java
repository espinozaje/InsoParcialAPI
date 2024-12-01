package com.vocacional.prestamoinso.Controller;

import com.vocacional.prestamoinso.DTO.PrestamoDTO;
import com.vocacional.prestamoinso.DTO.ReniecResponseDTO;
import com.vocacional.prestamoinso.DTO.SunatResponseDTO;
import com.vocacional.prestamoinso.Entity.Cliente;
import com.vocacional.prestamoinso.Entity.Prestamo;
import com.vocacional.prestamoinso.Mapper.PrestamoMapper;
import com.vocacional.prestamoinso.Repository.ClienteRepository;
import com.vocacional.prestamoinso.Service.ClienteService;
import com.vocacional.prestamoinso.Service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    public ResponseEntity<byte[]> crearPrestamo(@RequestBody PrestamoDTO request) {
        // Validar el DNI del cliente con RENIEC
        if (request.getNroDocumento().length() != 11) {
            ReniecResponseDTO datosReniec = clienteService.validarDNI(request.getNroDocumento());
            if (datosReniec == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // No se encontró el DNI en RENIEC
            }
        } else {
            SunatResponseDTO datosReniec = clienteService.validarRUC(request.getNroDocumento());
            if (datosReniec == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // No se encontró el DNI en RENIEC
            }
        }

        // Verificar si el cliente existe en la base de datos
        Cliente cliente = clienteRepository.findByNroDocumento(request.getNroDocumento());
        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Cliente no encontrado
        }

        // Crear el préstamo
        Prestamo prestamo = prestamoService.crearPrestamo(
                cliente.getNroDocumento(), request.getMonto(), request.getPlazo(), request.getInteres()
        );

        // Ruta del PDF generado
        String pdfPath = "prestamo_" + prestamo.getId() + ".pdf";

        try {
            // Leer el archivo PDF como un array de bytes
            File pdfFile = new File(pdfPath);
            byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());

            // Configurar los encabezados HTTP para enviar el PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(pdfFile.getName()).build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Error al procesar el archivo
        }
    }

    @GetMapping("/prestamoPorDni/{dni}")
    public ResponseEntity<List<PrestamoDTO>> getPrestamosporDNI(@PathVariable String dni) {
        List<Prestamo> prestamos = prestamoService.findByClienteNroDocumento(dni);

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


    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
        try {
            byte[] pdfBytes = prestamoService.generarPdf(id); // Llamar al servicio para generar el PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "prestamo_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Manejar errores si no se puede generar el PDF
        }
    }


    @GetMapping("/prestamos-pendientes")
    public List<Prestamo> obtenerPrestamosPendientes() {
        return prestamoService.listarPrestamosPendientes();
    }
}
